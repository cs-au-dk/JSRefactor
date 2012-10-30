package dk.brics.jscontrolflow.statements;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * Adds a new lexical environment on top of the existing one.
 */
public abstract class EnterScopeStatement extends NonAssignment {
    private Scope innerScope;

    public EnterScopeStatement(Scope innerScope) {
        this.innerScope = innerScope;
    }

    public Scope getInnerScope() {
        return innerScope;
    }

    public void setInnerScope(Scope innerScope) {
        this.innerScope = innerScope;
    }
}
