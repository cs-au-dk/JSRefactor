package dk.brics.jscontrolflow;

import java.util.Collection;
import java.util.List;

import dk.brics.jscontrolflow.statements.Assertion;
import dk.brics.jscontrolflow.statements.Nop;
import dk.brics.jscontrolflow.statements.StatementQuestionAnswer;
import dk.brics.jscontrolflow.statements.StatementVisitor;

/**
 * A statement in the control-flow graph. Each non-abstract subclass represents an operation
 * with concrete semantics. The concrete semantics are documented individually for each
 * statement.
 * <p/>
 * The semantics for a statement typically involve modifying a temporary variable, modifying
 * the lexical environment, or modifying the heap. Notable exceptions are {@link Assertion}
 * and {@link Nop}, which have no effects.
 * <p/>
 * All statements in the control-flow graph belong to a block, in which they are arranged in a
 * doubly linked list. Invariants such as
 * <pre>
 *  stm.getNext() == null || stm.getNext().getPrevious() == stm
 *  stm.getNext() == null || stm.getBlock() == stm.getNext().getBlock()
 * </pre>
 * are maintained by {@link Statement} and {@link Block}.
 * <p/>
 * By convention, abstract subclasses of <tt>Statement</tt> either have an <tt>Assignment</tt>
 * or <tt>Statement</tt> suffix in their name, while concrete subclasses do not.
 * 
 * @see Function
 */
public abstract class Statement implements IStatement {
    private Block block;
    private Statement next, previous;

    @Override
    public final Statement getNext() {
        return next;
    }
    @Override
    public final Statement getPrevious() {
        return previous;
    }
    final void setNext(Statement next) {
        this.next = next;
    }
    final void setPrevious(Statement previous) {
        this.previous = previous;
    }

    @Override
    public final Block getBlock() {
        return block;
    }
    final void setBlock(Block block) {
        this.block = block;
    }

    /**
     * Adds a new statement after this statement. If this statement was just returned by an iterator
     * created by {@link Block#getStatements()}, the appended statement will not be seen by that iterator.
     * @param newStm a statement not in any block
     */
    public final void appendStatement(Statement newStm) {
        assert block != null : "This statement must be in a block";
        assert newStm.block == null : "Inserted statement is already in a block";
        newStm.block = this.block;
        newStm.previous = this;
        newStm.next = this.next;
        if (block.getLast() == this) {
            block.setLast(newStm);
        } else {
            this.next.previous = newStm;
        }
        this.next = newStm;
    }

    /**
     * Adds a new statement before this statement.
     * @param newStm a statement not in any block
     */
    public final void prependStatement(Statement newStm) {
        assert block != null : "This statement must be in a block";
        assert newStm.block == null : "Inserted statement is already in a block";
        newStm.block = this.block;
        newStm.next = this;
        newStm.previous = this.previous;
        if (block.getFirst() == this) {
            block.setFirst(newStm);
        } else {
            this.previous.next = newStm;
        }
        this.previous = newStm;
    }

    @Override
    public abstract void apply(StatementVisitor v);
    @Override
    public abstract <Q,A> A apply(StatementQuestionAnswer<Q,A> v, Q arg);

    /**
     * Returns the variables assigned to by this statement. This is either a singleton or an empty list.
     * @return unmodifiable list
     */
    @Override
    public abstract List<Integer> getAssignedVariables();

    /**
     * Returns the variables read from by this statement.
     * @return unmodifiable collection
     */
    @Override
    public abstract Collection<Integer> getReadVariables();

    /**
     * Returns true if this statement might throw an exception during
     * its execution.
     * @return boolean
     */
    @Override
    public abstract boolean canThrowException();

    private int serial = nextSerial++;
    private static int nextSerial = 1;
    @Override
    public int getSerial() {
        return serial;
    }

    @Override
    public String toString() {
        // fallback toString for classes that don't implement it
        return getSerial() + " " + getClass().getSimpleName();
    }
}
