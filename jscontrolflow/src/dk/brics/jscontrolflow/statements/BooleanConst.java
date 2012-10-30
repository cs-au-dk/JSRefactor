package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

public class BooleanConst extends Assignment {
    private boolean value;

    public BooleanConst(int resultVar, boolean value) {
        super(resultVar);
        this.value = value;
    }
    public boolean getValue() {
        return value;
    }
    public void setValue(boolean value) {
        this.value = value;
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
        v.caseBooleanConst(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseBooleanConst(this, arg);
    }
}
