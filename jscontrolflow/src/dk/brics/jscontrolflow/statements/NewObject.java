package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


public class NewObject extends Assignment {
    public NewObject(int resultVar) {
        super(resultVar);
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
    public void apply(AssignmentVisitor v) {
        v.caseNewObject(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNewObject(this, arg);
    }
}
