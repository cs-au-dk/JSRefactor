package dk.brics.jsrefactoring.inlining;

import static dk.brics.jsrefactoring.NodeFactory.createBlock;
import static dk.brics.jsrefactoring.NodeFactory.createFunctionExp;
import static dk.brics.jsrefactoring.NodeFactory.createParenExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Token;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFactory;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.PrettyPrinter;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.changes.InsertExpIntoArglist;
import dk.brics.jsrefactoring.changes.ReplaceExp;
import dk.brics.jsrefactoring.encapsulateprty.ScopeConsistencyAnalysis;

public class InlineToOneShotClosure extends Refactoring {
	
	private AInvokeExp invoke;
	private IFunction inlinedFunction;
	private List<Change> changes = new ArrayList<Change>();
	
	// for evaluation
	private Set<String> safeClosureVars = new HashSet<String>();
	private Set<String> unsafeClosureVars = new HashSet<String>();
    private Set<String> inaccessibleClosureVars = new HashSet<String>();
    private boolean isBogusInvoke = false; // unreachable or cannot terminate normally
    private boolean multipleTargets = false;
    private boolean nativeTarget = false;
    private CommonClosureType commonClosureType = CommonClosureType.None;
    
    public enum CommonClosureType {
        None,
        FunctionDeclaration,
        ScopeConsistency,
    }
	
	public InlineToOneShotClosure(final Master input, AInvokeExp invoke) {
		super(input);
		this.invoke = invoke;
		final Function containingFunction = (Function) input.getScope(invoke.getAncestor(ABody.class));
		
		isBogusInvoke = true;
		inlinedFunction = null;
		for (FunctionValue fv : input.getCalledFunctions(invoke)) {
		    isBogusInvoke = false;
			if (fv instanceof NativeFunctionValue) {
			    if (!nativeTarget) {
	                nativeTarget = true;
			        log.error("Cannot inline call to native function %s", ((NativeFunctionValue) fv).getPrettyName());
			    }
			} else {
				UserFunctionValue uf = (UserFunctionValue)fv;
				IFunction functionNode = input.getFunctionNode(uf.getFunction());
				if (inlinedFunction != null && functionNode != inlinedFunction) {
				    if (!multipleTargets) {
    				    multipleTargets = true;
    					log.error("Multiple functions can be invoked");
				    }
				}
				if (input.isNativeCode(functionNode)) {
				    if (!nativeTarget) {
				        nativeTarget = true;
				        if (functionNode.getName() != null) {
				            log.error("Cannot inline native function %s", functionNode.getName().getText().replace("_", "."));
				        } else {
				            log.error("Cannot inline native function");
				        }
				    }
				}
				inlinedFunction = functionNode;
			}
		}
		
		if (inlinedFunction == null) {
			log.error("No function to inline");
			return;
		}
		
		final Function inlinedFunctionCfg = (Function) input.getScope(inlinedFunction.getBody());
		
		// special case check: function is definitely in common closure
		final Function[] commonClosureScope = new Function[] {null};
		if (invoke.getFunctionExp() instanceof ANameExp) {
			ANameExp nameExp = (ANameExp) invoke.getFunctionExp();
			String name = Literals.getName(nameExp);
			Scope scope = input.getScope(nameExp.getAncestor(IScopeBlockNode.class));
			boolean foundWithScope = false;
			while (scope.getParentScope() != null && !scope.getDeclaredVariables().contains(name)) {
				if (scope instanceof WithScope) {
					foundWithScope = true;
					break;
				}
				scope = scope.getParentScope();
			}
			if (scope.getParentScope() != null && !foundWithScope) {
				Function f = scope.getAncestorScope(Function.class);
				if (input.refersToCommonClosureFunction(name, f)) {
					commonClosureScope[0] = f;
					commonClosureType = CommonClosureType.FunctionDeclaration;
				}
			}
		}
		else if (invoke.getFunctionExp() instanceof APropertyExp) {
			APropertyExp prty = (APropertyExp) invoke.getFunctionExp();
			if (prty.getBase() instanceof AThisExp) {
				commonClosureScope[0] = findCommonThisClosure(inlinedFunctionCfg, containingFunction);
                commonClosureType = CommonClosureType.ScopeConsistency;
			}
		}
		
		// TODO: Report a warning instead of an error for closure variables whose declaration is accessible,
		//       but where a different instance may be read from.
		
		// check for inaccessible closure variables and collect names of used global variables
		final Scope callsiteScope = input.getScope(invoke.getAncestor(IScopeBlockNode.class));
		final Set<String> commonVarsUsed = new HashSet<String>();
		inlinedFunction.apply(new DepthFirstAdapter() {
			@Override
			public void caseANameExp(ANameExp node) {
				String name = Literals.getName(node);
				boolean outsideInlinedFunction = false;
				boolean crossedCommonClosureScope = false;
				Scope scope = input.getScope(node.getAncestor(IScopeBlockNode.class));
				boolean invalidAccess = false;
				while (true) {
					if (scope == commonClosureScope[0]) {
						crossedCommonClosureScope = true;
					}
					if (scope.getParentScope() == null || scope.getDeclaredVariables().contains(name)) {
						break;
					}
					if (scope == inlinedFunctionCfg) {
						outsideInlinedFunction = true;
					}
					if (outsideInlinedFunction && !crossedCommonClosureScope && scope instanceof WithScope) {
						WithScope ws = (WithScope) scope;
						for (ObjectValue obj : input.getWithStmtArguments(ws.getStatement())) {
							if (!input.isPropertyDefinitelyAbsent(obj, name)) {
								log.warn("Variable reference %s may resolve to with-statement", name);
								invalidAccess = true;
							}
						}
					}
					scope = scope.getParentScope();
				}
				if (outsideInlinedFunction && !crossedCommonClosureScope && scope.getParentScope() != null) {
				    if (callsiteScope.hasAncestorScope(scope)) {
				        if (unsafeClosureVars.add(name)) {
				            log.warn("A different instance of the closure variable %s may be read at the callsite", name);
				        }
				    } else {
				        if (inaccessibleClosureVars.add(name)) {
				            log.error("Closure variable %s is not accessible at the callsite", name);
				        }
				    }
					invalidAccess = true;
				}
				if (!invalidAccess && outsideInlinedFunction) {
					commonVarsUsed.add(name);
					if (scope.getParentScope() != null) {
					    safeClosureVars.add(name); // global vars don't count as closure vars for evaluation
					}
				}
			}
		});
		
		// check that references to global variables will not be captured at the callsite
		{
			Scope scope = input.getScope(invoke.getAncestor(IScopeBlockNode.class));
			while (scope.getParentScope() != null && scope != commonClosureScope[0]) {
				for (String commonVar : commonVarsUsed) {
					if (scope.getDeclaredVariables().contains(commonVar)) {
						log.error("Reference to variable %s will be captured by a scope enclosing the callsite", commonVar);
					}
					if (scope instanceof WithScope) {
						WithScope ws = (WithScope) scope;
						for (ObjectValue obj : input.getWithStmtArguments(ws.getStatement())) {
							if (!input.isPropertyDefinitelyAbsent(obj, commonVar)) {
								log.warn("Reference to variable %s may be captured by a with-statement enclosing the callsite", commonVar);
							}
						}
					}
				}
				scope = scope.getParentScope();
			}
		}
		
		PExp funcExp = invoke.getFunctionExp();
		while (funcExp instanceof AParenthesisExp) {
		    funcExp = ((AParenthesisExp)funcExp).getExp();
		}
		switch (funcExp.kindPExp()) {
		case NAME:
		case THIS:
        case PROPERTY:          // we ignore potential null-reference exception for property accesses
        case DYNAMIC_PROPERTY:
		    break;
	    default:
	        log.warn("Side-effects of the inlined expression may be suppressed");
		}
		
		// prepare changes
		{
			// first construct a skeleton closure with empty body to pretty print
			List<String> parms = new ArrayList<String>();
			for(Token parm : inlinedFunction.getParameters())
				parms.add(parm.getText());
			AFunctionExp newFunctionExp = createFunctionExp(parms, createBlock());
			AParenthesisExp newFunctionParen = createParenExp(newFunctionExp);
			PrettyPrinter.pp(newFunctionParen);
			// now replace the empty body with the actual body
			ABody bodyClone = AstUtil.clone(inlinedFunction.getBody());
			PrettyPrinter.setBody(newFunctionExp, bodyClone);
			
			PExp calleeExp = invoke.getFunctionExp();
			while (calleeExp instanceof AParenthesisExp) {
				calleeExp = ((AParenthesisExp)calleeExp).getExp();
			}
			if (calleeExp instanceof IPropertyAccessNode && calleeMayUseThis(invoke)) {
				IPropertyAccessNode prty = (IPropertyAccessNode)calleeExp;
				PExp base = prty.getBase();
				// TODO: Check for type coercion of base
				// TODO: Check for reassignment of Function.call
				changes.add(new InsertExpIntoArglist(invoke.getLparen(), invoke.getArguments(), invoke.getRparen(), 0, AstUtil.clone(base)));
				APropertyExp callExp = NodeFactory.createPropertyExp(newFunctionParen, "call");
				PrettyPrinter.pp(callExp);
				changes.add(new ReplaceExp(calleeExp, callExp));
			}
			else if (calleeExp instanceof ANameExp && !input.getWithScopeReceivers((ANameExp)calleeExp).isEmpty() && calleeMayUseThis(invoke)) {
				log.warn(calleeExp, "Argument to with-statement may act as 'this' argument. Cannot reproduce behavior if inlined");
				changes.add(new ReplaceExp(calleeExp, newFunctionParen)); // add a best guess change
			}
			else {
				changes.add(new ReplaceExp(calleeExp, newFunctionParen));
			}
		}
	}

