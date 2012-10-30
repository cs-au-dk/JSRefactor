package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.PrettyPrinter;

/**
 * AST change describing a new statement being inserted into a block.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class InsertStmtIntoBlock extends Change {
	private ABlock block;
	private int index;
	private PStmt stmt;
	

	public InsertStmtIntoBlock(ABlock block, int index, PStmt stmt) {
		super();
		this.block = block;
		this.index = index;
		this.stmt = stmt;
	}


	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(block.getRoot());
	}

	@Override
	public void perform() {
		PrettyPrinter.insertStmtIntoBlock(block, index, stmt);
	}


	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseInsertStmtIntoBlock(this, arg);
	}

}
