package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.nodes.AccessWithName;

/**
 * AST change describing name change of a {@link AccessWithName}.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class RenamePropertyNameNode extends Change {
	private AccessWithName node;
	private String newName;

	public RenamePropertyNameNode(AccessWithName node, String newName) {
		this.node = node;
		this.newName = newName;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(node.getNode().getRoot());
	}

	@Override
	public void perform() {
		node.replaceName(newName);
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseRenamePropertyNameNode(this, arg);
	}

}
