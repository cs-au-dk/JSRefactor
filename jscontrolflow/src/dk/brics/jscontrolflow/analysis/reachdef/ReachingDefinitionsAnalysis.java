package dk.brics.jscontrolflow.analysis.reachdef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowAnalysis;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowSolver;
import dk.brics.jscontrolflow.analysis.liveness.Liveness;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.DeclareVariable;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.WriteVariable;

/**
 * Flow analysis that computes reaching definitions for temporary variables
 * and program variables that are not assigned to by inner functions.
 * A <i>definition</i> of a variable is a parameter or a statement that assigned its value.
 * <p/>
 * A {@link Phi} statement is never considered a variable definition; instead,
 * its result variable inherits the definitions of the the two argument variables.
 * 
 * @see ForwardFlowSolver#solve(Function, ForwardFlowAnalysis)
 */
public class ReachingDefinitionsAnalysis implements ForwardFlowAnalysis<Map<Variable, Set<VariableDefinition>>> {

    private Function function;
    private Liveness liveness;
    private PrivateVariables privateVars;
    private boolean includeProgramVars;
    
    /**
     * Creates a reaching definition analysis for the given function.
     * @param function the function to analyze - should be the same function passed to {@link ForwardFlowSolver#solve(Function, ForwardFlowAnalysis) solve}
     * @param privateVars private variables; may be <tt>null</tt> if <tt>includeProgramVars</tt> is <tt>false</tt> 
     * @param includeProgramVars whether to include program variables in the analysis
     */
    public ReachingDefinitionsAnalysis(Function function,
    		Liveness liveness,
            PrivateVariables privateVars,
            boolean includeProgramVars) {
        if (includeProgramVars && privateVars == null) {
            throw new IllegalArgumentException("Private variables must be specified if program variables are to be included");
        }
        this.liveness = liveness;
        this.function = function;
        this.privateVars = privateVars;
        this.includeProgramVars = includeProgramVars;
    }

    private boolean isPrivateVar(String name, Scope scope) {
        if (!includeProgramVars || privateVars.getInnerAssignedVars(function).contains(name)) {
            return false;
        }
        while (scope != null) {
            if (scope.getDeclaredVariables().contains(name)) {
                return true;
            }
            // if variable is not declared inside function, then it is not private
            if (scope instanceof Function) {
                return false;
            }
            scope = scope.getParentScope();
        }
        throw new RuntimeException("Scope not nested in any Function instance");
    }
    private boolean isInWithScope(Scope scope, String var) {
        while (!(scope instanceof Function) && !scope.getDeclaredVariables().contains(var)) {
            if (scope instanceof WithScope) {
                return true;
            }
            scope = scope.getParentScope();
        }
        return false;
    }

    @Override
    public Map<Variable, Set<VariableDefinition>> bottom() {
        return null; // null means unreachable - empty map means reachable but no definitions
        //		return Collections.emptyMap();
    }

    @Override
    public Map<Variable, Set<VariableDefinition>> entry() {
        Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>();
        /// TODO only need to add private variables here
        for (int i=0; i<function.getParameterNames().size(); i++) {
            result.put(new ProgramVariable(function.getParameterNames().get(i), function), Collections.<VariableDefinition>singleton(new ParameterVariableDefinition(i)));
        }
        if (!function.hasExplicitArgumentsDeclaration() && function.getOuterFunction() != null) {
            result.put(new ProgramVariable("arguments", function), Collections.<VariableDefinition>singleton(ArgumentsArrayVariableDefinition.Instance));
        }
        
        for (String varName : function.getDeclaredVariables()) {
        	ProgramVariable var = new ProgramVariable(varName, function);
        	if (result.containsKey(var))
        		continue;
        	result.put(var, Collections.<VariableDefinition>singleton(UninitializedVariableDefinition.Instance));
        }
        
        return result;
    }

