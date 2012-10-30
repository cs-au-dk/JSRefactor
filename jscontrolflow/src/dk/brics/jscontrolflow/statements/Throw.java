package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * <tt>throw</tt> v<sub>arg</sub>
 * <p/>
 * Note that v<sub>arg</sub> does not need to be an object - it is perfectly
 * legal to throw numbers, strings, and even <tt>null</tt> and <tt>undefined</tt>.
 */
public class Throw extends NonAssignment {
    private int argVar;

    public Throw(int argVar) {
        this.argVar = argVar;
    }

    public int getArgVar() {
        return argVar;
    }

    public void setArgVar(int argVar) {
        this.argVar = argVar;
    }
    @Override
    public boolean canThrowException() {
        return true;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(argVar);
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseThrow(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseThrow(this, arg);
    }
}
