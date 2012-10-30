package dk.brics.jscontrolflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.ExceptionalReturn;

/**
 * Represents a JavaScript function or top-level scope. 
 * Contains a graph of blocks, which each contain a sequence of statements.
 * These blocks make up the function's body.
 * <p/>
 * A function has an <i>entry</i> block, which is where execution begins when the function
 * is invoked, and an <i>exceptional exit</i> block, which contains a single {@link ExceptionalReturn}
 * statement unique to that function.
 * <p/>
 * Each block <i>B</i> has an optional <i>exception handler</i> which denotes a block that receives control if a
 * statement in <i>B</i> throws an exception. An exception handler block must begin with a {@link Catch} statement or an {@link ExceptionalReturn}
 * statement. The exceptional exit block should never have an exception handler itself.
 * <p/>
 * Functions have an <i>outer function</i> which refers to the function that lexically encloses it in the source code.
 * Only function objects that represent a top-level scope have no outer function. A function also contains references
 * to its inner functions.
 * <p/>
 * Function objects are also {@link Scope scopes}, and represent the function's own outermost scope.
 * The function's outer function must be an ancestor scope of the function's scope, though it might not
 * be the direct parent.
 * 
 * @author asf
 */
public final class Function extends Scope {
    private Function outerFunction;
    private final Set<Function> innerFunctions = new HashSet<Function>();
    private final Set<Block> blocks = new HashSet<Block>();
    private final Block entry;
    private final Block exceptionalExit;
    private final List<String> parameterNames = new ArrayList<String>();
    private final Set<String> declaredVariables = new HashSet<String>();
    private String name;
    private SourceLocation location;
    private boolean hasExplicitArgumentsDeclaration;

    /**
     * Creates a new function.
     * @param name name of the function, or <tt>null</tt> if the function is anonymous
     * @param location location of the "function" keyword
     * @param parentScope parent scope, or <tt>null</tt> if this is a top-level scope
     */
    public Function(String name, SourceLocation location, Scope parentScope) {
        super(parentScope);
        this.name = name;
        this.location = location;
        entry = new Block();
        addBlock(entry);
        exceptionalExit = new Block();
        exceptionalExit.addFirst(new ExceptionalReturn());
        addBlock(exceptionalExit);
        entry.setExceptionHandler(exceptionalExit);
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }
    
    /** Returns the top-level function containing this function */
    public Function getTopLevelFunction() {
        Function f = this;
        while (f.outerFunction != null) {
          f = f.outerFunction;
        }
        return f;
    }
    public Function getOuterFunction() {
        return outerFunction;
    }
    public Set<Function> getInnerFunctions() {
        return Collections.unmodifiableSet(innerFunctions);
    }
    public Set<Block> getBlocks() {
        return Collections.unmodifiableSet(blocks);
    }

    public void addInnerFunction(Function f) {
        if (f.outerFunction == null) {
            f.outerFunction = this;
            this.innerFunctions.add(f);
        } else {
            throw new IllegalArgumentException("Argument already has an outer function");
        }
    }
    public void removeInnerFunction(Function f) {
        if (f.outerFunction == this) {
            f.outerFunction = null;
            this.innerFunctions.remove(f);
        } else {
            throw new IllegalArgumentException("Argument is not an inner function of this function");
        }
    }
    public boolean containsInnerFunction(Function f) {
        return f.getOuterFunction() == this;
    }

    public Block getEntry() {
        return entry;
    }

    /**
     * A block containing only an ExceptionalReturn statement.
     * This block has no exception handler.
     * @return a block in the function
     */
    public Block getExceptionalExit() {
        return exceptionalExit;
    }

    public void addBlock(Block block) {
        if (block.getFunction() == null) {
            block.setFunction(this);
            blocks.add(block);
        } else {
            throw new IllegalArgumentException("Block is already in a function");
        }
    }
    /**
     * Removes a block from this function. Note that the block may still have 
     * successors and predecessors and an exception handler inside this function 
     * after being removed.
     * @param block a block
     */
    public void removeBlock(Block block) {
        assert block != entry : "Cannot remove the entry block";
        assert block != exceptionalExit : "Cannot remove the exceptional exit block";
        if (block.getFunction() == this) {
            block.setFunction(null);
            blocks.remove(block);
        } else {
            throw new IllegalArgumentException("Block is not in this function");
        }
    }
    public boolean containsBlock(Block block) {
        return block.getFunction() == this;
    }

    /**
     * Computes the set of functions transitively contained in this function.
     * @param includeSelf if true, this function will also be in the resulting set
     * @return a newly created (and modifiable) set
     */
    public Set<Function> getTransitiveInnerFunctions(final boolean includeSelf) {
        Set<Function> result = new HashSet<Function>();
        if (includeSelf) {
            result.add(this);
        }
        for (Function inner : innerFunctions) {
            inner.collectInnerFunctions(result);
        }
        return result;
    }
    private void collectInnerFunctions(Set<Function> dest) {
        dest.add(this);
        for (Function inner : innerFunctions) {
            inner.collectInnerFunctions(dest);
        }
    }

    @Override
    public Set<String> getDeclaredVariables() {
        return declaredVariables;
    }

    /**
     * Returns the name of the function, or <tt>null</tt> if the function
     * is anonymous. Examples:
     * <pre>
     * function Foo() {} // getName returns "Foo"
     * var x = function Foo() {}; // getName returns "Foo"
     * var x = function() {}; // getName returns null
     * </pre>
     * @return a string, or <tt>null</tt>
     */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the location of the "function" keyword used to
     * instantiate this function, or the very beginning of the
     * source code if this is a top-level scope.
     */
    public SourceLocation getSourceLocation() {
        return location;
    }
    public void setSourceLocation(SourceLocation location) {
        this.location = location;
    }

    /**
     * If true, the function explicitly declares a variable named "arguments" either
     * as a parameter, with a <tt>var</tt> declaration, or with a function declaration.
     * In this case, it works as a normal variable.
     * <p/>
     * If false, the function declares its "arguments" variable implicitly, and it will
     * be initialized to an <i>arguments array</i> when the function is invoked.
     */
    public boolean hasExplicitArgumentsDeclaration() {
        return hasExplicitArgumentsDeclaration;
    }
    public void setHasExplicitArgumentsDeclaration(boolean hasExplicitArgumentsDeclaration) {
        this.hasExplicitArgumentsDeclaration = hasExplicitArgumentsDeclaration;
    }
    
    /**
     * Returns the unique assignment assigning to <tt>var</tt>, or <tt>null</tt>
     * if no such assignment is in the function body.
     * <p/>
     * This method is not efficient at all, as it simply searches through all statements
     * in the body.
     * @param var a temporary variable
     * @return a statement or <tt>null</tt>
     */
    public Assignment findAssignmentOfVariable(int var) {
        for (Block block : getBlocks()) {
            for (Statement stm : block.getStatements()) {
                if (stm instanceof Assignment) {
                    Assignment asn = (Assignment) stm;
                    if (asn.getResultVar() == var)
                        return asn;
                }
            }
        }
        return null;
    }

    private static int nextSerial = 1;
    private int serial = nextSerial++;

    public int getSerial() {
        return serial;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else if (outerFunction == null) {
            return "<main " + location + ">";
        } else {
            return "<anon " + location + ">";
        }
    }
    
    @Override
    public boolean isGlobal() {
    	return outerFunction == null;
    }
}
