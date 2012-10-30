package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * Restores the previous lexical environment.
 * Used to terminate the lexical environment created by
 * {@link EnterWith} or {@link EnterCatch}.
 */
public class LeaveScope extends NonAssignment {

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
        v.caseLeaveScope(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseLeaveScope(this, arg);
    }
}
