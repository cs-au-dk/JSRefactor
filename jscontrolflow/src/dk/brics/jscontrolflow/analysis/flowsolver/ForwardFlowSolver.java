package dk.brics.jscontrolflow.analysis.flowsolver;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;

/**
 * Solver for an analysis where information is propagated forwards along control-flow edges.
 */
public class ForwardFlowSolver<T> {

    private Comparator<Block> priorityComparator = new Comparator<Block>() {
        @Override
        public int compare(Block b1, Block b2) {
            return block2priority.get(b1).compareTo(block2priority.get(b2));
        }
    };

    private int nextPriority = 1;
    private Map<Block,Integer> block2priority = new HashMap<Block,Integer>();
    private PriorityQueue<Block> queue = new PriorityQueue<Block>(64, priorityComparator);
    private Set<Block> inqueue = new HashSet<Block>();
    private Map<Block,T> block2before = new HashMap<Block,T>();
    private Map<Statement,T> stmt2after = new HashMap<Statement,T>();
    private ForwardFlowAnalysis<T> analysis;

    public ForwardFlowSolver(ForwardFlowAnalysis<T> analysis) {
        this.analysis = analysis;
    }

    private void solvex(Function function) {
        for (Block block : function.getBlocks()) {
            block2before.put(block, analysis.bottom());
        }
        propagate(function.getEntry(), analysis.entry());
        while (!queue.isEmpty()) {
            Block block = queue.remove();
            inqueue.remove(block);
            T value = block2before.get(block);
            for (Statement stm : block.getStatements()) {
                if (stm.canThrowException()) {
                    propagate(block.getExceptionHandler(), value);
                }
                value = analysis.transfer(stm, value);
                stmt2after.put(stm, value);
            }
            for (Block succ : block.getSuccessors()) {
                propagate(succ, value);
            }
        }
    }

    private void propagate(Block block, T value) {
        T existing = block2before.get(block);
        T lub = analysis.leastUpperBound(existing, value, block);
        if (!analysis.equal(lub, existing)) {
            block2before.put(block, lub);
            enqueue(block);
        }
    }

    private void enqueue(Block block) {
        if (!block2priority.containsKey(block)) {
            block2priority.put(block, nextPriority++);
        }
        if (inqueue.add(block)) {
            queue.add(block);
        }
    }

    public static <T> ForwardFlowResult<T> solve(Function function, ForwardFlowAnalysis<T> analysis) {
        ForwardFlowSolver<T> solver = new ForwardFlowSolver<T>(analysis);
        solver.solvex(function);
        return new ForwardFlowResult<T>(solver.block2before, solver.stmt2after);
    }

}
