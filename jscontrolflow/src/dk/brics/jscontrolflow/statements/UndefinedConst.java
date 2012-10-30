package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = <tt>undefined</tt>
 */
public class UndefinedConst extends Assignment {
    public UndefinedConst(int resultVar) {
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
        v.caseUndefinedConst(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseUndefinedConst(this, arg);
    }
}
