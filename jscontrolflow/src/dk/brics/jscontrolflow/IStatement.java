package dk.brics.jscontrolflow;

import java.util.Collection;
import java.util.List;

import dk.brics.jscontrolflow.statements.StatementQuestionAnswer;
import dk.brics.jscontrolflow.statements.StatementVisitor;

public interface IStatement {

    public abstract Statement getNext();

    public abstract Statement getPrevious();

    public abstract Block getBlock();

    public abstract void apply(StatementVisitor v);

    public abstract <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg);

    /**
     * Returns the variables assigned to by this statement.
     * The order of the variables in the list must be consistent.
     * @return unmodifiable list
     */
    public abstract List<Integer> getAssignedVariables();

    /**
     * Returns the variables read from by this statement.
     * @return unmodifiable collection
     */
    public abstract Collection<Integer> getReadVariables();

    /**
     * Returns true if this statement might throw an exception during
     * its execution.
     * @return boolean
     */
    public abstract boolean canThrowException();

    public abstract int getSerial();

}