package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


/**
 * v<sub>result</sub> = caught exception.
 * <p/>
 * Must be first in its block.
 */
public class Catch extends Assignment {
    public Catch(int resultVar) {
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
        v.caseCatch(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseCatch(this, arg);
    }
}
