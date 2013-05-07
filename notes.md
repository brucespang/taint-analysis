## Unread, Potentially Relevant Papers:

	- certification of programs for secure information flow
	- language-based information-flow security
	- soot - a java bytecode optimization framework (http://www.sable.mcgill.ca/soot/)
	- anything that cites soot http://scholar.google.com/scholar?cites=9446044426713074986&as_sdt=40000005&sciodt=0,22&hl=en
	- Dare http://siis.cse.psu.edu/dare/
	- http://mtv2.univ-fcomte.fr/doc/laurent_12_11_2012.pdf

# Read Relevant Papers:

	- TaintDroid
	- "A Graph-Free Approach to Data-Flow Analysis"

General idea: Decide reachability on data-flow graph between private source and public sink.

Can use Shimple (SSA form, inject in wstp) + that paper I found on static analysis for taint via graph reachability, or Jimple (inject in wjtp) and figure things out.
Soot should be in whole program mode (-w).


This is supposedly nicer than Soot: http://asm.ow2.org/doc/tutorial-asm-2.0.html
