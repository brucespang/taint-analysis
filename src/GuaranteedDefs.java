import java.util.*;

import soot.*;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;

/**
 * Find all locals guaranteed to be defined at (just before) a given
 * program point.
 *
 * @author Navindra Umanee
 **/
public class GuaranteedDefs {
    protected Map<Unit, List> unitToGuaranteedDefs;

    public GuaranteedDefs(UnitGraph graph) {
        if(Options.v().verbose())
            G.v().out.println("[" + graph.getBody().getMethod().getName() +
			      "]     Constructing GuaranteedDefs...");

        GuaranteedDefsAnalysis analysis = new GuaranteedDefsAnalysis(graph);

        // build map
	unitToGuaranteedDefs = new HashMap<Unit, List>(graph.size() * 2 + 1);

	for(Unit s : graph) {
	    FlowSet set = (FlowSet) analysis.getFlowBefore(s);
	    unitToGuaranteedDefs.put(s, Collections.unmodifiableList(set.toList()));
	}
    }

    /**
     * Returns a list of locals guaranteed to be defined at (just
     * before) program point <tt>s</tt>.
     **/
    public List getGuaranteedDefs(Unit s) {
        return unitToGuaranteedDefs.get(s);
    }
}

class GuaranteedDefsAnalysis extends ForwardFlowAnalysis<Unit,FlowSet> {

    Map<Unit, FlowSet> unitToGenerateSet;

    public GuaranteedDefsAnalysis(DirectedGraph<Unit> graph) {
	super(graph);

	DominatorsFinder df = new MHGDominatorsFinder(graph);
        unitToGenerateSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1);

	// pre-compute generate sets
        for(Unit s : graph){
            FlowSet genSet = new ArraySparseSet();

            for(Unit dom : df.getDominators(s))
                for(ValueBox box : dom.getDefBoxes())
                    if(box.getValue() instanceof Local)
                        genSet.add(box.getValue(), genSet);

            unitToGenerateSet.put(s, genSet);
        }


	doAnalysis();
    }

    protected FlowSet newInitialFlow() {
	return new ArraySparseSet();
    }

    protected FlowSet entryInitialFlow() {
	return new ArraySparseSet();
    }

    protected void copy(FlowSet src, FlowSet dest) {
	src.copy(dest);
    }

    protected void merge(FlowSet in1, FlowSet in2, FlowSet out) {
	in1.intersection(in2, out);
    }

    protected void flowThrough(FlowSet in, Unit node, FlowSet out) {
	in.union(unitToGenerateSet.get(node), out);
    }
}
