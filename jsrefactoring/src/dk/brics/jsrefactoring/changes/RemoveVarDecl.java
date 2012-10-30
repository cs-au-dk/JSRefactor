package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.PrettyPrinter;

public class RemoveVarDecl extends Change {
	private final AVarDecl vardecl;

	public RemoveVarDecl(AVarDecl vardecl) {
		this.vardecl = vardecl;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(vardecl.getRoot());
	}

	@Override
	public void perform() {
		PrettyPrinter.removeVarDecl(vardecl);
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseRemoveVarDecl(this, arg);
	}

}
