package dk.brics.jscontrolflow.scope;

import java.util.Set;

import dk.brics.jscontrolflow.Function;

/**
 * A scope is an entity that can hold variable bindings in JavaScript.  
 * 
 * @author asf
 */
public abstract class Scope {
    private Scope parentScope;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    /**
     * Set of variables that are <i>declared</i> in this scope.
     * Specifically:
     * <ul>
     * <li>For a function scope, this includes parameters, variables declared
     * by <tt>var</tt> statements, and variables declared by function declarations.
     * <li>For a <tt>catch (x) {..}</tt> scope, this is the singleton set <tt>{x}</tt>.
     * <li>For a <tt>with</tt> scope, this is the empty set. 
     * </ul>
     * Note that the implicit "arguments" variable is not included.
     * 
     * @return if this is a {@link Function} then a modifiable set, otherwise an unmodifiable set
     */
    public abstract Set<String> getDeclaredVariables();

    public final Scope getParentScope() {
        return parentScope;
    }
    public final void setParentScope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    /**
     * Returns the closest ancestor of this scope of the given type, or <tt>null</tt>
     * if no such ancestor exists. Note that a scope is considered an ancestor of itself.
     * @param <T> type of the ancestor to get
     * @param type class of the ancestor to get
     * @return an ancestor of this scope (might be itself)
     */
    public final <T extends Scope> T getAncestorScope(Class<T> type) {
        Scope scope = this;
        while (scope != null && !type.isInstance(scope)) {
            scope = scope.getParentScope();
        }
        return type.cast(scope);
    }
    
    /**
     * Returns the closest ancestor of this scope that is a function (possibly itself).
     * Never returns <tt>null</tt> for wellformed scope chains (as created by Ast2Cfg),
     * since the toplevel is considered a function scope.
     * @return an ancestor function of this scope (might be itself)
     */
    public final Function getAncestorFunction() {
      return getAncestorScope(Function.class);
    }
    
    /**
     * Determines whether this scope is (reflexively, transitively) nested within the
     * given scope.
     */
    public final boolean hasAncestorScope(Scope that) {
    	Scope scope = this;
    	while(scope != null && scope != that)
    		scope = scope.getParentScope();
    	return scope == that;
    }
    
    /**
     * Returns the closest ancestor to declare a variable of the given name, or
     * <tt>null</tt> if no such ancestor exists.
     * @param varName a variable name
     * @return a scope, or <tt>null</tt>
     */
    public final Scope getDeclaringScope(String varName) {
        Scope scope = this;
        while (scope != null) {
            if (scope.getDeclaredVariables().contains(varName)) {
                return scope;
            }
            scope = scope.getParentScope();
        }
        return null;
    }
    
    /** 
     * Determines whether this scope is the global scope.
     */
    public abstract boolean isGlobal();
}
