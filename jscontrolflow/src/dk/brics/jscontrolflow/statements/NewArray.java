package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


/**
 * v<sub>result</sub> = new array object with length <i>L</i>.
 * 
 * @author Asger
 */
public class NewArray extends Assignment {
    private int length;

    public NewArray(int resultVar, int length) {
        super(resultVar);
        this.length = length;
    }

    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public boolean canThrowException() {
        return false; // no variables that can have funny values
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseNewArray(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNewArray(this, arg);
    }
}
