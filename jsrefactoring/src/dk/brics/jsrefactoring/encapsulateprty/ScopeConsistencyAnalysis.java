package dk.brics.jsrefactoring.encapsulateprty;

import java.util.HashSet;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.analysis.reachdef.ArgumentsArrayVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ParameterVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.SelfVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.StatementVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.UninitializedVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinitionQuestionAnswer;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.EExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.DOMNative;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;

/**
 * This class implements a conservative well-scopedness analysis.
 * 
 * <p>
 * A function <code>f</code> nested inside a function <code>g</code> is <i>well-scoped</i>
 * if every closure <code>c<sub>f</sub></code> of <code>f</code> created during an execution
 * of a closure <code>c<sub>g</sub></code> of <code>g</code> is only invoked on the receiver
 * of <code>c<sub>g</sub></code>.
 * </p>
 * 
 * <p>
 * A function <code>f</code> reflexively-transitively nested inside a function <code>h</code>
 * is <i>well-scoped up to <code>h</code></i> if <code>f</code> and every enclosing
 * function of <code>f</code> up to but not including <code>h</code> is well-scoped
 * It does not matter whether <code>h</code> itself is well-scoped.
 * </p>
 * 
 * @author asf
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class ScopeConsistencyAnalysis {
	private final Master input;
	private final NodeFinder finder;
	private final Set<Function> yes = new HashSet<Function>(),
	                            no = new HashSet<Function>();
	
	public ScopeConsistencyAnalysis(Master input, NodeFinder finder, Set<Function> enclosing) {
		this.input = input;
		this.finder = finder;
		this.yes.addAll(enclosing);
	}
	
	public boolean isWellScoped(IFunction f) {
		for(Function ff : input.getFunctions(f))
			if(!isWellScoped(ff))
				return false;
		return true;
	}
	
	public boolean isWellScoped(Function f) {
		if(yes.contains(f))
			return true;
		if(no.contains(f))
			return false;
		analyse(f);
		return isWellScoped(f);
	}
	
	private void analyse(Function f) {
		if(f.getOuterFunction() == null) {
			no.add(f);
		} else {
			Function outer = f.getOuterFunction();
			IFunction outerNode = input.getFunctionNode(outer);
			
			// f must not be reachable from DOM
			if(input.getReceivers(f).contains(DOMNative.Instance)) {
				no.add(f);
				return;
			}
			
			// any assignment whose RHS may evaluate to f must be in the body of outer, its RHS must have the function
			// declaration/expression as its only reaching definition, and its LHS must be a local variable or a 
			// this-property expression
			for(AAssignExp assgn : finder.getAllNodesOfType(AAssignExp.class)) {
				if(!mayReferTo(input.getAssignmentRHS(assgn), f))
					continue;
				if(assgn.getAncestor(IFunction.class) == outerNode) {
					// check that RHS must have f as its only reaching definition
					Set<VariableDefinition> defs = input.getRHSReachingDefs(assgn);
					if(defs.size() == 1) {
						VariableDefinition def = defs.iterator().next();
						if(def.apply(isCreateFunction(), null)) {
							// check that LHS is a this-property expression or local variable
							PExp lhs = assgn.getLeft();
							if(lhs instanceof ANameExp && input.getWithScopeReceivers((ANameExp)lhs).isEmpty())
								continue;
							if(lhs instanceof APropertyExp && ((APropertyExp)lhs).getBase().kindPExp() == EExp.THIS)
								continue;
						}
					}	
				}
				// if any of the above checks fail, this function cannot be proved well-scoped
				no.add(f);
				return;
			}

			// every call to f must have a function expression of the form e.f, with f directly read from e, not its prototypes
			for(IInvocationNode inv : finder.getAllNodesOfType(IInvocationNode.class)) {
				if(mayReferTo(input.getCalledFunctions(inv), f)) {
					PExp funexp = inv.getFunctionExp();
					if(funexp instanceof APropertyExp) {
						PropertyExpAccess acc = new PropertyExpAccess((APropertyExp)funexp);
						Set<ObjectValue> recv = acc.getDirectReceivers(input);
						for(ObjectValue proto : input.getAllPrototypes(recv, false)) {
							if(!input.isPropertyDefinitelyAbsent(proto, acc.getName())) {
								no.add(f);
								return;
							}
						}
						continue;
					}
					no.add(f);
					return;
				}
			}
			
			// f is not invoked through Function.prototype.{apply, bind, call}
			for(NativeFunctionValue nativ : new NativeFunctionValue[] {FunctionCallNative.Instance, FunctionApplyNative.Instance, FunctionBindNative.Instance}) {
				for(UserFunctionValue uf : input.lookupContextInsensitive(nativ.getThisArg(NullContext.Instance), UserFunctionValue.class)) {
					if(uf.getFunction() == f) {
						no.add(f);
						return;
					}
				}
			} 
						
			(isWellScoped(outer) ? yes : no).add(f);
		}
	}
	
	private boolean mayReferTo(Set<? extends Value> vals, Function f) {
		for(Value val : vals)
			if(val instanceof UserFunctionValue && ((UserFunctionValue)val).getFunction() == f)
				return true;
		return false;
	}
	
	public VariableDefinitionQuestionAnswer<Void, Boolean> isCreateFunction() {
		return new VariableDefinitionQuestionAnswer<Void, Boolean>() {
			@Override public Boolean caseStatement(StatementVariableDefinition def, Void arg) {
				return def.getStatement() instanceof CreateFunction;
			}

			@Override public Boolean caseParameter(ParameterVariableDefinition def, Void arg) {
				return false;
			}

			@Override public Boolean caseArgumentsArray(ArgumentsArrayVariableDefinition def, Void arg) {
				return false;
			}

			@Override public Boolean caseUninitialized(UninitializedVariableDefinition def,	Void arg) {
				return false;
			}
			
			@Override public Boolean caseSelf(SelfVariableDefinition def, Void arg) {
				return false;
			}
		};
	}
}
