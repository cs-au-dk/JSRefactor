package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * Assert that ToBoolean(v<sub>arg</sub>) = <i>B</i>, where
 * <i>B</i> is a known boolean.
 * <p/>
 * This type of statement is inserted to preserve information from
 * conditions in if-statements and other control structures.
 */
public class Assertion extends NonAssignment {
    private int argVar;
    private boolean value;

    public Assertion(int argVar, boolean value) {
        this.argVar = argVar;
        this.value = value;
    }

    public int getArgVar() {
        return argVar;
    }
    public void setArgVar(int argVar) {
        this.argVar = argVar;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseAssertion(this);
    }

    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseAssertion(this, arg);
    }

    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(argVar);
    }

    @Override
    public boolean canThrowException() {
        return false;
    }
}
