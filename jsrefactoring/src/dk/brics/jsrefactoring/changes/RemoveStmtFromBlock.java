package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.PrettyPrinter;

/**
 * AST change describing a statement being removed from a block.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class RemoveStmtFromBlock extends Change {
	private ABlock block;
	private int index;

	public RemoveStmtFromBlock(ABlock block, int index) {
		super();
		this.block = block;
		this.index = index;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(block.getRoot());
	}

	@Override
	public void perform() {
		PrettyPrinter.removeStmtFromBlock(block, index);
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseRemoveStmtFromBlock(this, arg);
	}
}
