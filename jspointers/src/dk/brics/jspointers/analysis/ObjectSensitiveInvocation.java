package dk.brics.jspointers.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.InvokeContext;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.contexts.ObjectContext;
import dk.brics.jspointers.lattice.contexts.PrimitiveContext;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.values.AllocObjectValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.Callback;

public class ObjectSensitiveInvocation implements InvocationStrategy {

    private int depth;

    public ObjectSensitiveInvocation(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    private Context getContext(Value thisVal, IInvocationFlowNode node, Context context, LatticeUtil lattice) {
        Context targetCtx;
        if (!node.isConstructor() && node.isThisArgOmitted()) {
            if (context == MainContext.Instance) {
                // in global scope, just call with callsite
                // XXX this works pretty well, but it's kind of a hack?
                targetCtx = new InvokeContext(node.getCallsiteId(), MainContext.Instance);
            } else {
                // carry over current context when this arg is absent
                // for the persistent-redblack-tree benchmark, this makes a significant difference
                targetCtx = context;
            }
        } else if (!(thisVal instanceof ObjectValue)) {
            targetCtx = PrimitiveContext.Instance;
        } else {
            ObjectValue thisObj = (ObjectValue)thisVal;
            targetCtx = new ObjectContext(thisObj);
        }
        // TODO use cycle-ish detection instead of k-depth widening
        return ContextWidening.widenContext(targetCtx, this.depth);
    }

    @Override
    public void transferInvoke(InvokeNode node, Context context, LatticeUtil lattice) {
        Callback<Key,Set<Value>> callback = lattice.getCallback();
        Set<Value> thisArgs = lattice.getActualThisArgs(node, context); //callback.readableValueAt(node.getBase().getKey(context));
        Set<Value> funcArgs = lattice.getValuesAtInputPoint(node.getFunc(), context);
        ObjectValue labelArg = new AllocObjectValue(node.getCallsiteId(), context);
        for (Value funcval : funcArgs) {
            if (!(funcval instanceof FunctionValue)) {
                continue;
            }
            FunctionValue f = (FunctionValue)funcval;
            for (Value thisVal : thisArgs) {
                Context targetCtx = getContext(thisVal, node, context, lattice);
                lattice.linkThisArgsToFunction(Collections.singleton(thisVal), f, targetCtx);
                for (int i=0; i<node.getArguments().size(); i++) {
                    lattice.linkArgToFunction(lattice.getValuesAtInputPoint(node.getArguments().get(i), context), f, targetCtx, i);
                }
                lattice.linkInstanceToFunction(f, targetCtx);
                lattice.linkLabelArgToFunction(Collections.singleton(labelArg), f, targetCtx);
            }
        }
    }
    @Override
    public void transferInvokeResult(InvokeResultNode node, Context context, LatticeUtil lattice) {
        Set<Value> funcArgs = lattice.getValuesAtInputPoint(node.getFunc(), context);
        Set<Value> thisArgs = lattice.getActualThisArgs(node.getInvocation(), context);
        for (Value funcval : funcArgs) {
            if (!(funcval instanceof FunctionValue)) {
                continue;
            }
            FunctionValue f = (FunctionValue)funcval;
            for (Value thisVal : thisArgs) {
                Context targetCtx = getContext(thisVal, node.getInvocation(), context, lattice);
                if (thisVal instanceof ObjectValue && node.isConstructor()) {
                    ObjectValue thisObj = (ObjectValue)thisVal;
                    lattice.addValuesToOutputPoint(node.getResult(), Collections.singleton(thisObj), context);
                }
                lattice.addValuesToOutputPoint(node.getResult(), lattice.getFunctionResults(f, targetCtx), context);
                lattice.addValuesToOutputPoint(node.getExceptionalResult(), lattice.getFunctionExceptionalResults(f, targetCtx), context);
            }
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
            Context targetCtx = getContext(baseval, node, context, lattice);
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

}
