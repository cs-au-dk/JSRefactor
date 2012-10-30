package dk.brics.jsrefactoring.extractmodule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.Master;

/**
 * This analysis determines, for a given set of root nodes, an over-approximation of the set
 * of user functions that may be invoked during evaluation of this code.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class CalleeAnalysis {
	private final Master input;
	
	public CalleeAnalysis(Master input) {
		this.input = input;
	}
	
	public Set<IFunction> getTransitiveCallees(Set<? extends NodeInterface> roots) {
		Set<IFunction> callees = new HashSet<IFunction>();
		LinkedList<IFunction> worklist = new LinkedList<IFunction>();
		
		worklist.addAll(getCallees(roots));
		callees.addAll(worklist);
		
		while(!worklist.isEmpty()) {
			IFunction fun = worklist.pop();
			for(IFunction callee : getCallees(fun.getBody()))
				if(callees.add(callee))
					worklist.push(callee);
		}
		return callees;
	}
	
	private Set<IFunction> getCallees(NodeInterface root) {
		return getCallees(Collections.singleton(root));
	}
	
	// determine direct callees of given set of nodes
	// TODO: does not consider calls to toString/valueOf
	private Set<IFunction> getCallees(Collection<? extends NodeInterface> roots) {
		final Set<IFunction> called = new HashSet<IFunction>();
		for(NodeInterface root : roots) {
			root.apply(new DepthFirstAdapter() {
				@Override public void caseAFunctionDeclStmt(AFunctionDeclStmt node) {}
				@Override public void caseAFunctionExp(AFunctionExp node) {}
				
				@Override
				public void inAInvokeExp(AInvokeExp node) {
					inIInvocationNode(node);
					super.inAInvokeExp(node);
				}
				
				@Override
				public void inANewExp(ANewExp node) {
					inIInvocationNode(node);
					super.inANewExp(node);
				}
				
				private void inIInvocationNode(IInvocationNode inv) {
					for(FunctionValue fv : input.getCalledFunctions(inv))
						if(fv instanceof UserFunctionValue)
							called.add(input.getFunctionNode((UserFunctionValue)fv));					
				}
			});
		}
		return called;
	}
}
