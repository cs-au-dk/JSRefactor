package dk.brics.jspointers.flowgraph.analysis;

import java.util.HashSet;
import java.util.Set;

import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.flowgraph.nodes.BinaryOperatorNode;
import dk.brics.tajs.flowgraph.nodes.CallNode;
import dk.brics.tajs.flowgraph.nodes.CatchNode;
import dk.brics.tajs.flowgraph.nodes.ConstantNode;
import dk.brics.tajs.flowgraph.nodes.DeclareFunctionNode;
import dk.brics.tajs.flowgraph.nodes.NewObjectNode;
import dk.brics.tajs.flowgraph.nodes.ReadPropertyNode;
import dk.brics.tajs.flowgraph.nodes.ReadVariableNode;
import dk.brics.tajs.flowgraph.nodes.TypeofNode;
import dk.brics.tajs.optimizer2.Decorator;
import dk.brics.tajs.optimizer2.analysis.FlowAnalysis;

public class TypeAnalysisFlow extends FlowAnalysis<Set<Integer>> {
	
	private Decorator decorator;
	private Liveness liveness;
	
	public TypeAnalysisFlow(Decorator decorator, Liveness liveness) {
		super(new SetLattice<Integer>());
		this.decorator = decorator;
		this.liveness = liveness;
	}
	
	@Override
	public Set<Node> getDependencySet(Node node) {
		return decorator.getAllSuccessorNodes(node);
	}
	@Override
	public Set<Node> getJoinSet(Node node) {
		return decorator.getAllPredecessorNodes(node);
	}
	
	private Set<Integer> addAndFilter(Set<Integer> set, int x, Node n) {
		if (!set.contains(x)) {
			Set<Integer> result = new HashSet<Integer>(set);
			result.add(x);
			result.retainAll(liveness.getLiveSetAfter(n));
			return result;
		} else {
			// don't bother removing stuff from the set, since it's faster
			// to just return it identically
			return set;
		}
	}
	private Set<Integer> removeAndFilter(Set<Integer> set, int x, Node n) {
		if (set.contains(x)) {
			Set<Integer> result = new HashSet<Integer>(set);
			result.remove(x);
			result.retainAll(liveness.getLiveSetAfter(n));
			return result;
		} else {
			return set;
		}
	}
	
	@Override
	public Set<Integer> visit(BinaryOperatorNode n, Set<Integer> l) {
		boolean maybeObjOrString = false;
		switch(n.getOperator()) {
		case ADD:
			if (l.contains(n.getArg1Var()) || l.contains(n.getArg2Var())) {
				maybeObjOrString = true;
			}
			break;
		}
		if (maybeObjOrString) {
			return addAndFilter(l, n.getResultVar(), n);
		} else {
			return removeAndFilter(l, n.getResultVar(), n);
		}
	}
	@Override
	public Set<Integer> visit(CallNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(ConstantNode n, Set<Integer> l) {
		switch (n.getType()) {
		case FUNCTION:
		case STRING:
			return addAndFilter(l, n.getResultVar(), n);
		default:
			return removeAndFilter(l, n.getResultVar(), n);
		}
	}
	@Override
	public Set<Integer> visit(DeclareFunctionNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(NewObjectNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(ReadPropertyNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(ReadVariableNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(TypeofNode n, Set<Integer> l) {
		return addAndFilter(l, n.getResultVar(), n);
	}
	@Override
	public Set<Integer> visit(CatchNode n, Set<Integer> l) {
		if (n.getTempVar() != Node.NO_VALUE) {
			return addAndFilter(l, n.getTempVar(), n);
		} else {
			return l;
		}
	}
	
}
