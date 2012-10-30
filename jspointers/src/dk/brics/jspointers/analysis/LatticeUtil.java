package dk.brics.jspointers.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.keys.FunctionInstanceKey;
import dk.brics.jspointers.lattice.keys.IntegerPropertyKey;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NativeArgKey;
import dk.brics.jspointers.lattice.keys.VariableKey;
import dk.brics.jspointers.lattice.values.ArgumentsArrayValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.Callback;

public class LatticeUtil {
    private Callback<Key, Set<Value>> callback;
    private DataflowGraph dataflow;

    public LatticeUtil(Callback<Key, Set<Value>> callback,
            DataflowGraph dataflow) {
        this.callback = callback;
        this.dataflow = dataflow;
    }

    public Callback<Key, Set<Value>> getCallback() {
        return callback;
    }
    public DataflowGraph getDataflow() {
        return dataflow;
    }
    
    public Set<Value> getValuesAtInputPoint(InputPoint ip, Context context) {
    	if (ip.getSources().size() == 1) {
    		return callback.readableValueAt(ip.getSources().iterator().next().getKey(context));
    	} else {
    		Set<Value> set = new HashSet<Value>();
    		for (OutputPoint op : ip.getSources()) {
    			set.addAll(callback.readableValueAt(op.getKey(context)));
    		}
    		return set;
    	}
    }
    
    public void linkThisArgsToFunction(Collection<? extends Value> thisArgs, FunctionValue function, Context ctx) {
        if (function instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)function;
            addValuesToVariable("this", uf.getFunction(), ctx, thisArgs);
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)function;
            addValuesToKey(nf.getThisArg(ctx), thisArgs);
        }
    }
    public void linkArgToFunction(Collection<? extends Value> argValues, FunctionValue function, Context ctx, int index) {
        if (function instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)function;
            Function func = uf.getFunction();
            // named parameters
            if (index < func.getParameterNames().size()) {
                addValuesToVariable(func.getParameterNames().get(index), uf.getFunction(), ctx, argValues);
            }
            // arguments array
            IntegerPropertyKey key = new ArgumentsArrayValue(func, ctx).getIntegerProperty();
            addValuesToKey(key, argValues);
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)function;
            addValuesToKey(new NativeArgKey(nf, ctx, index), argValues);
            addValuesToKey(nf.getDefaultArgs(ctx), argValues);
        }
    }
    public void linkDynamicArgToFunction(Collection<? extends Value> argValues, FunctionValue function, Context ctx) {
        if (function instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)function;
            Function func = uf.getFunction();
            for (String paramName : func.getParameterNames()) {
                addValuesToVariable(paramName, uf.getFunction(), ctx, argValues);
            }
            Key key = new ArgumentsArrayValue(func, ctx).getIntegerProperty();
            addValuesToKey(key, argValues);
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)function;
            addValuesToKey(nf.getDynamicArgs(ctx), argValues);
        }
    }
    public void linkLabelArgToFunction(Collection<? extends Value> labelArgs, FunctionValue function, Context ctx) {
        if (function instanceof UserFunctionValue) {
            // do nothing - user functions do not need label args
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)function;
            addValuesToKey(nf.getLabelArg(ctx), labelArgs);
        }
    }
    public void linkInstanceToFunction(FunctionValue function, Context ctx) {
        if (function instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)function;
            FunctionInstanceKey key = new FunctionInstanceKey(uf.getFunction(), ctx);
            addValueToKey(key, function);
        } else {
            // do nothing - native function do not need their function instance (which is unique anyway)
        }
    }
    public Set<Value> getFunctionResults(FunctionValue func, Context ctx) {
        if (func instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)func;
            Function function = uf.getFunction();
            return getValuesAtInputPoint(dataflow.getNormalReturns().get(function).getValue(), ctx);
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)func;
            return callback.readableValueAt(nf.getResult(ctx));
        }
    }
    public Set<Value> getFunctionExceptionalResults(FunctionValue func, Context ctx) {
        if (func instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)func;
            Function function = uf.getFunction();
            return getValuesAtInputPoint(dataflow.getExceptionalReturns().get(function).getValue(), ctx);
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)func;
            return callback.readableValueAt(nf.getExceptionalResult(ctx));
        }
    }
    public void addValuesToKey(Key key, Collection<? extends Value> values) {
        boolean changed = callback.modifiableValueAt(key).addAll(values);
        if (changed) {
            callback.markChanged(key);
        }
    }
    public void addValueToKey(Key key, Value value) {
        boolean changed = callback.modifiableValueAt(key).add(value);
        if (changed) {
            callback.markChanged(key);
        }
    }
    public void addValuesToVariable(String varname, Scope scope, Context ctx, Collection<? extends Value> valuesToAdd) {
        VariableKey key = new VariableKey(varname, scope, ctx);
        boolean changed = callback.modifiableValueAt(key).addAll(valuesToAdd);
        if (changed) {
            callback.markChanged(key);
        }
    }

    /** Returns the set of objects reachable by following prototype pointers, including the argument itself */
    public Set<ObjectValue> getPrototypeReachable(ObjectValue obj) {
        Set<ObjectValue> result = new HashSet<ObjectValue>();
        result.add(obj);
        LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>();
        queue.add(obj);
        while (!queue.isEmpty()) {
            ObjectValue o = queue.removeFirst();
            for (Value proto : callback.readableValueAt(o.getPrototypeProperty())) {
                if (!(proto instanceof ObjectValue)) {
                    continue;
                }
                ObjectValue protoObject = (ObjectValue)proto;
                if (result.add(protoObject)) {
                    queue.add(protoObject);
                }
            }
        }
        //						System.out.printf("%d objects reachable through prototype\n", result.size());
        return result;
    }

    public boolean convertibleToNumber(Set<Value> values) {
        for (Value value : values) {
            if (value instanceof NumberValue || value instanceof ObjectValue) {
                return true; // TODO also booleans?
            }
        }
        return false;
    }
    public boolean convertibleToString(Set<Value> values) {
        for (Value value : values) {
            if (value instanceof StringValue || value instanceof ObjectValue) {
                return true;
            }
        }
        return false;
    }
    public void addValuesToOutputPoint(OutputPoint op, Collection<? extends Value> values, Context context) {
    	addValuesToKey(op.getKey(context), values);
//        for (InputPoint ip : op.getDestinations()) {
//            addValuesToKey(ip.getKey(context), values);
//        }
    }

    /**
     * Returns the set of <tt>this</tt> arguments sent with the given invocation node.
     * If an explicit this arg is omitted, the global object is taken.
     */
    public Set<Value> getActualThisArgs(IInvocationFlowNode node, Context context) {
        if (node instanceof InvokeNode) {
            InvokeNode invoke = (InvokeNode)node;
            if (!invoke.isConstructor() && invoke.isThisArgOmitted()) {
                return Collections.<Value>singleton(GlobalObjectValue.Instance);
            } else {
            	return getValuesAtInputPoint(invoke.getBase(), context);
            }
        } else {
        	return getValuesAtInputPoint(node.getBase(), context);
        }
    }

    public UserFunctionValue getNamedHarnessFunction(String name) {
        Function func = dataflow.getNamedHarnessFunctions().get(name);
        if (func == null) {
            throw new IllegalArgumentException("No harness function is named " + name);
        }
        return new UserFunctionValue(func, MainContext.Instance);
    }

}