    @Override
    public Map<Variable, Set<VariableDefinition>> transfer(
            Statement stmt,
            Map<Variable, Set<VariableDefinition>> before) {
        if (before == null) {
            return null; // if unreachable, still unreachable
        }
        if (stmt instanceof DeclareVariable) {
        	DeclareVariable decl = (DeclareVariable) stmt;
        	switch (decl.getKind()) {
        	case SELF:
        		Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>(before);
        		result.put(new ProgramVariable(decl.getVarName(), function), Collections.<VariableDefinition>singleton(SelfVariableDefinition.Instance));
        		return result;
        	default:
        		return before;
        	}
        }
        else if (stmt instanceof ReadVariable) {
            ReadVariable read = (ReadVariable)stmt;
            Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>(before);
            if (isInWithScope(read.getScope(), read.getVarName()) || !isPrivateVar(read.getVarName(), read.getScope())) {
                VariableDefinition def = new StatementVariableDefinition(read);
                result.put(new TemporaryVariable(read.getResultVar()), Collections.singleton(def));
            } else {
                result.put(new TemporaryVariable(read.getResultVar()), before.get(new ProgramVariable(read.getVarName(), read.getScope().getDeclaringScope(read.getVarName()))));
            }
            return result;
        } else if (stmt instanceof WriteVariable) {
            WriteVariable write = (WriteVariable)stmt;
            if (!isPrivateVar(write.getVarName(), write.getScope())) {
                return before;
            } else {
                Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>(before);
                Scope declaringScope = write.getScope().getDeclaringScope(write.getVarName());
                ProgramVariable programVar = new ProgramVariable(write.getVarName(), declaringScope);
                Set<VariableDefinition> definitions;
                if (isInWithScope(write.getScope(), write.getVarName())) {
                    // perform weak update only - assignment might resolve to with statement and not actually overwrite the variable
                    definitions = SharedMultiMapUtil.union(before.get(new TemporaryVariable(write.getValueVar())), before.get(programVar));
                } else {
                    definitions = before.get(new TemporaryVariable(write.getValueVar()));
                }
                result.put(programVar, definitions);
                return result;
            }
        } else if (stmt instanceof Phi) {
            Phi phi = (Phi)stmt;
            Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>(before);
            result.put(new TemporaryVariable(phi.getResultVar()), 
                    SharedMultiMapUtil.union(before.get(new TemporaryVariable(phi.getArg1Var())), before.get(new TemporaryVariable(phi.getArg2Var()))));
            return result;
        } else if (stmt instanceof Assignment) { // keep this case below Phi and ReadVariable since those are also Assignments
            Assignment asn = (Assignment)stmt;
            Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>(before);
            VariableDefinition def = new StatementVariableDefinition(asn);
            int var = asn.getResultVar();
            result.put(new TemporaryVariable(var), Collections.singleton(def));
            return result;
        } else {
            return before;
        }
    }

    @Override
    public Map<Variable, Set<VariableDefinition>> leastUpperBound(
            Map<Variable, Set<VariableDefinition>> arg1,
            Map<Variable, Set<VariableDefinition>> arg2,
            Block followingBlock) {
    if (arg1 == arg2 || arg2 == null) {
		    return arg1;
		}
		if (arg1 == null) {
		    return arg2;
		}
		Set<Integer> live = liveness.getLiveBefore(followingBlock);
		Map<Variable, Set<VariableDefinition>> result = new HashMap<Variable, Set<VariableDefinition>>();
		for (Map.Entry<Variable, Set<VariableDefinition>> en1 : arg1.entrySet()) {
			if (en1.getKey() instanceof TemporaryVariable) {
				TemporaryVariable tmp = (TemporaryVariable)en1.getKey();
				if (!live.contains(tmp.getIndex())) {
					continue; // don't add dead variables to map
				}
			}
		    Set<VariableDefinition> set2 = arg2.get(en1.getKey());
		    if (set2 == null) {
		        result.put(en1.getKey(), en1.getValue());
		    } else {
		        result.put(en1.getKey(), SharedMultiMapUtil.union(en1.getValue(), set2));
		    }
		}
		for (Map.Entry<Variable, Set<VariableDefinition>> en2 : arg2.entrySet()) {
		    if (arg1.containsKey(en2.getKey())) {
		        continue; // handled in previous pass
		    }
		    if (en2.getKey() instanceof TemporaryVariable) {
				TemporaryVariable tmp = (TemporaryVariable)en2.getKey();
				if (!live.contains(tmp.getIndex())) {
					continue; // don't add dead variables to map
				}
			}
		    result.put(en2.getKey(), en2.getValue());
		}
		return result;
    }

	@Override
    public boolean equal(Map<Variable, Set<VariableDefinition>> arg1,
            Map<Variable, Set<VariableDefinition>> arg2) {
        if (arg1 == arg2) {
            return true;
        }
        if (arg1 == null || arg2 == null) {
            return false;
        }
        return arg1.equals(arg2);
    }

}
