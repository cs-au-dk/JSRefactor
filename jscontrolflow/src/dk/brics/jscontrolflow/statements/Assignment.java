package dk.brics.jscontrolflow.statements;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.Statement;

/**
 * Statement that assigns to exactly one temporary variable
 * (called v<sub>result</sub> by convention).
 * 
 * @author Asger
 */
public abstract class Assignment extends Statement {
    private int resultVar;

    public Assignment(int resultVar) {
        this.resultVar = resultVar;
    }

    public int getResultVar() {
        return resultVar;
    }
    public void setResultVar(int resultVar) {
        this.resultVar = resultVar;
    }

    public abstract void apply(AssignmentVisitor v);
    public abstract <Q,A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg);

    @Override
    public final void apply(StatementVisitor v) {
        apply((AssignmentVisitor)v);
    }
    @Override
    public final <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return apply((AssignmentQuestionAnswer<Q, A>)v, arg);
    }
    @Override
    public final List<Integer> getAssignedVariables() {
        return Collections.singletonList(resultVar);
    }
}
