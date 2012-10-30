package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.PrettyPrinter;

/**
 * AST change describing a new statement being inserted into a script.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class InsertStmtIntoScript extends Change {
	private Start script;
	private int index;
	private PStmt stmt;

	public InsertStmtIntoScript(Start script, int index, PStmt stmt) {
		super();
		this.script = script;
		this.index = index;
		this.stmt = stmt;
	}


	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(script);
	}

	@Override
	public void perform() {
		PrettyPrinter.insertStmtIntoScript(script, index, stmt);
	}


	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseInsertStmtIntoScript(this, arg);
	}

}
