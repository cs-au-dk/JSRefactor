package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


public class ExceptionalReturn extends NonAssignment {
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
        v.caseExceptionalReturn(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseExceptionalReturn(this, arg);
    }
}
