package dk.brics.jsrefactoring;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.EObjectLiteralProperty;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PObjectLiteralProperty;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.natives.DynamicAccessNative;
import dk.brics.jsutil.CollectionUtil;

public abstract class Refactoring {
	protected Master input;
	protected Log log = new Log();

	public Refactoring(Master input) {
		this.input = input;
	}

	public Master getInput() {
		return input;
	}

	public List<Diagnostic> getDiagnostics() {
		return log.diagnostics;
	}

	/**
	 * Checks preconditions, adding diagnostic messages to the log for violations, and
	 * builds a list of AST changes. If preconditions are violated, this list may be
	 * empty, but it should never be {@code null}.
	 * 
	 * @return
	 */
	public abstract List<Change> getChanges();
	
	/**
	 * Applies the AST modifications (ignoring any warnings).
	 * Must not be called more than once.
	 */
	public final void execute() {
		List<Change> changes = getChanges();
		execute(changes);
	}
	
	public final static void execute(List<Change> changes) {
		for(Change ch : changes)
			ch.perform();
	}

	/**
	 * Determines the changes that will be performed by a subrefactoring, adds them to
	 * the given collection of changes, and adds all the diagnostics created by the subrefactoring
	 * to the log.
	 */
	public void getSubRefactoringChanges(Refactoring refactoring, Collection<Change> changes) {
		changes.addAll(refactoring.getChanges());
		getDiagnostics().addAll(refactoring.getDiagnostics());
	}

	/**
	 * <p>
	 * This function ensures that there cannot be any reflective accesses (through dynamic property
	 * accesses, non-constant {@code in} expressions, or {@code for-in} loops) that might access a property
	 * of name {@code f} on the given set of objects.
	 * </p>
	 *  
	 * @param objects a set of object values
	 * @param f property name to check for capture
	 * @param finder
	 */
	public void checkReflectiveNameCapture(Set<ObjectValue> objects, String f, NodeFinder finder) {
		// check in-expressions
		for(ABinopExp exp : finder.getAllNodesOfType(ABinopExp.class)) {
			if(exp.getOp().kindPBinop() != EBinop.IN || !input.mayHaveName(exp, f))
				continue;
			if(CollectionUtil.intersects(input.getInExpObjectArgs(exp), objects))
				log.warn(exp, "Expression %s may change its truth value.", exp);
		}
		
        // dynamic property expressions
        for(ADynamicPropertyExp exp : finder.getAllNodesOfType(ADynamicPropertyExp.class)) {
        	if(!input.mayHaveName(exp, f))
        		continue;
            if(CollectionUtil.intersects(input.getReceivers(exp), objects))
            	log.error(exp, "Dynamic property expression %s may be captured.", exp);
        }
        
        // for-in loops
        for(AForInStmt stmt : finder.getAllNodesOfType(AForInStmt.class)) {
        	if(CollectionUtil.intersects(input.getForInObjects(stmt), objects))
        		log.error(stmt, "For-in loop may be affected.");
        }
        
        for(IFunction nat : findNativeDynamicAccesses(objects))
        	log.error("An invocation of native function %s may be affected by the refactoring.", getNativeFunctionName(nat));
	}

	protected boolean isDeleteOperand(Node nd) {
		Node parent = nd.parent();
		if(parent instanceof APrefixUnopExp)
			return ((APrefixUnopExp)parent).getOp().kindPPrefixUnop() == EPrefixUnop.DELETE;
		else if(parent instanceof AParenthesisExp)
			return isDeleteOperand(parent);
		return false;
	}

	/**
	 * Returns the set of all native library functions that may be invoked on one of the affected objects and
	 * dynamically access its properties.
	 */
	public Set<IFunction> findNativeDynamicAccesses(Set<ObjectValue> affectedObjects) {
		Set<IFunction> violatingNatives = new HashSet<IFunction>();
		for (DynamicAccessNative nat : DynamicAccessNative.LIST) {
	    	Function f = input.getHarnessNativeFunction(nat.getMember().getCodeName());
	    	UserFunctionValue obj = new UserFunctionValue(f, MainContext.Instance);
	    	String name = nat.getArgumentIndex() == DynamicAccessNative.THIS ? "this" : obj.getFunction().getParameterNames().get(nat.getArgumentIndex());
	    	Set<ObjectValue> values = CollectionUtil.filter(input.getValuesOfVariable(ScopeHost.fromScope(f), name), ObjectValue.class);
	    	values = input.getAllPrototypes(values, true);
	    	if (CollectionUtil.intersects(affectedObjects, values)) {
	    		violatingNatives.add(input.getFunctionNode(f));
	    	}
	    }
		return violatingNatives;
	}

	protected String getNativeFunctionName(IFunction function) {
		return function.getName().getText().replace('_', '.');
	}

