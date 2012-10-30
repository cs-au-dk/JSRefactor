package dk.brics.jspointers.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.InvokeContext;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.values.AllocObjectValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.Callback;

/**
 * <i>k</i>-callsite context sensitivity (<i>k</i> is called <i>depth</i> in this class).
 * 
 * @author Asger
 */
public class CallsiteSensitiveInvocation implements InvocationStrategy {

    private int depth;

    public CallsiteSensitiveInvocation(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth must be non-negative");
        }
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public void transferInvoke(InvokeNode node, Context context, LatticeUtil lattice) {
        Callback<Key,Set<Value>> callback = lattice.getCallback();
        Set<Value> thisArgs = lattice.getActualThisArgs(node, context);
        Set<Value> funcArgs = lattice.getValuesAtInputPoint(node.getFunc(), context);
        for (Value funcval : funcArgs) {
            if (!(funcval instanceof FunctionValue)) {
                continue;
            }
            FunctionValue f = (FunctionValue)funcval;
            Context targetCtx = ContextWidening.widenContext(new InvokeContext(node.getCallsiteId(), context), this.depth);
            lattice.linkThisArgsToFunction(thisArgs, f, targetCtx);
            lattice.linkLabelArgToFunction(Collections.singleton(new AllocObjectValue(node.getCallsiteId(), context)), f, targetCtx);
            for (int i=0; i<node.getArguments().size(); i++) {
                lattice.linkArgToFunction(lattice.getValuesAtInputPoint(node.getArguments().get(i), context), f, targetCtx, i);
            }
            lattice.linkInstanceToFunction(f, targetCtx);
        }
    }
    @Override
    public void transferInvokeResult(InvokeResultNode node, Context context, LatticeUtil lattice) {
        Set<Value> funcArgs = lattice.getValuesAtInputPoint(node.getFunc(), context);
        if (node.isConstructor()) {
            Set<Value> thisValues = lattice.getValuesAtInputPoint(node.getAllocatedObject(), context);
            lattice.addValuesToOutputPoint(node.getResult(), thisValues, context);
        }
        for (Value funcval : funcArgs) {
            if (!(funcval instanceof FunctionValue)) {
                continue;
            }
            FunctionValue func = (FunctionValue)funcval;
            Context targetCtx = ContextWidening.widenContext(new InvokeContext(node.getCallsiteId(), context), this.depth);
            lattice.addValuesToOutputPoint(node.getResult(), lattice.getFunctionResults(func, targetCtx), context);
            lattice.addValuesToOutputPoint(node.getExceptionalResult(), lattice.getFunctionExceptionalResults(func, targetCtx), context);
        }
    }
    @Override
    public void transferLoadAndInvoke(LoadAndInvokeNode node, Context context, LatticeUtil lattice) {
        Callback<Key,Set<Value>> callback = lattice.getCallback();
        Set<Value> basevals = lattice.getValuesAtInputPoint(node.getBase(), context);
        List<Set<Value>> arguments = new ArrayList<Set<Value>>();
        for (int i=0; i<node.getArguments().size(); i++) {
            arguments.add(lattice.getValuesAtInputPoint(node.getArguments().get(i), context));
        }
        ObjectValue labelArg = new AllocObjectValue(node.getCallsiteId(), context);
        for (Value baseval : basevals) {
        	if (!(baseval instanceof ObjectValue))
        		continue;
            ObjectValue baseObj = (ObjectValue)baseval;
            Context targetCtx = ContextWidening.widenContext(new InvokeContext(node.getCallsiteId(), context), this.depth);
            for (ObjectValue proto : lattice.getPrototypeReachable(baseObj)) {
                Set<Value> funcs = new HashSet<Value>();
                funcs.addAll(callback.readableValueAt(new NamedPropertyKey(proto, node.getProperty())));
                funcs.addAll(callback.readableValueAt(proto.getDynamicStoreProperty()));
                lattice.addValuesToOutputPoint(node.getInvokedFunction(), funcs, context);
                for (Value funcval : funcs) {
                    if (!(funcval instanceof FunctionValue)) {
                        continue;
                    }
                    FunctionValue func = (FunctionValue)funcval;
                    
                    lattice.linkThisArgsToFunction(Collections.singleton(baseval), func, targetCtx);
                    lattice.linkLabelArgToFunction(Collections.singleton(labelArg), func, targetCtx);
                    for (int i=0; i<node.getArguments().size(); i++) {
                        lattice.linkArgToFunction(lattice.getValuesAtInputPoint(node.getArguments().get(i), context), func, targetCtx, i);
                    }
                    lattice.linkInstanceToFunction(func, targetCtx);
                }
            }
        }
    }

    /*
	@Override
	public void transferInvoke(Object callsite, Set<Value> thisArgs,
			List<Set<Value>> arguments, Context context, FunctionValue target,
			LatticeUtil lattice) {
		Context ctx = callingContext(callsite, context, lattice);
		lattice.linkThisArgsToFunction(thisArgs, target, ctx);
		lattice.linkLabelArgToFunction(Collections.singleton(new AllocObjectValue(callsite, context)), target, ctx);
		for (int i=0; i<arguments.size(); i++) {
			lattice.linkArgToFunction(arguments.get(i), target, ctx, i);
		}
		lattice.linkInstanceToFunction(target, ctx);
	}

	@Override
	public void transferInvokeResult(Object callsite, Context context,
			FunctionValue target, LatticeUtil lattice, OutputPoint result, OutputPoint exceptionalResult) {
		Context ctx = callingContext(callsite, context, lattice);

		// transfer normal returns
		lattice.addValuesToOutputPoint(result, lattice.getFunctionResults(target, ctx), context);

		// transfer exceptional returns
		lattice.addValuesToOutputPoint(exceptionalResult, lattice.getFunctionExceptionalResults(target, ctx), context);
	}
     */
//    private Context callingContext(Object callsiteId,
//            Context currentContext,
//            LatticeUtil lattice) {
//        return ContextWidening.widenContext(new InvokeContext(callsiteId, currentContext), this.depth);
//    }

}
