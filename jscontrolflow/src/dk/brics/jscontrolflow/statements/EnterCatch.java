package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * On top of the existing one, add a new lexical environment with
 * a variable binding for <tt>x</tt>.
 * <p/>
 * This statement has nothing to do with catching exceptions, it
 * is simply named after the JavaScript construct <tt>catch</tt> from 
 * which the statement is created.
 */
public class EnterCatch extends EnterScopeStatement {
    private String varName;

    public EnterCatch(Scope innerScope, String varName) {
        super(innerScope);
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseEnterCatch(this);
    }

    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseEnterCatch(this, arg);
    }

    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.emptySet();
    }

    @Override
    public boolean canThrowException() {
        return false;
    }
}
