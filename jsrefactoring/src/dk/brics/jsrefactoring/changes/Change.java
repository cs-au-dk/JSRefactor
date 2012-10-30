package dk.brics.jsrefactoring.changes;

import java.util.Set;

import dk.brics.jsparser.node.Start;


/**
 * Refactorings do not directly manipulate the AST; rather, they create
 * a list of change objects that represent the manipulations to be
 * performed. This class is the abstract super class of all AST changes.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public abstract class Change {
	/**
	 * Performs the described AST change.
	 */
	public abstract void perform();
	
	public abstract Set<Start> getAffectedScripts();
	
	public abstract <Q,A> A apply(ChangeVisitor<Q,A> v, Q arg);
}
