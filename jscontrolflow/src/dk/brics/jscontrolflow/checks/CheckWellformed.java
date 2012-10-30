package dk.brics.jscontrolflow.checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.scope.CatchScope;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeclareVariable;
import dk.brics.jscontrolflow.statements.EnterCatch;
import dk.brics.jscontrolflow.statements.EnterScopeStatement;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.ExceptionalReturn;
import dk.brics.jscontrolflow.statements.LeaveScope;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jscontrolflow.transforms.SimplifyGraph;

/**
 * Checks if a control-flow graph is <i>well-formed</i>. Only well-formed control-flow graphs
 * have well-defined semantics. This check is mostly for debugging, as production code
 * should <i>always</i> produce well-formed control-flow graphs.
 * <p/>
 * The definition of <i>well-formedness</i> is listed below, but note that we only include the
 * constraints that are not automatically enforced by Java's type system or runtime checks performed
 * by {@link Function}, {@link Block}, and {@link Statement}. The well-formedness constraints
 * are typically impractical to enforce on-the-fly.
 * <p/>
 * <b>Well-formedness constraints</b><br/>
 * The following must hold for every function <i>F</i>:
 * <ul>
 * <li>Every temporary variable has at most one assignment.
 * <li>No statement assigns to and reads from the same variable.
 * <li>The exception handler for a block must begin with {@link Catch} or {@link ExceptionalReturn}.
 * <li>{@link Catch} must only occur as the first statement in its block.
 * <li>There is exactly one {@link ExceptionalReturn} statement in the function, and this statement
 * 	   is also the only statement in the function's <i>exceptional return</i> block.
 * <li>Every block containing a statement whose {@link Statement#canThrowException() canThrowException} 
 * 	   method returns <tt>true</tt> must have an exception handler.
 * <li>Every block in the function (as by {@link Function#getBlocks()}) has only successors and predecessors
 * 	   that are in the same function. (Ie. you can't jump between functions)
 * <li>No {@link CreateFunction} statement refers to a function that is not inner function of <i>F</i>.
 * <li>Scope-sensitive statements are consistent with the occurence of scope-modifying statements.
 *     That is, for every path from the entry to a statement <i>S</i>, if one computes the scope at the
 *     end of the path by considering the scope-modifying statements on the path, the resulting scope
 *     equals the scope before <i>S</i>.
 *   <ul>
 *   <li>The scope before a {@link ReadVariable} or {@link WriteVariable} statement is returned by <tt>getScope</tt>
 *   <li>The scope before a {@link EnterWith} or {@link EnterCatch} is the parent of the scope returned by <tt>getInnerScope</tt>
 *   <li>Other statements are not scope-sensitive
 *   </ul> 
 * <li>The contract of {@link Function#getDeclaredVariables()} is satisfied for <i>F</i>.
 * <li>No temporary variables are live before a {@link Catch} or {@link ExceptionalReturn} statement (that is, temporary variables
 * 		lose their value if an exception is thrown).
 * </ul>
 * <p/>
 * <b>Unusual things that may still occur in well-formed CFGs</b>
 * <ul>
 * <li>There may be empty blocks. Empty blocks that are not the entry can be removed by {@link SimplifyGraph graph simplification}.
 * <li>A block does not need to have a successor, even if it does not end in <tt>return</tt> or <tt>throw</tt>.
 *     The code <tt>for(;;){}</tt> can result in such a graph after graph simplification.
 * </ul>
 */
public class CheckWellformed {

    private Function function;

    public CheckWellformed(Function function) {
        this.function = function;
    }

    /**
     * Checks that the body of the given function is well-formed. The function itself
     * is only well-formed if all its transitive inner functions are also well-formed.
     * @param func a function whose body should be checked
     * @throws RuntimeException if the body is not well-formed
     */
    public static void checkBody(Function func) {
        new CheckWellformed(func).checkAll();
    }

    /**
     * Checks that the given function and all its transitive inner functions satisfy
     * the well-formedness constraints.
     * @param func a function (typically the top-level function)
     */
    public static void check(Function func) {
        for (Function inner : func.getTransitiveInnerFunctions(true)) {
            checkBody(inner);
        }
    }

    private void checkAll() {
        checkExceptionHandlers();
        checkScopes();
        checkVariables();
        checkAssignAndReadFromSameVariable();
        checkUniqueExceptionalReturn();
        checkSuccessorsAndPredecessors();
        checkCreateFunctions();
        checkDeclaredVariables();
    }

    private void checkDeclaredVariables() {
        Set<String> declared = new HashSet<String>();
        for (Block block : function.getBlocks()) {
            for (Statement stm : block.getStatements()) {
                if (stm instanceof DeclareVariable) {
                    DeclareVariable decl = (DeclareVariable) stm;
                    declared.add(decl.getVarName());
                }
            }
        }
        if (!declared.equals(function.getDeclaredVariables())) {
            throw new RuntimeException(function + " declares " + function.getDeclaredVariables() + " but contains declaration statements for " + declared);
        }
    }

    private void checkCreateFunctions() {
        for (Block block : function.getBlocks()) {
            for (Statement stm : block.getStatements()) {
                if (stm instanceof CreateFunction) {
                    CreateFunction create = (CreateFunction) stm;
                    if (create.getFunction().getOuterFunction() != function) {
                        throw new RuntimeException("CreateFunction with non-inner function");
                    }
                }
            }
        }
    }

