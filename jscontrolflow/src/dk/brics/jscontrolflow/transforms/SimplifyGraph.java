package dk.brics.jscontrolflow.transforms;

import java.util.LinkedList;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.ast2cfg.AstBinding;

/**
 * Removes most empty blocks from a control-flow graph while preserving its semantics.
 * The only block that may still be empty after simplification is the entry block.
 * <p/>
 * <b>Note:</b> This is not compatible with the {@link AstBinding} class, which may have references
 * to some statements that get removed by this operation.
 */
public class SimplifyGraph {
    /**
     * Simplifies the body of the given function. Does not affect its inner functions.
     * @param function a function
     */
    public static void simplifyBody(Function function) {
        LinkedList<Block> emptyBlocks = new LinkedList<Block>();
        for (Block block : function.getBlocks()) {
            if (block != function.getEntry() && block.isEmpty()) {
                // shortcut around the empty block
                for (Block pred : block.getPredecessors()) {
                    if (pred == block) {
                        continue;
                    }
                    for (Block succ : block.getSuccessors()) {
                        if (succ == block) {
                            continue;
                        }
                        pred.addSuccessor(succ);
                    }
                }
                // prepare to remove the block
                emptyBlocks.add(block);
                block.removeAllPredecessors();
                block.removeAllSuccessors();
                block.setExceptionHandler(null); // just for good housekeeping
            }
        }
        for (Block empty : emptyBlocks) {
            // removed blocks here to avoid concurrent modification
            function.removeBlock(empty);
        }
    }

    /**
     * Simplifies the given function and all functions transitively contained inside it.
     * @param function a function (typically the top-level function)
     * @see Function#getTransitiveInnerFunctions(boolean)
     */
    public static void simplifyAll(Function function) {
        for (Function func : function.getTransitiveInnerFunctions(true)) {
            simplifyBody(func);
        }
    }
}
