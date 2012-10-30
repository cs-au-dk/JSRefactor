package dk.brics.jscontrolflow.analysis.flowsolver;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Statement;

/**
 * Forward flow analysis using an immutable lattice data structure (ie. instances of <tt>T</tt>
 * must not be modified).
 *
 * @param <T> lattice type
 */
public interface ForwardFlowAnalysis<T> {
    /**
     * Bottom of the lattice.
     */
    T bottom();

    /**
     * Lattice point to use at the function entry point.
     */
    T entry();

    /**
     * Transfer function for all statements.
     * @param stmt a statement
     * @param before lattice point before the statement
     * @return a new lattice point or an alias of <tt>before</tt>
     */
    T transfer(Statement stmt, T before);

    /**
     * Computes the least upper bound of two lattice points at the program
     * point before the given block. The arguments should not be modified.
     * @param arg1 a lattice point
     * @param arg2 a lattice point
     * @param followingBlock a block
     * @return a new lattice point or an alias of one of the arguments
     */
    T leastUpperBound(T arg1, T arg2, Block followingBlock);

    /**
     * Returns true if the two arguments represent the same lattice point.
     * @param arg1 a lattice point
     * @param arg2 a lattice point
     * @return
     */
    boolean equal(T arg1, T arg2);
}
