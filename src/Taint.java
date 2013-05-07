import java.util.*;
import java.lang.reflect.Method;
import soot.*;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.baf.*;
import soot.jimple.*;

public class Taint {
    public static void main(String[] args) {
	PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new BodyTransformer() {
		protected void internalTransform(Body body, String phase, Map options) {
		    new TaintAnalysisWrapper(new ExceptionalUnitGraph(body));
		}
	    }));
	soot.Main.main(args);
    }
}

class TaintAnalysisWrapper {
    public TaintAnalysisWrapper(UnitGraph graph) {
	TaintAnalysis analysis = new TaintAnalysis(graph);

	if(analysis.taintedSinks.size() > 0) {
	    G.v().out.print("FAILURE: ");
	    G.v().out.println(analysis.taintedSinks);
	}
    }
}

interface GetUseBoxes {
    public List<ValueBox> getUseBoxes();
}

class TaintAnalysis extends ForwardFlowAnalysis<Unit, Set<Value>> {
    public Map<Unit, Set<Set<Value>>> taintedSinks;

    public TaintAnalysis(UnitGraph graph) {
	super(graph);

	taintedSinks = new HashMap();

	doAnalysis();
    }

    protected Set<Value> newInitialFlow() {
	return new HashSet();
    }

    protected Set<Value> entryInitialFlow() {
	return new HashSet();
    }

    protected void copy(Set<Value> src, Set<Value> dest) {
	dest.removeAll(dest);
	dest.addAll(src);
    }

    // Called after if/else blocks, with the result of the analysis from both
    // branches.
    protected void merge(Set<Value> in1, Set<Value> in2, Set<Value> out) {
	out.removeAll(out);
	out.addAll(in1);
	out.addAll(in2);
    }

    protected void flowThrough(Set<Value> in, Unit node, Set<Value> out) {
	Set<Value> filteredIn = stillTaintedValues(in, node);
	Set<Value> newOut = newTaintedValues(filteredIn, node);

	out.removeAll(out);
	out.addAll(filteredIn);
	out.addAll(newOut);

	System.out.println(in);
	System.out.println(node);
	if(isTaintedPublicSink(node, in)) {
	    if(!taintedSinks.containsKey(node))
		taintedSinks.put(node, new HashSet());

	    taintedSinks.get(node).add(in);
	}
    }

    protected Set<Value> stillTaintedValues(Set<Value> in, Unit node) {
	return in;
    }

    // It would be sweet if java had a way to duck type, but it doesn't so we have to do this.
    protected boolean containsValues(Collection<Value> vs, Object s) {
	for(Value v : vs)
	    if(containsValue(v, s))
		return true;
	return false;
    }

    protected boolean containsValue(Value v, Object s) {
	try {
	    // I'm so sorry.
	    Method m = s.getClass().getMethod("getUseBoxes");
	    for(ValueBox b : (Collection<ValueBox>) m.invoke(s))
		if(b.getValue().equals(v))
		    return true;
	    return false;
	} catch(Exception e) {
	    return false;
	}
    }

    protected Set<Value> newTaintedValues(Set<Value> in, Unit node) {
	Set<Value> out = new HashSet();

	if(containsValues(in, node)) {
	    if(node instanceof AssignStmt) {
		out.add(((AssignStmt) node).getLeftOpBox().getValue());
	    } else if(node instanceof IfStmt) {
		IfStmt i = (IfStmt) node;
		if(i.getTarget() instanceof AssignStmt)
		    out.add(((AssignStmt) i.getTarget()).getLeftOpBox().getValue());
	    }
	} else if(node instanceof AssignStmt) {
	    AssignStmt assn = (AssignStmt) node;

	    if(isPrivateSource(assn.getRightOpBox().getValue()))
		out.add(assn.getLeftOpBox().getValue());
	}

	return out;
    }

    protected boolean isPrivateSource(Value u) {
	if(u instanceof VirtualInvokeExpr) {
	    VirtualInvokeExpr e = (VirtualInvokeExpr) u;
	    SootMethod m = e.getMethod();

	    if(m.getName().equals("readLine") &&
	       m.getDeclaringClass().getName().equals("java.io.Console"))
		return true;
	}

	return false;
    }

    protected boolean isTaintedPublicSink(Unit u, Set<Value> in) {
	if(u instanceof InvokeStmt) {
	    InvokeExpr e = ((InvokeStmt) u).getInvokeExpr();
	    SootMethod m = e.getMethod();
	    if(m.getName().equals("println") &&
	       m.getDeclaringClass().getName().equals("java.io.PrintStream") &&
	       containsValues(in, e))
		return true;
	}

	return false;
    }

}
