package dk.brics.jspointers.flowgraph.analysis;

import java.util.Set;

import dk.brics.tajs.flowgraph.Function;
import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.optimizer2.Decorator;
import dk.brics.tajs.optimizer2.FixpointSolver;

/**
 * Determines the set of relevant live variables before and after each statement.
 * A variable is <em>relevant live</em> if its value can be <em>read</em> by a statement,
 * where <em>read</em> is defined by the provided {@link ReadVarsInterface}.
 */
public class Liveness {
	
	private FixpointSolver<Set<LocalVariable>> solver;
	
	/**
	 * A liveness analysis using the {@link ReadVarsVisitor} as the definition
	 * of read variables.
	 * @param flowGraph flow graph
	 * @param func function to analyze
	 */
	public Liveness(Decorator flowGraph, Function func, Set<String> privateVars) {
		this(flowGraph, func, privateVars, new ReadVarsVisitor());
	}
	public Liveness(Decorator flowGraph, Function func, Set<String> privateVars, ReadVarsInterface readvars) {
		solver = new FixpointSolver<Set<LocalVariable>>(new LiveVarsAnalysis(flowGraph, readvars, privateVars), func);
		solver.solve();
	}
	
	public boolean isLiveAfter(Node node, int var) {
	    return getLiveSetAfter(node).contains(new LocalTemporaryVariable(var));
	}
	public boolean isLiveAfter(Node node, String varname) {
	    return getLiveSetAfter(node).contains(new LocalProgramVariable(varname));
	}
	public boolean isLiveAfter(Node node, LocalVariable var) {
		return getLiveSetAfter(node).contains(var);
	}
    public boolean isLiveBefore(Node node, int var) {
        return getLiveSetBefore(node).contains(new LocalTemporaryVariable(var));
    }
    public boolean isLiveBefore(Node node, String varname) {
        return getLiveSetBefore(node).contains(new LocalProgramVariable(varname));
    }
    public boolean isLiveBefore(Node node, LocalVariable var) {
    	return getLiveSetBefore(node).contains(var);
    }
	
    public Set<LocalVariable> getLiveSetBefore(Node node) {
		return solver.getLatticePoint(node);
	}
	
    public Set<LocalVariable> getLiveSetAfter(Node node) {
		return solver.getJoinPoint(node);
	}
	
}
