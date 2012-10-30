package dk.brics.jspointers.display;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.values.BooleanValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jsutil.MultiMap;

public class DisplayUtil {

    public static MultiMap<Key,Value> makeContextInsensitive(AnalysisResult<Key,Set<Value>> result) {
        MultiMap<Key,Value> output = new MultiMap<Key, Value>();
        for (Map.Entry<Key, Set<Value>> en : result.entrySet()) {
            Key key = en.getKey().makeContextInsensitive();
            for (Value value : en.getValue()) {
                output.add(key, value.makeContextInsensitive());
            }
        }
        return output;
    }

    private MultiMap<Key, Value> map;
    private DataflowGraph dataflow;

    public MultiMap<Key, Value> getMap() {
        return map;
    }
    public Set<Value> getValuesAtInputPoint(InputPoint ip) {
    	if (ip.getSources().size() == 1) {
    		return map.getView(ip.getSources().iterator().next().getKey(NullContext.Instance));
    	} else {
    		Set<Value> set = new HashSet<Value>();
    		for (OutputPoint op : ip.getSources()) {
    			set.addAll(map.getView(op.getKey(NullContext.Instance)));
    		}
    		return set;
    	}
    }

    public DisplayUtil(MultiMap<Key, Value> map, DataflowGraph dataflow) {
        this.map = map;
        this.dataflow = dataflow;
    }

    public Set<Value> getFunctionArgs(IInvocationFlowNode invoke) {
        if (invoke instanceof InvokeNode) {
            InvokeNode in = (InvokeNode) invoke;
            return getValuesAtInputPoint(in.getFunc());
        } else {
            Set<Value> result = new HashSet<Value>();
            LoadAndInvokeNode lin = (LoadAndInvokeNode) invoke;
            for (Value val : getValuesAtInputPoint(lin.getBase())) {
                ObjectValue obj = coerceToReadonlyObject(val);
                if (obj == null) {
                    continue;
                }
                for (ObjectValue proto : getPrototypeReachable(obj)) {
                    result.addAll(map.getView(new NamedPropertyKey(proto, lin.getProperty())));
                    result.addAll(map.getView(obj.getDynamicStoreProperty()));
                }
            }
            return result;
        }
    }

    public Set<ObjectValue> getPrototypeReachable(ObjectValue obj) {
        Set<ObjectValue> result = new HashSet<ObjectValue>();
        result.add(obj);
        LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>();
        queue.add(obj);
        while (!queue.isEmpty()) {
            ObjectValue o = queue.removeFirst();
            for (Value proto : map.getView(o.getPrototypeProperty())) {
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

    /**
     * Performs a <tt>ToObject</tt> type conversion, and returns an object
     * whose properties are identical to those of the resulting object, or
     * <tt>null</tt> if the conversion throws a TypeError.
     * <p/>
     * In particular, if the object is coerced to a Boolean, Number or String
     * object, the prototype for that object is returned since the real return
     * value has no properties of its own. This design allows us to omit the
     * creation of a new abstract object.
     * @param value any value (not null)
     * @return <tt>null</tt>, or the same ObjectValue as the argument, or the prototype of Boolean, Number or String
     */
    public ObjectValue coerceToReadonlyObject(Value value) {
        if (value instanceof ObjectValue) {
            return (ObjectValue)value;
        }
        else if (value instanceof BooleanValue) {
            return getNamedHarnessFunction("Boolean").getFunctionPrototype();
        }
        else if (value instanceof NumberValue) {
            return getNamedHarnessFunction("Number").getFunctionPrototype();
        }
        else if (value instanceof StringValue) {
            return getNamedHarnessFunction("String").getFunctionPrototype();
        }
        else {
            return null;
        }
    }

    public UserFunctionValue getNamedHarnessFunction(String name) {
        Function func = dataflow.getNamedHarnessFunctions().get(name);
        if (func == null) {
            throw new IllegalArgumentException("No harness function is named " + name);
        }
        return new UserFunctionValue(func, NullContext.Instance);
    }
}
