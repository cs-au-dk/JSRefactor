package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = <i>S</i>, where <i>S</i> is a known string constant.
 */
public class StringConst extends Assignment {
    private String string;

    public StringConst(int resultVar, String string) {
        super(resultVar);
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
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
        v.caseStringConst(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseStringConst(this, arg);
    }
}
