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
 * Checks that all temporary variables are definitely assigned to before they are used.
 */
public class CheckDefiniteAssignment {
    public static void check(Function function) {
        for (Function inner : function.getTransitiveInnerFunctions(true)) {
            checkBody(inner);
        }
    }
    public static void checkBody(Function function) {
        new CheckDefiniteAssignment(function).check();
    }

    private Function function;
    private LinkedList<Block> queue = new LinkedList<Block>();
    private Set<Block> inqueue = new HashSet<Block>();
    private Map<Block,Set<Integer>> beforeBlock = new HashMap<Block, Set<Integer>>();

    public CheckDefiniteAssignment(Function function) {
        this.function = function;
    }

    private void check() {
        enqueueBlock(function.getEntry(), new HashSet<Integer>());
        while (!queue.isEmpty()) {
            Block block = queue.removeFirst();
            inqueue.remove(block);
            Set<Integer> defasn = new HashSet<Integer>(beforeBlock.get(block));
            for (Statement stm : block.getStatements()) {
                if (stm.canThrowException() && block.getExceptionHandler() != null) {
                    enqueueBlock(block.getExceptionHandler(), defasn);
                }
                defasn.addAll(stm.getAssignedVariables());
            }
            for (Block succ : block.getSuccessors()) {
                enqueueBlock(succ, defasn);
            }
        }
        // fixed point found, now check
        for (Block block : function.getBlocks()) {
            Set<Integer> defasn = beforeBlock.get(block);
            if (defasn == null)
            {
                continue; // unreachable or exceptional exit
            }
            for (Statement stm : block.getStatements()) {
                if (stm instanceof Phi) {
                    // Phi statements only need one of its arguments to be assigned
                    //Phi phi = (Phi)stm;
                    // TODO: We can't check phi for definite assignment without
                    // a stronger lattice. Eg. pair-based where (x,y) is in L if x or y is defasn
                    //					if (!defasn.contains(phi.getArg1Var()) && !defasn.contains(phi.getArg2Var())) {
                    //						throw new RuntimeException(phi + " in " + function + " has both arguments ");
                    //					}
                } else if (!defasn.containsAll(stm.getReadVariables())) {
                    Set<Integer> x = new HashSet<Integer>(stm.getReadVariables());
                    x.removeAll(defasn);
                    throw new RuntimeException(stm + " in " + function + " reads variables before assigned: " + x);
                }
                defasn.addAll(stm.getAssignedVariables());
            }
        }
    }

    private void enqueueBlock(Block block, Set<Integer> defasn) {
        Set<Integer> existing = beforeBlock.get(block);
        boolean changed;
        if (existing == null) {
            beforeBlock.put(block, new HashSet<Integer>(defasn));
            changed = true;
        } else {
            changed = existing.retainAll(defasn);
        }
        if (changed) {
            if (inqueue.add(block)) {
                queue.add(block);
            }
        }
    }

}
