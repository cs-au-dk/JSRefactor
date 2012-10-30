package dk.brics.jscontrolflow.statements;

import java.util.Arrays;
import java.util.Collection;

/**
 * v<sub>result</sub> = the value of either v<sub>arg1</sub> or v<sub>arg2</sub>.
 * 
 * @author Asger
 */
public class Phi extends Assignment {
    private int arg1Var;
    private int arg2Var;

    public Phi(int resultVar, int arg1Var, int arg2Var) {
        super(resultVar);
        this.arg1Var = arg1Var;
        this.arg2Var = arg2Var;
    }

    public int getArg1Var() {
        return arg1Var;
    }
    public void setArg1Var(int arg1Var) {
        this.arg1Var = arg1Var;
    }
    public int getArg2Var() {
        return arg2Var;
    }
    public void setArg2Var(int arg2Var) {
        this.arg2Var = arg2Var;
    }
    @Override
    public boolean canThrowException() {
        return false;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Arrays.asList(arg1Var, arg2Var);
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.casePhi(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.casePhi(this, arg);
    }
}
