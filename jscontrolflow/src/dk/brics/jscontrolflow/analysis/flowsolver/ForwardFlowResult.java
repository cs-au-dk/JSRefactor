package dk.brics.jscontrolflow.analysis.flowsolver;

import java.util.HashMap;
import java.util.Map;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Statement;

/**
 * The results of a {@link ForwardFlowAnalysis}.
 */
public class ForwardFlowResult<T> {
    private Map<Block,T> block2before = new HashMap<Block,T>();
    private Map<Statement,T> stmt2after = new HashMap<Statement,T>();

    public ForwardFlowResult(Map<Block, T> block2before, Map<Statement, T> stmt2after) {
        this.block2before = block2before;
        this.stmt2after = stmt2after;
    }

    /**
     * Returns the lattice point before the given statement.
     * @param stmt a statement in the analyzed function
     * @return a lattice point. Only null if the statement was not in the analyzed function, or if the bottom element is null.
     */
    public final T getBefore(Statement stmt) {
        if (stmt.getPrevious() == null) {
            return block2before.get(stmt.getBlock());
        } else {
            return getAfter(stmt.getPrevious());
        }
    }

    /**
     * Returns the lattice point after the given statement.
     * @param stmt a statement in the analyzed function
     * @return a lattice point. Only null if the statement was not in the analyzed function, or if the bottom element is null.
     */
    public final T getAfter(Statement stmt) {
        T t = stmt2after.get(stmt);
        if (t == null)
        {
            return block2before.get(stmt.getBlock()); // returns bottom, since unreachable blocks map to bottom
        }
        return t;
    }
}
