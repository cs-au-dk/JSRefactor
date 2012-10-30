package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.node.Token;
import dk.brics.jsrefactoring.PrettyPrinter;

/**
 * AST change describing a new expression being inserted into an argument list.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class InsertExpIntoArglist extends Change {
	private Token start;	// the left parenthesis token starting the argument list
	private LinkedList<PExp> args;
	private Token end;		// the right parenthesis token ending the argument list
	private int index;
	private PExp exp;

	public InsertExpIntoArglist(Token start, LinkedList<PExp> args, Token end,	int index, PExp exp) {
		this.start = start;
		this.args = args;
		this.end = end;
		this.index = index;
		this.exp = exp;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(exp.getRoot());
	}

	@Override
	public void perform() {
		PrettyPrinter.insertExpIntoArglist(start, args, end, index, exp);
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseInsertExpIntoArglist(this, arg);
	}
}
