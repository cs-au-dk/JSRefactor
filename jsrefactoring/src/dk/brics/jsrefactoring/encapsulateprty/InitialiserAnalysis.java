package dk.brics.jsrefactoring.encapsulateprty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.EExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsutil.CollectionUtil;

/**
 * A function is an initialiser if it is invoked exactly once on its every receiver,
 * and this invocation happens before any properties of the receiver are accessed.
 * 
 * <p>This class implements a (currently very) conservative analysis to determine
 * whether a function is an initialiser by making sure that it is only invoked through
 * <code>new</code> expressions with a unique invocation target.</p>
 * 
 * TODO: Extend this to allow super constructor invocations.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class InitialiserAnalysis {
	private final Master input;
	private final NodeFinder finder;
	private final Set<IFunction> yes = new HashSet<IFunction>(), no = new HashSet<IFunction>();
	private final boolean suspiciousCalls;
	private final Set<NativeFunctionValue> applyOrCall = new HashSet<NativeFunctionValue>(Arrays.asList(FunctionApplyNative.Instance, FunctionCallNative.Instance));
	
	public InitialiserAnalysis(Master input, NodeFinder finder) {
		this.input = input;
		this.finder = finder;
		this.suspiciousCalls = suspiciousReflectiveCalls();
	}
	
	private boolean isInitialisingCall(AInvokeExp inv) {
		// must be first statement in a function
		if(!(inv.parent() instanceof AExpStmt))
			return false;
		IFunction enclosing = inv.getAncestor(IFunction.class);
		if(enclosing == null || enclosing.getBody().getBlock().getStatements().isEmpty())
			return false;
		if(enclosing.getBody().getBlock().getStatements().getFirst() != inv.parent())
			return false;
		
		// first argument must be "this"
		if(inv.getArguments().isEmpty() || inv.getArguments().getFirst().kindPExp() != EExp.THIS)
			return false;
		
		// must be an invocation of Function.prototype.{apply, call}
		Set<FunctionValue> callees = input.getCalledFunctions(inv);
		if(!applyOrCall.containsAll(callees))
			return false;
		
		return true;
	}
	
	// check whether Function.prototype.{apply, bind, call} may ever be invoked on each other
	private boolean suspiciousReflectiveCalls() {
		Set<NativeFunctionValue> abc = new HashSet<NativeFunctionValue>(Arrays.asList(FunctionApplyNative.Instance, 
																					  FunctionBindNative.Instance,
																					  FunctionCallNative.Instance));
		for(NativeFunctionValue f : abc)
			if(CollectionUtil.intersects(abc, input.lookupContextInsensitive(f.getThisArg(NullContext.Instance))))
				return true;
		return false;
	}
	
	public boolean isInitialiser(IFunction fun) {
		if(suspiciousCalls)
			return false;
		if(yes.contains(fun))
			return true;
		if(no.contains(fun))
			return false;
		return computeIsInitialiser(fun);
	}
	
	private boolean computeIsInitialiser(IFunction fun) {
		// to break circularity
		no.add(fun);
		
		boolean isInitialiser = true;
		Set<Function> funs = input.getFunctions(fun);
		
		// check whether it is ever passed to Function.prototype.bind
		Set<Value> bindRecv = input.lookupContextInsensitive(FunctionBindNative.Instance.getThisArg(NullContext.Instance));
		for(Value v : bindRecv) {
			if(v instanceof UserFunctionValue && funs.contains(((UserFunctionValue)v).getFunction())) {
				isInitialiser = false;
				break;
			}
		}
		
		// check all invocations
		if(isInitialiser) {
			for(IInvocationNode inv : finder.getAllNodesOfType(IInvocationNode.class)) {
				if(mayCall(inv, funs)) {
					if(!(inv instanceof ANewExp) || !mustCall(inv, funs)) {
						isInitialiser = false;
						break;
					}
				}
				if(inv instanceof AInvokeExp && mayInvokeReflectively((AInvokeExp)inv, funs)) {
					if(isInitialisingCall((AInvokeExp)inv))
						if(isInitialiser(inv.getAncestor(IFunction.class)) && mustInvokeReflectively((AInvokeExp)inv, funs))
							continue;
					isInitialiser = false;
					break;
				}
			}
		}
		
		if(isInitialiser) {
			no.remove(fun);
			yes.add(fun);
		}
		
		return isInitialiser;
	}
	
	private boolean mayCall(IInvocationNode inv, Set<Function> funs) {
		for(FunctionValue callee : input.getCalledFunctions(inv))
			if(callee instanceof UserFunctionValue && funs.contains(((UserFunctionValue)callee).getFunction()))
				return true;
		return false;
	}
	
	private boolean mustCall(IInvocationNode inv, Set<Function> funs) {
		for(FunctionValue callee : input.getCalledFunctions(inv))
			if(!(callee instanceof UserFunctionValue) || !funs.contains(((UserFunctionValue)callee).getFunction()))
				return false;
		return true;		
	}
	
	private boolean mayInvokeReflectively(AInvokeExp inv, Set<Function> funs) {
		if(!CollectionUtil.intersects(input.getCalledFunctions(inv), applyOrCall))
			return false;
		PExp functor = inv.getFunctionExp();
		if(!(functor instanceof APropertyExp))
			return true;
		for(ObjectValue recv : input.getAccessedObjects((APropertyExp)functor))
			if(recv instanceof UserFunctionValue && funs.contains(((UserFunctionValue)recv).getFunction()))
				return true;
		return false;
	}
	
	private boolean mustInvokeReflectively(AInvokeExp inv, Set<Function> funs) {
		if(!applyOrCall.containsAll(input.getCalledFunctions(inv)))
			return false;
		PExp functor = inv.getFunctionExp();
		if(!(functor instanceof APropertyExp))
			return false;
		for(ObjectValue recv : input.getAccessedObjects((APropertyExp)functor))
			if(!(recv instanceof UserFunctionValue) || !funs.contains(((UserFunctionValue)recv).getFunction()))
				return false;
		return true;
	}
}
