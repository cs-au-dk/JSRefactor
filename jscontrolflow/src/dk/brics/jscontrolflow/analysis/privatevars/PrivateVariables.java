package dk.brics.jscontrolflow.analysis.privatevars;

import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.CallVariable;
import dk.brics.jscontrolflow.statements.IVariableAccessStatement;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jsutil.MultiMap;

/**
 * Computes which program variables are assigned to by inner functions
 * and which are read from by inner functions. By <i>inner functions</i>
 * we mean the set of transitive inner functions of the function that declared
 * the variable, excluding the declaring function itself.
 * <p/>
 * This is useful for determining if a variable is <i>private</i>, meaning that
 * it can be treated as a traditional local variable, without interference from other
 * functions or with statements. See {@link #isPrivate(String, Scope)}.
 */
public class PrivateVariables {

    private MultiMap<Function, String> innerAssignedVars = new MultiMap<Function, String>();
    private MultiMap<Function, String> innerReadVars = new MultiMap<Function, String>();

    public PrivateVariables(Function root) {
        for (Function func : root.getTransitiveInnerFunctions(true)) {
            for (Block block : func.getBlocks()) {
                for (Statement stm : block.getStatements()) {
                    if (stm instanceof ReadVariable || stm instanceof CallVariable) {
                        IVariableAccessStatement read = (IVariableAccessStatement) stm;
                        Scope scope = resolveScope(read.getScope(), read.getVarName());
                        if (scope == null) {
                            continue;
                        }
                        Function affectedFunction = scope.getAncestorScope(Function.class);
                        if (affectedFunction != null && affectedFunction != func) {
                            innerReadVars.add(affectedFunction, read.getVarName());
                        }
                    }
                    else if (stm instanceof WriteVariable) {
                        WriteVariable write = (WriteVariable) stm;
                        Scope scope = resolveScope(write.getScope(), write.getVarName());
                        if (scope == null) {
                            continue;
                        }
                        Function affectedFunction = scope.getAncestorScope(Function.class);
                        if (affectedFunction != null && affectedFunction != func) {
                            innerAssignedVars.add(affectedFunction, write.getVarName());
                        }
                    }
                }
            }
        }
    }

    private Scope resolveScope(Scope scope, String name) {
        while (scope != null ) {
            if (scope.getDeclaredVariables().contains(name)) {
                return scope;
            }
            scope = scope.getParentScope();
        }
        return null;
    }

    public Set<String> getInnerAssignedVars(Function func) {
        return innerAssignedVars.getView(func);
    }
    public Set<String> getInnerReadVars(Function func) {
        return innerReadVars.getView(func);
    }
    
    /**
     * Returns true if the variable as seen from the given scope can be treated as
     * a traditional local variable. The following conditions must hold:
     * <ul>
     * <li>The variable is not global
     * <li>The variable is declared inside the function containing the given scope
     * <li>Inner functions don't assign to this variable
     * <li>The variable cannot be resolved by a with statement. This check fails if a with scope W exists such that
     * <ul>
     *  <li>W encloses the given scope
     *  <li>The scope declaring the variable encloses W
     * </ul>
     * </ul>
     * @param varName name of a variable
     * @param scope the scope from which the variable is resolved
     * @return true if private
     */
    public boolean isPrivate(String varName, Scope scope) {
        Scope declaringScope = scope.getDeclaringScope(varName);
        if (declaringScope == null || declaringScope.getParentScope() == null) {
            return false; // undeclared variables are global variables
        }
        Function declaringFunction = declaringScope.getAncestorScope(Function.class);
        if (declaringFunction != scope.getAncestorScope(Function.class)) {
            return false; // declared in parent function
        }
        if (innerAssignedVars.contains(declaringFunction, varName)) {
            return false; // assigned to by inner functions
        }
        Scope s = scope;
        while (s != declaringScope) {
            if (s instanceof WithScope)
                return false; // contained in with scope
            s = s.getParentScope();
        }
        return true;
    }

}