	private Function findCommonThisClosure(Function f1, Function f2) {
		Function enclosing = getCommonEnclosingFunction(f2, f1);
		Set<Function> thisunique = input.getThisUniqueFunctions();
		
		Function commonClosure = enclosing;
		while (commonClosure != null && !thisunique.contains(commonClosure)) {
			commonClosure = commonClosure.getOuterFunction();
		}
		
		if (commonClosure == null) {
			return null;
		}
		
		ScopeConsistencyAnalysis sca = new ScopeConsistencyAnalysis(input, new NodeFinder(input), getEnclosingFunctions(commonClosure, true));
		
		for (Function f = f1; f != commonClosure; f=f.getOuterFunction()) {
			if (!sca.isWellScoped(f))
				return null;
		}
		for (Function f = f2; f != commonClosure; f=f.getOuterFunction()) {
			if (!sca.isWellScoped(f))
				return null;
		}
		
		return commonClosure;
	}

	@Override
	public List<Change> getChanges() {
		return changes;
	}

	public Set<String> getSafeClosureVars() {
        return safeClosureVars;
    }
	public Set<String> getUnsafeClosureVars() {
        return unsafeClosureVars;
	}
	public Set<String> getInaccessibleClosureVars() {
        return inaccessibleClosureVars;
    }
	public boolean isBogusInvoke() {
        return isBogusInvoke;
    }
	public AInvokeExp getInvoke() {
        return invoke;
    }
	public boolean hasMultipleTargets() {
        return multipleTargets;
    }
	public boolean hasNativeTarget() {
        return nativeTarget;
    }
	public CommonClosureType getCommonClosureType() {
        return commonClosureType;
    }
}
