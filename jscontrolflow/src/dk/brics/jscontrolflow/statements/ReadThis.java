package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = <tt>this</tt>
 */
public class ReadThis extends Assignment {

    public ReadThis(int resultVar) {
        super(resultVar);
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseReadThis(this);
    }

    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseReadThis(this, arg);
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