	/**
	 * Determines conservatively whether the given expression may have side effects.
	 */
	public boolean mayHaveSideEffects(PExp exp) {
		return exp.apply(new AnswerAdapter<Boolean>() {
			@Override
			public Boolean caseAArrayLiteralExp(AArrayLiteralExp lit) {
				for(PExp exp : lit.getValues())
					if(mayHaveSideEffects(exp))
						return true;
				return false;
			}
			
			@Override
			public Boolean caseAConstExp(AConstExp exp) {
				return false;
			}
			
			@Override
			public Boolean caseAFunctionExp(AFunctionExp exp) {
				return false;
			}
			
			@Override
			public Boolean caseANameExp(ANameExp exp) {
				return false;
			}
			
			@Override
			public Boolean caseAPropertyExp(APropertyExp node) {
				// NB: we ignore possible exceptions from null dereference here
				return mayHaveSideEffects(node.getBase());
			}
			
			@Override
			public Boolean caseADynamicPropertyExp(ADynamicPropertyExp node) {
				// NB: we ignore possible exceptions from null dereference and side effects from
				//     invocation of toString()
				return mayHaveSideEffects(node.getBase())
					|| mayHaveSideEffects(node.getPropertyExp());
			}
			
			@Override
			public Boolean caseAThisExp(AThisExp node) {
				return false;
			}
			
			@Override
			public Boolean caseAObjectLiteralExp(AObjectLiteralExp lit) {
				for(PObjectLiteralProperty prop : lit.getProperties()) {
					if(prop.kindPObjectLiteralProperty() != EObjectLiteralProperty.NORMAL)
						continue;
					if(mayHaveSideEffects(((ANormalObjectLiteralProperty)prop).getValue()))
						return true;
				}
				return false;
			}
			
			@Override
			public Boolean defaultPExp(PExp node) {
				return true;
			}
		});
	}

	/**
	 * Determines whether any function that might be invoked at this node may use the value of <code>this</code>.
	 * Conservatively assumes that all native functions use <code>this</code>.
	 */
	protected boolean calleeMayUseThis(IInvocationNode inv) {
		for(FunctionValue callee : input.getCalledFunctions(inv)) {
			if(callee instanceof UserFunctionValue) {
				IFunction fun = input.getFunctionNode((UserFunctionValue)callee);
				final boolean[] foundThis = new boolean[1];
				fun.getBody().apply(new DepthFirstAdapter() {
					@Override public void caseAFunctionExp(AFunctionExp node) {}
					@Override public void caseAFunctionDeclStmt(AFunctionDeclStmt node) {}
					@Override public void caseAThisExp(AThisExp node) { foundThis[0] = true; }
				});
				if(foundThis[0])
					return true;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the input program uses <code>eval</code>, the <code>Function</code> constructor
	 * or assigns to <code>innerHTML</code>, and generates warnings for such uses.
	 * 
	 * @param input
	 */
	public void checkEval(Master input) {
	    if (input.isEvalUsed()) {
	    	log.warn("Use of eval detected. Cannot guarantee correctness.");
	    }
	    if (input.isFunctionConstructorUsed()) {
	    	log.warn("Use of Function constructor detected. Cannot guarantee correctness.");
	    }
	    if (input.isInnerHTMLAssignedTo()) {
//	    	log.warn("Use of innerHTML assignment detected. Cannot guarantee correctness.");
	    }
	}

	/**
	 * If the given expression (possibly surrounded by parentheses) is the function expression of an {@link AInvokeExp},
	 * this method returns that expression. Otherwise it returns <code>null</code>.
	 */
	public static AInvokeExp getInvocationParent(PExp nd) {
		Node parent = nd.parent();
		if(parent instanceof AInvokeExp) {
			AInvokeExp inv = (AInvokeExp)parent;
			if(inv.getFunctionExp() == nd)
				return inv;
		} else if(parent instanceof AParenthesisExp) {
			return getInvocationParent((PExp)parent);
		}
		return null;
	}
	
	/**
	 * Returns the innermost function that encloses both f1 and f2, or null
	 * if they originate from different files (they still have the global scope
	 * in common).
	 * @param f1 a function
	 * @param f2 a function
	 * @return a function or null
	 */
	public static Function getCommonEnclosingFunction(Function f1, Function f2) {
		Set<Function> chain1 = getEnclosingFunctions(f1, true);
		Function f = f2;
		while (f != null && !chain1.contains(f)) {
			f = f.getOuterFunction();
		}
		return f;
	}
	
	/**
	 * Returns the set of functions enclosing the given function.
	 * @param f a function
	 * @param includeSelf true if the function itself should be included
	 * @return newly created set
	 */
	public static Set<Function> getEnclosingFunctions(Function f, boolean includeSelf) {
		Set<Function> set = new HashSet<Function>();
		if (!includeSelf) {
			f = f.getOuterFunction();
		}
		while (f != null) {
			set.add(f);
			f = f.getOuterFunction();
		}
		return set;
	}
}