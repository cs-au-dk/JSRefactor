package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * <tt>return</tt> v<sub>arg</sub>
 * 
 * @see ReturnVoid
 */
public class Return extends NonAssignment {
    private int argVar;

    public Return(int argVar) {
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
        return false;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(argVar);
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseReturn(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseReturn(this, arg);
    }
}
