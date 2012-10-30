package dk.brics.jscontrolflow.analysis.reachdef;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowResult;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowSolver;
import dk.brics.jscontrolflow.analysis.liveness.Liveness;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.statements.WriteVariable;

/**
 * Computes the reaching definitions for temporary and {@link PrivateVariables private} program variables.
 * 
 * @author Asger
 */
public class ReachingDefinitions {

    private Function function;
    private ForwardFlowResult<Map<Variable,Set<VariableDefinition>>> result;
    private PrivateVariables privateVars;

    public ReachingDefinitions(Function function, PrivateVariables privateVars, boolean includeProgramVars) {
    	this(function, new Liveness(function), privateVars, includeProgramVars);
    }
    
    /**
     * 
     * @param function
     * @param privateVars
     * @param includeProgramVars whether to include program variables in the analysis (set to false if the function is the toplevel)
     */
    public ReachingDefinitions(Function function, Liveness liveness, PrivateVariables privateVars, boolean includeProgramVars) {
        this.function = function;
        this.privateVars = privateVars;
        this.result = ForwardFlowSolver.solve(function, new ReachingDefinitionsAnalysis(function, liveness, privateVars, includeProgramVars));
    }

    public Function getFunction() {
        return function;
    }
    
    /**
     * Returns true if the given statement definitely never occurs as a variable definition
     * in the reaching definitions. In this case, the reaching definitions for the assigned
     * variable will be inherited from v<sub>value</sub>.
     */
    public boolean isTransparentVariableAssignment(WriteVariable var) {
        return privateVars.isPrivate(var.getVarName(), var.getScope());
    }

    public Set<VariableDefinition> getReachingDefinitions(Statement stm, int tempVar) {
        assert stm.getBlock().getFunction() == function : "Statement belogs to another function";

        Map<Variable,Set<VariableDefinition>> map = result.getBefore(stm);
        if (map == null) {
            return Collections.emptySet();
        }
        Set<VariableDefinition> set = map.get(new TemporaryVariable(tempVar));
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    public Set<VariableDefinition> getReachingDefinitions(Statement stm, String varName, Scope scope) {
        assert stm.getBlock().getFunction() == function : "Statement belogs to another function";

        Map<Variable,Set<VariableDefinition>> map = result.getBefore(stm);
        if (map == null) {
            return Collections.emptySet();
        }
        Set<VariableDefinition> set = map.get(new ProgramVariable(varName, scope.getDeclaringScope(varName)));
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

}
