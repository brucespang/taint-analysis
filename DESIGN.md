# Static Taint Analysis

## First Attempt

I've got an intra-procedural soot analysis working, that proves that there might exist a way data can flow between a `System.console().readLine()` call and a `System.out.println()` call. It is a severe overapproximation, in that if a tainted variable appears on the right-hand side of an assignment, it taints the left-hand side. However, it does support tainting if-branches that are conditioned on a tainted variable, retaining taint after a mutation to an assigned variable, and de-tainting a variable that's re-assigned to something taintless.

Some problems:

	- It seems like tainting doesn't work on the else branch of an if statement.
	- It's an intra-procedural analysis, so we just assume that a response from a function that takes a tainted value as an argument is tainted.
	- The error output is absolutely atrocious.
	- The handling of class variables is incorrect.
	- There's some truly atrocious reflection going on, since getUseBoxes() isn't part of an interface in soot.
	- There's no nice way to condition on type in java, so there's lots of unnecessary casting going on.
	- The current way to specify sources of and sinks for taint is unpleasant.
