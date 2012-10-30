package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * On top of the existing one,
 * add a new lexical environment with variable bindings corresponding
 * to the properties of ToObject(v<sub>object</sub>).
 * <p/>
 * Throws a TypeError if v<sub>object</sub> is <tt>null</tt> or <tt>undefined</tt>
 * (follows from the semantics of ToObject).
 */
public class EnterWith extends EnterScopeStatement {
    private int objectVar;

    public EnterWith(Scope innerScope, int objectVar) {
        super(innerScope);
        this.objectVar = objectVar;
    }

    public int getObjectVar() {
        return objectVar;
    }
    public void setObjectVar(int objectVar) {
        this.objectVar = objectVar;
    }

    @Override
    public boolean canThrowException() {
        return true; // ToObject throws TypeError for null and undefined
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(objectVar);
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseEnterWith(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseEnterWith(this, arg);
    }
}
