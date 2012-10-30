package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = <i>X</i>, where <i>X</i> is a known number value.
 */
public class NumberConst extends Assignment {
    private double number;

    public NumberConst(int resultVar, double number) {
        super(resultVar);
        this.number = number;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
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
        v.caseNumberConst(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNumberConst(this, arg);
    }
}
