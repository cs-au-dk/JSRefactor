package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


/**
 * Return from function, without a specified return value.
 * <tt>undefined</tt> is the value returned by the function.
 * <p/>
 * An <i>implicit</i> return void statement will be inserted at the end of a function body.
 * Use {@link #isImplicit()} to determine if a return void is such an implicit return
 * statement (it has no semantic significance).
 * Note that top-level functions may also have an implicit return statement, even
 * though a return statement would be prohibited in that context.
 */
public class ReturnVoid extends NonAssignment {
    private boolean implicit;

    public ReturnVoid(boolean implicit) {
        this.implicit = implicit;
    }

    public boolean isImplicit() {
        return implicit;
    }
    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    @Override
    public boolean canThrowException() {
        return false;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseReturnVoid(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseReturnVoid(this, arg);
    }
}
