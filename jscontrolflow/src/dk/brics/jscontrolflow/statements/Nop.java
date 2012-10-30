package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


public class Nop extends NonAssignment {
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
        v.caseNop(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNop(this, arg);
    }
}
