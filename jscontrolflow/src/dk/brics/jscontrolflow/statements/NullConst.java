package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = <tt>null</tt>
 */
public class NullConst extends Assignment {
    public NullConst(int resultVar) {
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
        v.caseNullConst(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNullConst(this, arg);
    }
}
