package dk.brics.jsrefactoring.movetoprototype;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowAnalysis;
import dk.brics.jscontrolflow.statements.IPropertyAccessStatement;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jspointers.cfg2dataflow.Controlflow2DataflowBinding;
import dk.brics.jspointers.dataflow.IDynamicPropertyAccessFlowNode;
import dk.brics.jspointers.dataflow.IPropertyAccessFlowNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadNode;
import dk.brics.jspointers.dataflow.StoreIfPresentNode;
import dk.brics.jspointers.dataflow.StoreNode;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsutil.CollectionUtil;

/**
 * Intra-procedural dependency analysis that determines, given a set <code>bases</code> of object values and
 * a property name <code>n</code>, for every CFG node in a function the set of {@link IPropertyAccessStatement}s
 * preceding it in the CFG that may read, write or delete a property <code>n</code> on one of the objects in
 * <code>bases</code>. We conservatively treat invoke statements as potential writes.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class DataDependencyAnalysis implements ForwardFlowAnalysis<Set<Statement>> {
	private Controlflow2DataflowBinding binding;
	private Master input;
	private Set<ObjectValue> bases;
	private String propertyName;
	
	public DataDependencyAnalysis(Master input, Controlflow2DataflowBinding binding, Set<ObjectValue> bases, String propertyName) {
		this.input = input;
		this.binding = binding;
		this.bases = bases;
		this.propertyName = propertyName;
	}

	@Override
	public Set<Statement> bottom() {
		return null;
	}

	@Override
	public Set<Statement> entry() {
		return Collections.<Statement>emptySet();
	}

	@Override
	public Set<Statement> transfer(Statement stmt, Set<Statement> before) {
		if(before == null)
			return null;
		Set<Statement> result = new HashSet<Statement>(before);
		if(stmt instanceof IPropertyAccessStatement) {
			for(IPropertyAccessFlowNode node : binding.getPropertyAccess((IPropertyAccessStatement)stmt)) {
				if(CollectionUtil.intersects(input.lookup(node.getBase(), ObjectValue.class), bases)) {
					if(node instanceof IDynamicPropertyAccessFlowNode) {
						result.add(stmt);
					} else if(node instanceof LoadAndInvokeNode && ((LoadAndInvokeNode)node).getProperty().equals(propertyName)) {
						result.add(stmt);
					} else if(node instanceof LoadNode && ((LoadNode)node).getProperty().equals(propertyName)) {
						result.add(stmt);
					} else if(node instanceof StoreIfPresentNode && ((StoreIfPresentNode)node).getProperty().equals(propertyName)) {
						result.add(stmt);
					} else if(node instanceof StoreNode && ((StoreNode)node).getProperty().equals(propertyName)) {
						result.add(stmt);
					}
				}
			}
		} else if(stmt instanceof InvokeStatement) {
			result.add((InvokeStatement)stmt);
		}
		// TODO: handle IVariableAccessStatements in with-blocks
		return result;
	}

	@Override
	public Set<Statement> leastUpperBound(Set<Statement> a, Set<Statement> b, Block followingBlock) {
        if (a == b || b == null)
		    return a;
		if (a == null)
		    return b;
		return CollectionUtil.union(a, b);
	}

	@Override
	public boolean equal(Set<Statement> a, Set<Statement> b) {
		if(a == null)
			return b == null;
		return a.equals(b);
	}
}
