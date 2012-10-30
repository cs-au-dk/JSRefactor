package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;

/**
 * AST change describing one expression being replaced by another.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class ReplaceExp extends Change {
	private PExp oldexp, newexp;

	public ReplaceExp(PExp oldexp, PExp newexp) {
		this.oldexp = oldexp;
		this.newexp = newexp;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(oldexp.getRoot());
	}

	@Override
	public void perform() {
		AstUtil.replaceNode(oldexp, newexp);
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseReplaceExp(this, arg);
	}
}
