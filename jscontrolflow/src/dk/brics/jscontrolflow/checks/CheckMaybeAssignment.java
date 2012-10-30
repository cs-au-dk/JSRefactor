package dk.brics.jscontrolflow.checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.statements.Phi;

/**
 * Checks that all temporary variables might be assigned to before they are used.
 */
public class CheckMaybeAssignment {
    public static void check(Function function) {
        for (Function inner : function.getTransitiveInnerFunctions(true)) {
            checkBody(inner);
        }
    }
    public static void checkBody(Function function) {
        new CheckMaybeAssignment(function).check();
    }

    private Function function;
    private LinkedList<Block> queue = new LinkedList<Block>();
    private Set<Block> inqueue = new HashSet<Block>();
    private Map<Block,Set<Integer>> beforeBlock = new HashMap<Block, Set<Integer>>();

    public CheckMaybeAssignment(Function function) {
        this.function = function;
    }

    private void check() {
        enqueueBlock(function.getEntry(), new HashSet<Integer>());
        while (!queue.isEmpty()) {
            Block block = queue.removeFirst();
            inqueue.remove(block);
            Set<Integer> mayasn = new HashSet<Integer>(beforeBlock.get(block));
            for (Statement stm : block.getStatements()) {
                if (stm.canThrowException() && block.getExceptionHandler() != null) {
                    enqueueBlock(block.getExceptionHandler(), mayasn);
                }
                mayasn.addAll(stm.getAssignedVariables());
            }
            for (Block succ : block.getSuccessors()) {
                enqueueBlock(succ, mayasn);
            }
        }
        // fixed point found, now check
        for (Block block : function.getBlocks()) {
            Set<Integer> mayasn = beforeBlock.get(block);
            if (mayasn == null)
            {
                continue; // unreachable or exceptional exit
            }
            for (Statement stm : block.getStatements()) {
                if (stm instanceof Phi) {
                    // Phi statements only need one of its arguments to be assigned
                    Phi phi = (Phi)stm;
                    if (!mayasn.contains(phi.getArg1Var()) && !mayasn.contains(phi.getArg2Var())) {
                        throw new RuntimeException(phi + " in " + function + " has both arguments unassigned");
                    }
                } else if (!mayasn.containsAll(stm.getReadVariables())) {
                    Set<Integer> x = new HashSet<Integer>(stm.getReadVariables());
                    x.removeAll(mayasn);
                    throw new RuntimeException(stm + " in " + function + " reads variables before assigned: " + x);
                }
                mayasn.addAll(stm.getAssignedVariables());
            }
        }
    }

    private void enqueueBlock(Block block, Set<Integer> mayasn) {
        Set<Integer> existing = beforeBlock.get(block);
        boolean changed;
        if (existing == null) {
            beforeBlock.put(block, new HashSet<Integer>(mayasn));
            changed = true;
        } else {
            changed = existing.addAll(mayasn);
        }
        if (changed) {
            if (inqueue.add(block)) {
                queue.add(block);
            }
        }
    }

}
