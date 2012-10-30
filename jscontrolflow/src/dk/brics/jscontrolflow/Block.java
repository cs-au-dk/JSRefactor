package dk.brics.jscontrolflow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Sequence of statements.
 */
public final class Block {
    private Statement first, last;
    private Set<Block> successors = new HashSet<Block>();
    private Set<Block> predecessors = new HashSet<Block>();
    private Block exceptionHandler;
    private Set<Block> exceptionalPredecessors = new HashSet<Block>();
    private Function function;

    public Function getFunction() {
        return function;
    }
    void setFunction(Function function) {
        this.function = function;
    }

    public Block getExceptionHandler() {
        return exceptionHandler;
    }
    public void setExceptionHandler(Block exceptionHandler) {
        if (this.exceptionHandler != null) {
            this.exceptionHandler.exceptionalPredecessors.remove(this);
        }
        this.exceptionHandler = exceptionHandler;
        if (exceptionHandler != null) {
            exceptionHandler.exceptionalPredecessors.add(this);
        }
    }
    public Statement getFirst() {
        return first;
    }
    public Statement getLast() {
        return last;
    }
    public Collection<Block> getSuccessors() {
        return Collections.unmodifiableCollection(successors);
    }
    public Collection<Block> getPredecessors() {
        return Collections.unmodifiableCollection(predecessors);
    }
    public Collection<Block> getExceptionalPredecessors() {
        return Collections.unmodifiableCollection(exceptionalPredecessors);
    }

    public boolean addSuccessor(Block succ) {
        if (this.successors.add(succ)) {
            succ.predecessors.add(this);
            return true;
        } else {
            return false;
        }
    }
    public boolean removeSuccessor(Block succ) {
        if (this.successors.remove(succ)) {
            succ.predecessors.remove(this);
            return true;
        } else {
            return false;
        }
    }
    public void removeAllSuccessors() {
        for (Block succ : successors) {
            succ.predecessors.remove(this);
        }
        successors.clear();
    }
    public void removeAllPredecessors() {
        for (Block pred : predecessors) {
            pred.successors.remove(this);
        }
        predecessors.clear();
    }

    public boolean isEmpty() {
        return first == null;
    }

    public void addFirst(Statement stm) {
        assert stm.getBlock() == null : "Statement is already in a block";
        stm.setBlock(this);
        if (first != null) {
            stm.setNext(first);
            first.setPrevious(stm);
        }
        first = stm;
        if (last == null) {
            last = stm;
        }
    }
    public void addLast(Statement stm) {
        assert stm.getBlock() == null : "Statement is already in a block";
        stm.setBlock(this);
        if (first == null) {
            first = stm;
        }
        if (last != null) {
            stm.setPrevious(last);
            last.setNext(stm);
        }
        last = stm;
    }
    public void remove(Statement stm) {
        assert stm.getBlock() == this : "Statement is not in this block";
        if (first == stm) {
            first = stm.getNext();
        } else {
            stm.getPrevious().setNext(stm.getNext());
        }
        if (last == stm) {
            last = stm.getPrevious();
        } else {
            stm.getNext().setPrevious(stm.getPrevious());
        }
        stm.setBlock(null);
        stm.setNext(null);
        stm.setPrevious(null);
    }
    public boolean contains(Statement stm) {
        return stm.getBlock() == this;
    }

    void setLast(Statement last) {
        this.last = last;
    }
    void setFirst(Statement first) {
        this.first = first;
    }

    /**
     * Iterates all statements in this block. The iterable is backed by the block.
     * It does not support removal, and it does not check for concurrent modification.
     * <p/>
     * <h2>About concurrent modification:</h2>
     * A statement returned by the iterator (and any of its predecessors) can be removed using {@link Block#remove(Statement)} before
     * the next call to {@link Iterator#next()}. Its successor must not be removed, however.
     * If new statements are inserted immediately after the returned statement, the new statements will <i>not</i> be seen by the iterator.
     */
    public Iterable<Statement> getStatements() {
        return new Iterable<Statement>() {
            @Override
            public Iterator<Statement> iterator() {
                return new Iterator<Statement>() {
                    private Statement next = Block.this.first;
                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }
                    @Override
                    public Statement next() {
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        assert next.getBlock() == Block.this : "Next statement was removed from block"; // make sure next statement has not been removed
                        Statement s = next;
                        next = s.getNext();
                        return s;
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Use Block.remove instead");
                    }
                };
            }
        };
    }

    @Override
    public String toString() {
        if (first == null) {
            return "Block[empty]";
        } else {
            return "Block[" + first.getSerial() + "]";
        }
    }

}
