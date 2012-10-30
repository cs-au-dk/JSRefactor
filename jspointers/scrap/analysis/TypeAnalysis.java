package dk.brics.jspointers.flowgraph.analysis;

import java.util.Collections;
import java.util.Set;

import dk.brics.tajs.flowgraph.Function;
import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.flowgraph.nodes.BinaryOperatorNode;
import dk.brics.tajs.optimizer2.Decorator;
import dk.brics.tajs.optimizer2.FixpointSolver;

public class TypeAnalysis implements ReadVarsInterface {
	
	private ReadVarsVisitor readvars = new ReadVarsVisitor();
	private FixpointSolver<Set<Integer>> solver;
	
	/**
	 * Returns the set of variables that might be strings or
	 * objects at the given node. Non-live variables may be
	 * excluded.
	 * @param node a node from the same function passed to the constructor
	 * @return unmodifiable set
	 */
	public Set<Integer> getVariables(Node node) {
		return solver.getJoinPoint(node);
	}
	
	public TypeAnalysis(Decorator fgd, Function func, Liveness liveness) {
//		ForwardAnalysis<Set<Integer>> fa = new ForwardAnalysis<Set<Integer>>(fgd, func);
//		map = fa.solve(new TypeAnalysisTransfer(liveness));
//		solver = new FixpointSolver<Set<Integer>>(new TypeAnalysisFlow(fgd, liveness), func);
		solver = new FixpointSolver<Set<Integer>>(new TypeAnalysisFlow(fgd, liveness), func);
		solver.solve();
	}
	
	/**
	 * Returns the relevant variables read by the given node.
	 * <p/>
	 * This function takes into account that binary plus statements
	 * do not use their arguments if both arguments are known to
	 * be non-string non-object values.
	 * Apart from that, it defaults to the same semantics as {@link ReadVarsVisitor}.
	 */
	public int[] getReadVars(Node node) {
		if (node instanceof BinaryOperatorNode) {
			BinaryOperatorNode bin = (BinaryOperatorNode)node;
			switch (bin.getOperator()) {
			case ADD:
				Set<Integer> vars = getVariables(node);
				if (!vars.contains(bin.getArg1Var()) && !vars.contains(bin.getArg2Var())) {
					return new int[0];
				}
				break;
			}
		}
		return readvars.getReadVars(node);
	}
	
}