    private void checkSuccessorsAndPredecessors() {
        for (Block block : function.getBlocks()) {
            for (Block pred : block.getPredecessors()) {
                if (pred.getFunction() != function) {
                    throw new RuntimeException("Predecessor block not in same function");
                }
            }
            for (Block succ : block.getSuccessors()) {
                if (succ.getFunction() != function) {
                    throw new RuntimeException("Successor block not in same function");
                }
            }
            for (Block pred : block.getExceptionalPredecessors()) {
                if (pred.getFunction() != function) {
                    throw new RuntimeException("Exceptional predecessor block not in same function");
                }
            }
            if (block.getExceptionHandler() != null) {
                if (block.getExceptionHandler().getFunction() != function) {
                    throw new RuntimeException("Exception handler block not in same function");
                }
            }
        }
    }

    private void checkUniqueExceptionalReturn() {
        if (function.getExceptionalExit().isEmpty()) {
            throw new RuntimeException("Empty exceptional exit block");
        }
        if (function.getExceptionalExit().getFirst() != function.getExceptionalExit().getLast()) {
            throw new RuntimeException("Exceptional exit block has more than one statement");
        }
        if (!(function.getExceptionalExit().getFirst() instanceof ExceptionalReturn)) {
            throw new RuntimeException("Exceptional exit block does not have ExceptionalReturn statement");
        }
        for (Block block : function.getBlocks()) {
            if (block == function.getExceptionalExit()) {
                continue;
            }
            for (Statement stm : block.getStatements()) {
                if (stm instanceof ExceptionalReturn) {
                    throw new RuntimeException("Non-unique ExceptionalReturn statement");
                }
            }
        }
    }

    private void checkAssignAndReadFromSameVariable() {
        for (Block block : function.getBlocks()) {
            for (Statement stm : block.getStatements()) {
                for (int x : stm.getReadVariables()) {
                    if (stm.getAssignedVariables().contains(x)) {
                        throw new RuntimeException("Statement assigns to and reads from " + x);
                    }
                }
            }
        }
    }

    private void checkExceptionHandlers() {
        for (Block block : function.getBlocks()) {
            if (block.getExceptionHandler() == null) {
                // check that no statement in the block may throw an exception
                for (Statement stm : block.getStatements()) {
                    if (stm.canThrowException()) {
                        throw new RuntimeException("Statement can throw exceptions, but its block has no exception handler");
                    }
                }
            } else {
                // check that the exception handler starts with Catch or ExceptionalReturn
                Statement first = block.getExceptionHandler().getFirst();
                if (!(first instanceof Catch || first instanceof ExceptionalReturn)) {
                    throw new RuntimeException("Exception handler starts with statement of type " + first.getClass());
                }
            }
            for (Statement stm : block.getStatements()) {
                if (stm != block.getFirst() && stm instanceof Catch) {
                    throw new RuntimeException("Catch statement is not the first in its block");
                }
            }
        }
    }

    private void checkVariables() {
        Set<Integer> assignedVars = new HashSet<Integer>();
        for (Block block : function.getBlocks()) {
            for (Statement stm : block.getStatements()) {
                if (stm instanceof Assignment) {
                    Assignment asn = (Assignment) stm;
                    int var = asn.getResultVar();
                    if (!assignedVars.add(var)) {
                        throw new RuntimeException("The variable " + var + " is assigned to more than once");
                    }
                }
            }
        }
    }

    private void checkScopes() {
        Map<Block,Scope> block2scope = new HashMap<Block,Scope>();
        LinkedList<Block> queue = new LinkedList<Block>();
        Set<Block> inqueue = new HashSet<Block>();
        block2scope.put(function.getEntry(), function);
        block2scope.put(function.getExceptionalExit(), function);
        queue.add(function.getEntry());
        inqueue.add(function.getEntry());
        while (!queue.isEmpty()) {
            Block block = queue.removeFirst();
            inqueue.remove(block);
            Scope scope = block2scope.get(block);
            for (Statement stm : block.getStatements()) {
                if (stm.canThrowException()) {
                    enqueueBlock(block.getExceptionHandler(), scope, block2scope, queue, inqueue);
                }
                if (stm instanceof EnterScopeStatement) {
                    EnterScopeStatement enter = (EnterScopeStatement)stm;
                    if (scope != enter.getInnerScope().getParentScope()) {
                        throw new RuntimeException(stm + " has wrong inner scope");
                    }
                    scope = enter.getInnerScope();
                } else if (stm instanceof LeaveScope) {
                    if (scope instanceof WithScope || scope instanceof CatchScope) {
                        scope = scope.getParentScope();
                    } else {
                        // cannot leave FunctionScope
                        throw new RuntimeException("Leave-scope reachable without matching enter-with or enter-catch");
                    }
                } else if (stm instanceof ReadVariable) {
                    ReadVariable read = (ReadVariable)stm;
                    if (scope != read.getScope()) {
                        throw new RuntimeException(read + " has wrong scope");
                    }
                } else if (stm instanceof WriteVariable) {
                    WriteVariable write = (WriteVariable)stm;
                    if (scope != write.getScope()) {
                        throw new RuntimeException(write + " has wrong scope");
                    }
                }
            }
            for (Block succ : block.getSuccessors()) {
                enqueueBlock(succ, scope, block2scope, queue, inqueue);
            }
        }
    }

    private void enqueueBlock(Block succ, Scope scope,
            Map<Block, Scope> block2scope, LinkedList<Block> queue,
            Set<Block> inqueue) {
        Scope existing = block2scope.put(succ, scope);
        if (existing != null && existing != scope) {
            throw new RuntimeException(succ + " in " + function + " is reachable from multiple scopes");
        }
        if (existing == null) { // only add to worklist if scope changed
            if (inqueue.add(succ)) {
                queue.add(succ);
            }
        }
    }

}
