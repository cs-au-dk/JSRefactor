package dk.brics.jspointers.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.CoerceToObject;
import dk.brics.jspointers.dataflow.CoerceToPrimitive;
import dk.brics.jspointers.dataflow.ConstNode;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.FlowNodeVisitor;
import dk.brics.jspointers.dataflow.FunctionInstanceNode;
import dk.brics.jspointers.dataflow.GlobalExceptionNode;
import dk.brics.jspointers.dataflow.IdentityNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InitializeNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InterscopeIdentityNode;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadDirectNode;
import dk.brics.jspointers.dataflow.LoadDynamicNode;
import dk.brics.jspointers.dataflow.LoadNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.dataflow.PlusNode;
import dk.brics.jspointers.dataflow.ReturnNode;
import dk.brics.jspointers.dataflow.SetPrototype;
import dk.brics.jspointers.dataflow.StoreDynamicNode;
import dk.brics.jspointers.dataflow.StoreIfPresentNode;
import dk.brics.jspointers.dataflow.StoreNode;
import dk.brics.jspointers.dataflow.StubNode;
import dk.brics.jspointers.dataflow.VarReadGlobalNode;
import dk.brics.jspointers.dataflow.VarReadInterscopeNode;
import dk.brics.jspointers.dataflow.VarReadNode;
import dk.brics.jspointers.dataflow.VarWriteGlobalNode;
import dk.brics.jspointers.dataflow.VarWriteInterscopeNode;
import dk.brics.jspointers.dataflow.VarWriteNode;
import dk.brics.jspointers.lattice.contexts.CoercionContext;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.DOMInvokeContext;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.keys.BoundArgKey;
import dk.brics.jspointers.lattice.keys.BoundDefaultArgKey;
import dk.brics.jspointers.lattice.keys.BoundDynamicArgKey;
import dk.brics.jspointers.lattice.keys.BoundTargetKey;
import dk.brics.jspointers.lattice.keys.BoundThisArgKey;
import dk.brics.jspointers.lattice.keys.DefaultPropertyKey;
import dk.brics.jspointers.lattice.keys.DynamicStorePropertyKey;
import dk.brics.jspointers.lattice.keys.FunctionInstanceKey;
import dk.brics.jspointers.lattice.keys.IntegerPropertyKey;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.KeyVisitor;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.keys.NativeArgKey;
import dk.brics.jspointers.lattice.keys.NativeDefaultArgsKey;
import dk.brics.jspointers.lattice.keys.NativeDynamicArgsKey;
import dk.brics.jspointers.lattice.keys.NativeExceptionalResultKey;
import dk.brics.jspointers.lattice.keys.NativeLabelArgKey;
import dk.brics.jspointers.lattice.keys.NativeResultKey;
import dk.brics.jspointers.lattice.keys.NativeThisArgKey;
import dk.brics.jspointers.lattice.keys.OuterFunctionPropertyKey;
import dk.brics.jspointers.lattice.keys.OutputPointKey;
import dk.brics.jspointers.lattice.keys.PrototypePropertyKey;
import dk.brics.jspointers.lattice.keys.VariableKey;
import dk.brics.jspointers.lattice.values.AllocObjectValue;
import dk.brics.jspointers.lattice.values.ArgumentsArrayValue;
import dk.brics.jspointers.lattice.values.CoercedPrimitiveObjectValue;
import dk.brics.jspointers.lattice.values.FunctionPrototypeValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NativeErrorValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.NullValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.PrimitiveValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UndefinedValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.DOMNative;
import dk.brics.jspointers.lattice.values.natives.EvalNative;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindResultNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jspointers.lattice.values.natives.FunctionNative;
import dk.brics.jspointers.lattice.values.natives.NativeFunctionVisitor;
import dk.brics.jspointers.lattice.values.natives.ObjectCreateNative;
import dk.brics.jspointers.lattice.values.natives.ObjectGetPrototypeOfNative;
import dk.brics.jspointers.lattice.values.natives.ObjectNative;
import dk.brics.jspointers.solver.Analysis;
import dk.brics.jspointers.solver.Callback;

/**
 * The {@link Analysis} implementation. This contains all transfer functions
 * and initial dependency computations.
 * 
 * @author Asger
 */
public final class JSAnalysis implements Analysis<TransferNode, Key, Set<Value>> {
    private final DataflowGraph dataflow;
    private InvocationStrategy contextSensitivity;

    public JSAnalysis(DataflowGraph dataflow, InvocationStrategy contextSensitivity) {
        this.dataflow = dataflow;
        this.contextSensitivity = contextSensitivity;
    }

    @Override
    public Set<Value> emptyValue() {
        return Collections.emptySet();
    }

    @Override
    public Set<Value> clone(Set<Value> value) {
        return new HashSet<Value>(value);
    }

    @Override
    public Set<Value> bottom(Key k) {
        final Set<Value> result = new HashSet<Value>();
        k.apply(new KeyVisitor() {
            @Override
            public void caseOuterFunctionProperty(OuterFunctionPropertyKey key) {
            }
            @Override
            public void caseDefaultProperty(DefaultPropertyKey key) {
                if (key.getObject() instanceof FunctionValue) {
                    // add the .prototype from caseNamedProperty
                    FunctionValue func = (FunctionValue)key.getObject();
                    result.add(func.getFunctionPrototype());
                } else if (key.getObject() instanceof FunctionPrototypeValue) {
                    // add the .constructor from caseNamedProperty
                    FunctionPrototypeValue proto = (FunctionPrototypeValue)key.getObject();
                    result.add(proto.getFunction());
                }
            }
            @Override
            public void caseOutputPoint(OutputPointKey key) {
            }
            @Override
            public void caseIntegerProperty(IntegerPropertyKey key) {
            }
            @Override
            public void caseNamedProperty(NamedPropertyKey key) {
                // note: Everything added here should also be added in caseDefaultProperty
                if (key.getObject() instanceof FunctionValue) {
                    FunctionValue func = (FunctionValue)key.getObject();
                    if (key.getProperty().equals("prototype")) {
                        result.add(func.getFunctionPrototype());
                    } else if (key.getProperty().equals("length")) {
                        result.add(NumberValue.Instance);
                    }
                }
                else if (key.getObject() instanceof FunctionPrototypeValue && key.getProperty().equals("constructor")) {
                    FunctionPrototypeValue proto = (FunctionPrototypeValue)key.getObject();
                    result.add(proto.getFunction());
                }
            }
            @Override
            public void caseVariableKey(VariableKey key) {
                result.add(UndefinedValue.Instance);
                Scope scope = key.getScope();
                if (scope instanceof Function && ((Function)scope).getOuterFunction() != null && key.getVarname().equals("arguments")) {
                    result.add(new ArgumentsArrayValue((Function)scope, key.getContext()));
                }
            }
            @Override
            public void caseDynamicStoreProperty(DynamicStorePropertyKey key) {
            }
            @Override
            public void casePrototypePropertyKey(PrototypePropertyKey key) {
                if (key.getObject() instanceof FunctionValue) {
                    result.add(FunctionNative.Instance.getFunctionPrototype());
                }
                else if (key.getObject() instanceof ArgumentsArrayValue) {
                    result.add(ObjectNative.Instance.getFunctionPrototype());
                }
                else if (key.getObject() instanceof CoercedPrimitiveObjectValue) {
                	CoercedPrimitiveObjectValue obj = (CoercedPrimitiveObjectValue)key.getObject();
                	PrimitiveValue type = obj.getPrimitive();
                	Function func;
                	switch (type.getBasicType()) {
                	case BOOLEAN:
                		func = dataflow.getNamedHarnessFunctions().get("Boolean");
                		break;
                	case NUMBER:
                		func = dataflow.getNamedHarnessFunctions().get("Number");
                		break;
                	case STRING:
                		func = dataflow.getNamedHarnessFunctions().get("String");
                		break;
                	default:
                		throw new RuntimeException("Not a meaningful primitive type: " + type);
                	}
                	result.add(new FunctionPrototypeValue(new UserFunctionValue(func, MainContext.Instance)));
                }
                else if (key.getObject() instanceof FunctionPrototypeValue) {
                	result.add(ObjectNative.Instance.getFunctionPrototype());
                }
            }
            @Override
            public void caseNativeThisArg(NativeThisArgKey key) {
            }
            @Override
            public void caseNativeArg(NativeArgKey key) {
            }
            @Override
            public void caseNativeResult(NativeResultKey key) {
            }
            @Override
            public void caseNativeDefaultArgs(NativeDefaultArgsKey key) {
            }
            @Override
            public void caseNativeDynamicArgs(NativeDynamicArgsKey key) {
            }
            @Override
            public void caseNativeLabelArg(NativeLabelArgKey key) {
            }
            @Override
            public void caseNativeExceptionalResult(NativeExceptionalResultKey key) {
            }
            @Override
            public void caseFunctionInstance(FunctionInstanceKey key) {
                if (dataflow.getEntryFunctions().contains(key.getFunction())) {
                    result.add(new UserFunctionValue(key.getFunction(), key.getContext()));
                }
            }
			@Override
			public void caseBoundTarget(BoundTargetKey key) {
			}
			@Override
			public void caseBoundArg(BoundArgKey key) {
			}
			@Override
			public void caseBoundThisArg(BoundThisArgKey key) {
			}
			@Override
			public void caseBoundDynamicArg(BoundDynamicArgKey key) {
			}
			@Override
			public void caseBoundDefaultArg(BoundDefaultArgKey key) {
			}
        });
        return result;
    }

    @Override
    public Iterable<? extends TransferNode> initialNodes() {
        // the native initializer and the main function's instance node are the two initial nodes
        List<TransferNode> result = new LinkedList<TransferNode>();
        result.add(InvokeDOMFunctionTransfer.Instance);
        result.add(new FlowNodeContextPair(dataflow.getInitializer(), MainContext.Instance));
        for (Function entry : dataflow.getEntryFunctions()) {
            result.add(new FlowNodeContextPair(
                    dataflow.getFunctionInstanceFlowNodes().get(entry),
                    MainContext.Instance));
        }
        return result;
    }

    @Override
    public void transfer(TransferNode t, final Callback<Key, Set<Value>> callback) {
        final LatticeUtil lattice = new LatticeUtil(callback, dataflow);
        class Inner {
            public void transferFlowNode(FlowNode flowNode, final Context flowNodeContext) {
                flowNode.apply(new FlowNodeVisitor() {
                    @Override
                    public void caseInterscopeIdentity(InterscopeIdentityNode node) {
                        Set<Context> contexts = getOuterContexts(lattice.getValuesAtInputPoint(node.getFunctionInstance(), flowNodeContext), node.getDepth());
                        for (Context context : contexts) {
                            addValuesToOutputPoint(node.getResult(), lattice.getValuesAtInputPoint(node.getForeignInputPoint(), context));
                        }
                    }
                    @Override
                    public void caseStub(StubNode node) {
                        // do nothing
                    }
                    @SuppressWarnings("unused")
                    private Function findFunction(FlowNode node) {
                        // only for debugging
                        Set<FlowNode> visited = new HashSet<FlowNode>();
                        LinkedList<FlowNode> queue = new LinkedList<FlowNode>();
                        queue.add(node);
                        visited.add(node);
                        while (!queue.isEmpty()) {
                            FlowNode n = queue.removeFirst();
                            if (n instanceof FunctionInstanceNode) {
                                return ((FunctionInstanceNode)n).getFunction();
                            }
                            for (InputPoint ip : n.getInputPoints()) {
                                for (OutputPoint op : ip.getSources()) {
                                    if (visited.add(op.getFlowNode())) {
                                        queue.add(op.getFlowNode());
                                    }
                                }
                            }
                        }
                        throw new RuntimeException("FlowNode did not belong to a function: " + node);
                    }
                    @Override
                    public void caseCoerceToObject(CoerceToObject node) {
                    	Set<ObjectValue> values = new HashSet<ObjectValue>(); 
                    	for (Value arg : lattice.getValuesAtInputPoint(node.getArgument(), flowNodeContext)) {
                    		if (arg instanceof NullValue || arg instanceof UndefinedValue)
                    			continue; // throws exception at runtime
                    		if (arg instanceof PrimitiveValue) {
                    			values.add(new CoercedPrimitiveObjectValue((PrimitiveValue)arg));
                    		} else {
                    			values.add((ObjectValue)arg);
                    		}
                    	}
                    	addValuesToOutputPoint(node.getResult(), values);
                    }
                    @Override
                    public void caseCoerceToPrimitive(CoerceToPrimitive node) {
                        Set<Value> baseValues = lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        for (Value base : baseValues) {
                            if (!(base instanceof ObjectValue)) {
                                continue;
                            }
                            ObjectValue baseObj = (ObjectValue)base;
                            for (ObjectValue prototype : getPrototypeReachable(baseObj)) {
                                coerceToPrimitive(baseObj, callback.readableValueAt(new NamedPropertyKey(prototype, "toString")), node.getExceptionalResult());
                                coerceToPrimitive(baseObj, callback.readableValueAt(new NamedPropertyKey(prototype, "valueOf")), node.getExceptionalResult());
                                coerceToPrimitive(baseObj, callback.readableValueAt(prototype.getDynamicStoreProperty()), node.getExceptionalResult());
                            }
                        }
                    }
                    private void coerceToPrimitive(ObjectValue thisArg, Set<Value> functions, OutputPoint op) {
                        for (Value funcval : functions) {
                            if (!(funcval instanceof FunctionValue)) {
                                continue;
                            }
                            FunctionValue func = (FunctionValue)funcval;
                            linkInstanceToFunction(func, CoercionContext.Instance);
                            linkThisArgsToFunction(Collections.singleton(thisArg), func, CoercionContext.Instance);
                            // TODO separate CoerceToPrimitive into call and result?
                            addValuesToOutputPoint(op, getFunctionExceptionalResults(func, CoercionContext.Instance));
                        }
                    }
                    @Override
                    public void caseIdentity(IdentityNode node) {
                        addValuesToOutputPoint(node.getResult(), lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext));
                    }
                    @Override
                    public void caseVarReadGlobal(VarReadGlobalNode node) {
                        addValuesToOutputPoint(node.getResult(), callback.readableValueAt(new NamedPropertyKey(GlobalObjectValue.Instance, node.getVarName())));
                        addValuesToOutputPoint(node.getResult(), callback.readableValueAt(GlobalObjectValue.Instance.getDynamicStoreProperty()));
                        addValuesToOutputPoint(node.getResult(), Collections.singleton(UndefinedValue.Instance));
                    }
                    @Override
                    public void caseVarWriteGlobal(VarWriteGlobalNode node) {
                        addValuesToKey(new NamedPropertyKey(GlobalObjectValue.Instance, node.getVarName()), 
                        		lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext));
                    }
                    @Override
                    public void caseConst(ConstNode node) {
                        addValuesToOutputPoint(node.getResult(), Collections.singleton(node.getValue()));
                    }
                    @Override
                    public void caseAlloc(AllocNode node) {
                        AllocObjectValue value = new AllocObjectValue(node.getAllocationSite(), flowNodeContext);
                        addValuesToOutputPoint(node.getResult(), Collections.singleton(value));
                    }
                    @Override
                    public void caseInvoke(InvokeNode node) {
                        contextSensitivity.transferInvoke(node, flowNodeContext, lattice);
                    }
                    @Override
                    public void caseLoadAndInvoke(LoadAndInvokeNode node) {
                        contextSensitivity.transferLoadAndInvoke(node, flowNodeContext, lattice);
                    }
                    @Override
                    public void caseInvokeResult(InvokeResultNode node) {
                        contextSensitivity.transferInvokeResult(node, flowNodeContext, lattice);
                    }

                    
                    @Override
                    public void caseReturn(ReturnNode node) {
                    }
                    private UserFunctionValue getNamedHarnessFunction(String name) {
                        return lattice.getNamedHarnessFunction(name);
                    }
                    @Override
                    public void caseSetPrototype(SetPrototype node) {
                    	Set<Value> protos = lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                    	for (Value base : lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext)) {
                    		if (!(base instanceof ObjectValue))
                    			continue;
                    		ObjectValue obj = (ObjectValue) base;
                    		lattice.addValuesToKey(obj.getPrototypeProperty(), protos);
                    	}
                    }
                    @Override
                    public void caseLoadDirect(LoadDirectNode node) {
                        Set<Value> baseValues = 
                        	lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext);
                        Set<Value> tmp = new HashSet<Value>();
                        tmp.add(UndefinedValue.Instance);
                        for (Value base : baseValues) {
                        	if (!(base instanceof ObjectValue))
                        		continue;
                        	ObjectValue obj = (ObjectValue)base;
                            NamedPropertyKey prop = new NamedPropertyKey(obj, node.getProperty());
                            tmp.addAll(callback.readableValueAt(prop));
                            tmp.addAll(callback.readableValueAt(obj.getDynamicStoreProperty()));
                        }
                        addValuesToOutputPoint(node.getResult(), tmp);
                    }
                    @Override
                    public void caseLoad(LoadNode node) {
                        Set<Value> baseValues = 
                        	lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext);
                        Set<Value> tmp = new HashSet<Value>();
                        tmp.add(UndefinedValue.Instance);
                        for (Value base : baseValues) {
                        	if (!(base instanceof ObjectValue))
                        		continue;
                        	ObjectValue baseObj = (ObjectValue)base;
                          final Set<ObjectValue> hosts;
                          if (base instanceof FunctionValue && node.getProperty().equals("prototype")) {
                            // functions always have a "prototype" property, so ignore the prototype chain in this case
                            // (it cannot be deleted because [Configurable]=false when created)
                            hosts = Collections.singleton(baseObj);
                          } else {
                            hosts = getPrototypeReachable(baseObj);
                          }
                          for (ObjectValue obj : hosts) { 
                              NamedPropertyKey prop = new NamedPropertyKey(obj, node.getProperty());
                              tmp.addAll(callback.readableValueAt(prop));
                              tmp.addAll(callback.readableValueAt(obj.getDynamicStoreProperty()));
                          }
                        }
                        addValuesToOutputPoint(node.getResult(), tmp);
                    }
                    @Override
                    public void casePlus(PlusNode node) {
                        Set<Value> args = 
                        	lattice.getValuesAtInputPoint(node.getArgument(), flowNodeContext);
                        boolean containsString = convertibleToString(args);
                        boolean containsNumber = args.contains(NumberValue.Instance);
                        Set<Value> result = callback.modifiableValueAt(node.getResult().getKey(flowNodeContext));
                        boolean changed = false;
                        if (containsString) {
                            changed |= result.add(StringValue.Instance);
                        }
                        if (containsNumber) {
                            changed |= result.add(NumberValue.Instance);
                        }
                        if (changed) {
                            callback.markChanged(node.getResult().getKey(flowNodeContext));
                        }
                    }
                    @Override
                    public void caseStoreIfPresent(StoreIfPresentNode node) {
                        // TODO: More precise StoreIfPresent. This is just copied from caseStore
                        Set<Value> baseValues = 
                        	lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext);
                        Set<Value> rhsValues = 
                        	lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        for (Value value : baseValues) {
                            if (!(value instanceof ObjectValue)) {
                                continue;
                            }
                            ObjectValue obj = (ObjectValue)value;
                            addValuesToKey(new NamedPropertyKey(obj, node.getProperty()), rhsValues);
                            addValuesToKey(obj.getDefaultProperty(), rhsValues);
                        }
                    }
                    @Override
                    public void caseStore(StoreNode node) {
                        Set<Value> baseValues = 
                        	lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext);
                        Set<Value> rhsValues = 
                        	lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        for (Value value : baseValues) {
                            if (!(value instanceof ObjectValue)) {
                                continue;
                            }
                            ObjectValue obj = (ObjectValue)value;
                            addValuesToKey(new NamedPropertyKey(obj, node.getProperty()), rhsValues);
                            addValuesToKey(obj.getDefaultProperty(), rhsValues);
                        }
                    }
                    @Override
                    public void caseVarRead(VarReadNode node) {
                        Set<Value> varValues = callback.readableValueAt(new VariableKey(node.getVarName(), node.getScope(), flowNodeContext));
                        addValuesToOutputPoint(node.getResult(), varValues);
                    }
                    @Override
                    public void caseVarWrite(VarWriteNode node) {
                        Set<Value> rhsValues = lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        addValuesToVariable(node.getVarName(), node.getScope(), flowNodeContext, rhsValues);
                    }
                    private Set<Context> getOuterContexts(Set<Value> functionInstance, int depth) {
                        if (depth < 1) {
                            throw new IllegalArgumentException("depth must be >= 1");
                        }
                        Set<UserFunctionValue> a = new HashSet<UserFunctionValue>();
                        Set<UserFunctionValue> b = new HashSet<UserFunctionValue>();
                        for (Value init : functionInstance) {
                            if (init instanceof UserFunctionValue) {
                                a.add((UserFunctionValue)init);
                            }
                        }
                        for (int i=0; i<depth-1; i++) {
                            for (UserFunctionValue uf : a) {
                                Set<Value> outervals = callback.readableValueAt(uf.getOuterFunction());
                                for (Value outerval : outervals) {
                                    if (!(outerval instanceof UserFunctionValue)) {
                                        continue;
                                    }
                                    UserFunctionValue outer = (UserFunctionValue)outerval;
                                    b.add(outer);
                                }
                            }
                            Set<UserFunctionValue> tmp = a;
                            a = b;
                            b = tmp;
                            b.clear();
                        }
                        // now find the execution contexts of these functions
                        Set<Context> result = new HashSet<Context>();
                        for (UserFunctionValue uf : a) {
                            result.add(uf.getContext());
                        }
                        return result;
                    }
                    @Override
                    public void caseVarReadInterscope(VarReadInterscopeNode node) {
                        Set<Value> result = new HashSet<Value>();
                        Set<Context> contexts = getOuterContexts(
                        		lattice.getValuesAtInputPoint(node.getFunctionInstance(), flowNodeContext), node.getDepth());
                        for (Context srcCtx : contexts) {
                            result.addAll(callback.readableValueAt(new VariableKey(node.getVarName(), node.getScope(), srcCtx)));
                        }
                        addValuesToOutputPoint(node.getResult(), result);
                    }
                    @Override
                    public void caseVarWriteInterscope(VarWriteInterscopeNode node) {
                        Set<Context> contexts = getOuterContexts(
                        		lattice.getValuesAtInputPoint(node.getFunctionInstance(), flowNodeContext), node.getDepth());
                        Set<Value> rhsValues = 
                        		lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        for (Context dstCtx : contexts) {
                            addValuesToVariable(node.getVarName(), node.getScope(), dstCtx, rhsValues);
                        }
                    }
                    @Override
                    public void caseLoadDynamic(LoadDynamicNode node) {
                        // result := base[property]
                        Set<Value> prop = lattice.getValuesAtInputPoint(node.getProperty(), flowNodeContext);
                        Set<Value> values = new HashSet<Value>();
                        values.add(UndefinedValue.Instance);
                        boolean canBeString = convertibleToString(prop);
                        boolean canBeNumber = convertibleToNumber(prop);
                        for (Value base : lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext)) {
                            if (!(base instanceof ObjectValue)) {
                                continue;
                            }
                            ObjectValue baseObj = (ObjectValue)base;
                            for (ObjectValue obj : getPrototypeReachable(baseObj)) {
                                if (canBeString) {
                                    values.addAll(callback.readableValueAt(obj.getDefaultProperty()));
                                }
                                else if (canBeNumber) {
                                    values.addAll(callback.readableValueAt(obj.getIntegerProperty()));
                                }
                            }
                        }
                        addValuesToOutputPoint(node.getResult(), values);
                    }
                    @Override
                    public void caseStoreDynamic(StoreDynamicNode node) {
                        Set<Value> prop = lattice.getValuesAtInputPoint(node.getProperty(), flowNodeContext);
                        Set<Value> values = lattice.getValuesAtInputPoint(node.getValue(), flowNodeContext);
                        boolean canBeString = convertibleToString(prop);
                        boolean canBeNumber = convertibleToNumber(prop);
                        Set<ObjectValue> baseObjects = new HashSet<ObjectValue>();
                        for (Value base : lattice.getValuesAtInputPoint(node.getBase(), flowNodeContext)) {
                            if (!(base instanceof ObjectValue)) {
                                continue;
                            }
                            ObjectValue obj = (ObjectValue)base;
                            baseObjects.add(obj);
                            if (canBeString) {
                                addValuesToKey(obj.getDynamicStoreProperty(), values);
                            }
                            if (canBeString || canBeNumber) {
                                addValuesToKey(obj.getIntegerProperty(), values);
                            }
                            addValuesToKey(obj.getDefaultProperty(), values);
                        }
                    }
                    @Override
                    public void caseInitialize(InitializeNode node) {
                        // NativeErrorValue.[[Prototype]] = Error.prototype
                        Function errorFunc = dataflow.getNamedHarnessFunctions().get("Error");
                        addValueToKey(NativeErrorValue.Instance.getPrototypeProperty(), new UserFunctionValue(errorFunc, MainContext.Instance).getFunctionPrototype());
                        
                        // DOM.anything = DOM
                        addValueToKey(DOMNative.Instance.getDynamicStoreProperty(), DOMNative.Instance);
                        addValueToKey(DOMNative.Instance.getDefaultProperty(), DOMNative.Instance);
                        addValueToKey(DOMNative.Instance.getIntegerProperty(), DOMNative.Instance);

                        addValueToKey(new NamedPropertyKey(new CoercedPrimitiveObjectValue(StringValue.Instance), "length"), NumberValue.Instance);
                    }
                    @Override
                    public void caseInitializeFunction(InitializeFunctionNode node) {
                        Set<Value> outerInstances = lattice.getValuesAtInputPoint(node.getOuterFunction(), flowNodeContext);
                        UserFunctionValue inner = new UserFunctionValue(node.getFunction(), flowNodeContext);
                        addValuesToKey(inner.getOuterFunction(), outerInstances);
                        addValuesToOutputPoint(node.getResult(), Collections.singleton(inner));
                    }
                    @Override
                    public void caseFunctionInstance(FunctionInstanceNode node) {
                        Set<Value> values = callback.readableValueAt(new FunctionInstanceKey(node.getFunction(), flowNodeContext));
                        addValuesToOutputPoint(node.getResult(), values);
                    }

                    @Override
                    public void caseGlobalException(GlobalExceptionNode node) {
                        addValuesToOutputPoint(node.getResult(), Collections.singleton(NativeErrorValue.Instance));
                    }

                    private void addValuesToOutputPoint(OutputPoint op, Collection<? extends Value> values) {
                        lattice.addValuesToOutputPoint(op, values, flowNodeContext);
                    }


                });
            } // end of transferFlowNode
            
            public void transferNative(NativeTransfer t) {
            	final Context invokeContext = t.getContext();
            	t.getNativeFunction().apply(new NativeFunctionVisitor() {
                    @Override
                    public void caseObject(ObjectNative objfunc) {
                        Set<Value> result = callback.modifiableValueAt(objfunc.getResult(invokeContext));
                        boolean changed = false;
                        changed |= result.addAll(callback.readableValueAt(new NativeArgKey(objfunc, invokeContext, 0)));
                        changed |= result.addAll(callback.readableValueAt(objfunc.getLabelArg(invokeContext)));
                        if (changed) {
                            callback.markChanged(objfunc.getResult(invokeContext));
                        }
                    }
                    @Override
                    public void caseFunction(FunctionNative value) {
                    	// do nothing
                    }
                    @Override
                    public void caseEval(EvalNative value) {
                    	// TODO: Can we return a "best guess" value here?
                    }
                    @Override
                    public void caseFunctionCall(FunctionCallNative callfunc) {
                        // Format: func.call(thisArg, arg0, arg1, arg2, ...)
                        Set<Value> funcvals = callback.readableValueAt(callfunc.getThisArg(invokeContext));
                        Set<Value> firstArgs = callback.readableValueAt(new NativeArgKey(callfunc, invokeContext, 0));
                        //								Set<Value> defaultArgs = callback.readableValueAt(callfunc.getDefaultArgs(flowNodeContext));
                        Set<Value> dynamicArgs = callback.readableValueAt(callfunc.getDynamicArgs(invokeContext));
                        Set<Value> labelArgs = callback.readableValueAt(callfunc.getLabelArg(invokeContext));
                        Set<Value> result = callback.modifiableValueAt(callfunc.getResult(invokeContext));
                        Set<Value> exresult = callback.modifiableValueAt(callfunc.getExceptionalResult(invokeContext));
                        boolean resultChanged = false;
                        boolean exceptionsChanged = false;
                        for (Value value : funcvals) {
                            if (!(value instanceof FunctionValue)) {
                                continue;
                            }
                            FunctionValue func = (FunctionValue)value;
                            linkThisArgsToFunction(firstArgs, func, invokeContext);
                            linkThisArgsToFunction(dynamicArgs, func, invokeContext);
                            linkLabelArgToFunction(labelArgs, func, invokeContext);
                            linkInstanceToFunction(func, invokeContext);
                            Set<Value> argumentValues;
                            int index = 0;
                            // note: termination here is NOT obvious!
                            // it terminates because we only assign arguments to lower indices
                            do {
                                NativeArgKey key = new NativeArgKey(callfunc, invokeContext, index+1);
                                argumentValues = callback.readableValueAt(key);
                                linkArgToFunction(argumentValues, func, invokeContext, index);
                                index++;
                            } while (!argumentValues.isEmpty());

                            // arguments to "call" with unknown index should be given as arguments to "func" with unknown index
                            linkDynamicArgToFunction(dynamicArgs, func, invokeContext);

                            resultChanged |= result.addAll(getFunctionResults(func, invokeContext));

                            exceptionsChanged |= exresult.addAll(getFunctionExceptionalResults(func, invokeContext));
                        }
                        if (resultChanged) {
                            callback.markChanged(callfunc.getResult(invokeContext));
                        }
                        if (exceptionsChanged) {
                            callback.markChanged(callfunc.getExceptionalResult(invokeContext));
                        }
                    }
                    @Override
                    public void caseFunctionApply(FunctionApplyNative applyfunc) {
                        // Format: func.apply(thisArg, argumentsArray)
                        Set<Value> funcvals = callback.readableValueAt(applyfunc.getThisArg(invokeContext));
                        Set<Value> thisArgs = callback.readableValueAt(new NativeArgKey(applyfunc, invokeContext, 0));
                        Set<Value> arrayArgs = callback.readableValueAt(new NativeArgKey(applyfunc, invokeContext, 1));
                        Set<Value> dynamicArgs = callback.readableValueAt(applyfunc.getDynamicArgs(invokeContext));
                        Set<Value> labelArgs = callback.readableValueAt(applyfunc.getLabelArg(invokeContext));
                        Set<Value> result = callback.modifiableValueAt(applyfunc.getResult(invokeContext));
                        Set<Value> exresult = callback.modifiableValueAt(applyfunc.getExceptionalResult(invokeContext));
                        boolean resultChanged = false;
                        boolean exceptionsChanged = false;
                        for (Value funcval : funcvals) {
                            if (!(funcval instanceof FunctionValue)) {
                                continue;
                            }
                            FunctionValue func = (FunctionValue)funcval;
                            linkThisArgsToFunction(thisArgs, func, invokeContext);
                            linkThisArgsToFunction(dynamicArgs, func, invokeContext);
                            linkLabelArgToFunction(labelArgs, func, invokeContext);
                            linkInstanceToFunction(func, invokeContext);
                            for (Value array : arrayArgs) {
                                if (!(array instanceof ObjectValue)) {
                                    continue;
                                }
                                ObjectValue arrayObj = (ObjectValue)array;
                                linkDynamicArgToFunction(callback.readableValueAt(arrayObj.getIntegerProperty()), func, invokeContext);
                            }
                            for (Value array : dynamicArgs) {
                                if (!(array instanceof ObjectValue)) {
                                    continue;
                                }
                                ObjectValue arrayObj = (ObjectValue)array;
                                linkDynamicArgToFunction(callback.readableValueAt(arrayObj.getIntegerProperty()), func, invokeContext);
                            }

                            resultChanged |= result.addAll(getFunctionResults(func, invokeContext));

                            exceptionsChanged |= exresult.addAll(getFunctionExceptionalResults(func, invokeContext));
                        }
                        if (resultChanged) {
                            callback.markChanged(applyfunc.getResult(invokeContext));
                        }
                        if (exceptionsChanged) {
                            callback.markChanged(applyfunc.getExceptionalResult(invokeContext));
                        }
                    }
                    @Override
                    public void caseDOM(DOMNative value) {
                        // assume every DOM function just stores its arguments somewhere in the DOM
                        Set<Value> args = callback.readableValueAt(value.getDefaultArgs(invokeContext));
                        addValuesToKey(value.getDynamicStoreProperty(), args);
                        addValuesToKey(value.getDefaultProperty(), args);
                        addValueToKey(value.getResult(invokeContext), value);
                    }
                    @Override
                    public void caseObjectCreate(ObjectCreateNative value) {
                    	Set<Value> labelArgs = callback.readableValueAt(value.getLabelArg(invokeContext));
                    	Set<Value> protoArgs = callback.readableValueAt(new NativeArgKey(value, invokeContext, 0));
                    	Set<Value> dynamicArgs = callback.readableValueAt(value.getDynamicArgs(invokeContext));
                    	Set<Value> prtyArgs = callback.readableValueAt(new NativeArgKey(value, invokeContext, 1));
                    	for (Value val : labelArgs) {
                    		if (!(val instanceof ObjectValue))
                    			continue;
                    		ObjectValue obj = (ObjectValue)val;
                    		addValuesToKey(obj.getPrototypeProperty(), protoArgs);
                    		addValuesToKey(obj.getPrototypeProperty(), dynamicArgs);
                    		
                    		defineProperties(obj, prtyArgs);
                    		defineProperties(obj, dynamicArgs);
                    	}
                    	addValuesToKey(value.getResult(invokeContext), labelArgs);
                    }
					private void defineProperties(ObjectValue obj,Set<Value> prtyArgs) {
						for (Value prtyList : prtyArgs) {
							if (!(prtyList instanceof ObjectValue))
								continue;
							ObjectValue prtyListObj = (ObjectValue)prtyList;
							for (Value prty : callback.readableValueAt(prtyListObj.getDefaultProperty())) {
								if (!(prty instanceof ObjectValue))
									continue;
								ObjectValue prtyObj = (ObjectValue)prty;
								addValuesToKey(obj.getDynamicStoreProperty(), callback.readableValueAt(new NamedPropertyKey(prtyObj, "value")));
							}
						}
					}
                    @Override
                    public void caseFunctionBind(FunctionBindNative value) {
                    	Set<Value> thisArgs = callback.readableValueAt(value.getThisArg(invokeContext));
                    	Set<Value> firstArgs = callback.readableValueAt(new NativeArgKey(value, invokeContext, 0));
                    	Set<Value> dynamicArgs = callback.readableValueAt(value.getDynamicArgs(invokeContext));
                		FunctionBindResultNative result = new FunctionBindResultNative(invokeContext);
                    	for (Value thisArg : thisArgs) {
                    		if (!(thisArg instanceof FunctionValue))
                    			continue;
                    		FunctionValue func = (FunctionValue)thisArg;
                    		addValueToKey(result.getBoundTarget(), func);
                    		addValuesToKey(result.getBoundThisArg(), firstArgs);
                    		addValuesToKey(result.getBoundThisArg(), dynamicArgs);
                    		Set<Value> argumentValues;
                    		int index = 0;
                            do {
                                NativeArgKey key = new NativeArgKey(value, invokeContext, index+1);
                                argumentValues = callback.readableValueAt(key);
                                addValuesToKey(result.getBoundArg(index), argumentValues);
                                addValuesToKey(result.getBoundDefaultArg(), argumentValues);
                                index++;
                            } while (!argumentValues.isEmpty());
                            addValuesToKey(result.getBoundDynamicArg(), dynamicArgs);
                            addValuesToKey(result.getBoundDefaultArg(), dynamicArgs);
                        }
                    	addValueToKey(value.getResult(invokeContext), result);
                    }
                    @Override
                    public void caseFunctionBindResult(FunctionBindResultNative value) {
                    	Set<Value> boundDynamicArgs = callback.readableValueAt(value.getBoundDynamicArg());
                    	Set<Value> boundDefaultArgs = callback.readableValueAt(value.getBoundDefaultArg());
                    	Set<Value> actualDynamicArgs = callback.readableValueAt(value.getDynamicArgs(invokeContext));
                    	Set<Value> actualDefaultArgs = callback.readableValueAt(value.getDefaultArgs(invokeContext));
                    	Set<Value> targets = callback.readableValueAt(value.getBoundTarget());
                    	Set<Value> boundThisArgs = callback.readableValueAt(value.getBoundThisArg());
                		for (Value target : targets) {
                			if (!(target instanceof FunctionValue))
                				continue;
                			FunctionValue ftarget = (FunctionValue)target;
                			
                			linkInstanceToFunction(ftarget, invokeContext);
                			linkThisArgsToFunction(boundThisArgs, ftarget, invokeContext);
                			
                			if (boundDynamicArgs.isEmpty()) {
	                    		Set<Value> argumentValues;
	                    		int index = 0;
	                    		while (true) {
	                                argumentValues = callback.readableValueAt(new BoundArgKey(value, index));
	                                if (argumentValues.isEmpty())
	                                	break;
	                                linkArgToFunction(argumentValues, ftarget, invokeContext, index);
	                                index++;
	                    		}
	                    		int numberOfBoundArguments = index;
	                    		index = 0;
	                    		while (true) {
	                    			argumentValues = callback.readableValueAt(new NativeArgKey(value, invokeContext, index));
	                    			if (argumentValues.isEmpty())
	                    				break;
	                    			linkArgToFunction(argumentValues, ftarget, invokeContext, numberOfBoundArguments+index);
	                    			index++;
	                    		}
	                    		linkDynamicArgToFunction(actualDynamicArgs, ftarget, invokeContext);
                			} else {
                        		// if there are any dynamic arguments, we can't know how many bound arguments there are
                    			linkDynamicArgToFunction(boundDefaultArgs, ftarget, invokeContext);
                    			linkDynamicArgToFunction(actualDefaultArgs, ftarget, invokeContext);
                			}
                    		
                    		addValuesToKey(value.getResult(invokeContext), getFunctionResults(ftarget, invokeContext));
                    		addValuesToKey(value.getExceptionalResult(invokeContext), getFunctionExceptionalResults(ftarget, invokeContext));
                		}
                    }
                    @Override
                    public void caseObjectGetPrototypeOf(ObjectGetPrototypeOfNative value) {
                    	for (Value val : callback.readableValueAt(new NativeArgKey(value, invokeContext, 0))) {
                    		if (!(val instanceof ObjectValue))
                    			continue;
                    		ObjectValue obj = (ObjectValue)val;
                    		addValuesToKey(value.getResult(invokeContext), callback.readableValueAt(obj.getPrototypeProperty()));
                    	}
                    	for (Value val : callback.readableValueAt(value.getDynamicArgs(invokeContext))) {
                    		if (!(val instanceof ObjectValue))
                    			continue;
                    		ObjectValue obj = (ObjectValue)val;
                    		addValuesToKey(value.getResult(invokeContext), callback.readableValueAt(obj.getPrototypeProperty()));
                    	}
                    }
                });
            }
            
            public void transferInvokeDOMFunction(InvokeDOMFunctionTransfer dom) {
                // invariant: dom object has no [[Prototype]]
                // invoke every function reachable from the DOM heap label
                for (ObjectValue value : getReachableObjects(DOMNative.Instance)) {
                    if (value instanceof FunctionValue) {
                        FunctionValue func = (FunctionValue)value;
                        linkThisArgsToFunction(Collections.singleton(DOMNative.Instance), func, DOMInvokeContext.Instance);
                        linkDynamicArgToFunction(Collections.singleton(DOMNative.Instance), func, DOMInvokeContext.Instance);
                        linkInstanceToFunction(func, DOMInvokeContext.Instance);
                        // TODO: Can the return value from these functions flow somewhere?
                    }
                }
            }

            private Set<ObjectValue> getReachableObjects(ObjectValue obj) {
                Set<ObjectValue> result = new HashSet<ObjectValue>();
                LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>();
                queue.add(obj);
                result.add(obj);
                while (!queue.isEmpty()) {
                    ObjectValue o = queue.removeFirst();
                    for (Value value : callback.readableValueAt(o.getDefaultProperty())) {
                        if (!(value instanceof ObjectValue)) {
                            continue;
                        }
                        ObjectValue objval = (ObjectValue)value;
                        if (result.add(objval)) {
                            queue.add(objval);
                        }
                    }
                }
                return result;
            }

            private void linkThisArgsToFunction(Collection<? extends Value> thisArgs, FunctionValue function, Context ctx) {
                lattice.linkThisArgsToFunction(thisArgs, function, ctx);
            }
            private void linkArgToFunction(Collection<? extends Value> argValues, FunctionValue function, Context ctx, int index) {
                if (argValues.contains(NumberValue.Instance)) {
                    System.out.print("");
                }
                lattice.linkArgToFunction(argValues, function, ctx, index);
            }
            //			private void linkDefaultArgToFunction(Collection<? extends Value> argValues, FunctionValue function, Context ctx) {
            //				lattice.linkDefaultArgToFunction(argValues, function, ctx);
            //			}
            private void linkDynamicArgToFunction(Collection<? extends Value> argValues, FunctionValue function, Context ctx) {
                lattice.linkDynamicArgToFunction(argValues, function, ctx);
            }
            private void linkLabelArgToFunction(Collection<? extends Value> labelArgs, FunctionValue function, Context ctx) {
                lattice.linkLabelArgToFunction(labelArgs, function, ctx);
            }
            private void linkInstanceToFunction(FunctionValue function, Context ctx) {
                lattice.linkInstanceToFunction(function, ctx);
            }
            private Set<Value> getFunctionResults(FunctionValue func, Context ctx) {
                return lattice.getFunctionResults(func, ctx);
            }
            private Set<Value> getFunctionExceptionalResults(FunctionValue func, Context ctx) {
                return lattice.getFunctionExceptionalResults(func, ctx);
            }
            private void addValuesToKey(Key key, Collection<? extends Value> values) {
                lattice.addValuesToKey(key, values);
            }
            private void addValueToKey(Key key, Value value) {
                lattice.addValueToKey(key, value);
            }
            private void addValuesToVariable(String varname, Scope scope, Context ctx, Collection<? extends Value> valuesToAdd) {
                lattice.addValuesToVariable(varname, scope, ctx, valuesToAdd);
            }
            private void addGlobalNativeFunction(String name, NativeFunctionValue func, ObjectValue prototype) {
                addValueToKey(new NamedPropertyKey(GlobalObjectValue.Instance, name), func);
                if (prototype != null) {
                    addValueToKey(func.getPrototypeProperty(), prototype);
                }
                addValueToKey(new NamedPropertyKey(func, "prototype"), new FunctionPrototypeValue(func));
            }
            private void addNestedNativeFunction(ObjectValue host, String name, NativeFunctionValue func) {
                addValueToKey(new NamedPropertyKey(host, name), func);
                addValueToKey(func.getPrototypeProperty(), FunctionNative.Instance);
            }
            /** Returns the set of objects reachable by following prototype pointers, including the argument itself */
            private Set<ObjectValue> getPrototypeReachable(ObjectValue obj) {
                return lattice.getPrototypeReachable(obj);
            }
            private boolean convertibleToNumber(Set<Value> values) {
                return lattice.convertibleToNumber(values);
            }
            private boolean convertibleToString(Set<Value> values) {
                return lattice.convertibleToString(values);
            }
        } // end of class Inner

        Inner inner = new Inner();
        if (t instanceof FlowNodeContextPair) {
            FlowNodeContextPair fc = (FlowNodeContextPair)t;
            inner.transferFlowNode(fc.getFlowNode(), fc.getContext());
        }
        else if (t instanceof InvokeDOMFunctionTransfer) {
            InvokeDOMFunctionTransfer dom = (InvokeDOMFunctionTransfer)t;
            inner.transferInvokeDOMFunction(dom);
        }
        else if (t instanceof NativeTransfer) {
        	NativeTransfer nat = (NativeTransfer)t;
        	inner.transferNative(nat);
        }
        else {
            throw new RuntimeException("Unknown transfer node: " + t.getClass());
        }
    }

    @Override
    public Iterable<? extends TransferNode> getInitialDependencies(Key key) {
        final List<TransferNode> list = new LinkedList<TransferNode>();
        key.apply(new KeyVisitor() {
            @Override
            public void caseOuterFunctionProperty(OuterFunctionPropertyKey key) {
            }
            @Override
            public void caseDefaultProperty(DefaultPropertyKey key) {
            }
            @Override
            public void caseDynamicStoreProperty(DynamicStorePropertyKey key) {
            }
            @Override
            public void casePrototypePropertyKey(PrototypePropertyKey key) {
            }
            @Override
            public void caseOutputPoint(OutputPointKey key) {
            	for (InputPoint ip : key.getOutputPoint().getDestinations()) {
            		list.add(new FlowNodeContextPair(ip.getFlowNode(), key.getContext()));
            	}
            }
            @Override
            public void caseIntegerProperty(IntegerPropertyKey key) {
            }
            @Override
            public void caseNamedProperty(NamedPropertyKey key) {
            }
            @Override
            public void caseVariableKey(VariableKey key) {
            }
            @Override
            public void caseNativeThisArg(NativeThisArgKey key) {
                handleNative(key.getFunction(), key.getContext());
            }
            @Override
            public void caseNativeArg(NativeArgKey key) {
                handleNative(key.getFunction(), key.getContext());
            }
            @Override
            public void caseNativeDefaultArgs(NativeDefaultArgsKey key) {
                handleNative(key.getFunction(), key.getContext());
            }
            @Override
            public void caseNativeDynamicArgs(NativeDynamicArgsKey key) {
                handleNative(key.getFunction(), key.getContext());
            }
            @Override
            public void caseNativeLabelArg(NativeLabelArgKey key) {
                handleNative(key.getFunction(), key.getContext());
            }
            @Override
            public void caseFunctionInstance(FunctionInstanceKey key) {
                list.add(new FlowNodeContextPair(dataflow.getFunctionInstanceFlowNodes().get(key.getFunction()), key.getContext()));
            }
            private void handleNative(NativeFunctionValue func, Context context) {
            	list.add(new NativeTransfer(func, context));
            }
            @Override
            public void caseNativeResult(NativeResultKey key) {
            }
            @Override
            public void caseNativeExceptionalResult(NativeExceptionalResultKey key) {
            }
			@Override
			public void caseBoundTarget(BoundTargetKey key) {
			}
			@Override
			public void caseBoundArg(BoundArgKey key) {
			}
			@Override
			public void caseBoundThisArg(BoundThisArgKey key) {
			}
			@Override
			public void caseBoundDynamicArg(BoundDynamicArgKey key) {
			}
			@Override
			public void caseBoundDefaultArg(BoundDefaultArgKey key) {
			}
        });
        return list;
    }



    //	/**
    //	 * The context-sensitivity strategy.
    //	 * @param invoke invocation node
    //	 * @param currentContext current execution context
    //	 * @return a context
    //	 */
    //	private Context callingContext(InvokeNode invoke, Context currentContext) {
    //		assert invoke != null;
    //		assert currentContext != null;
    //		return widenContext(new InvokeContext(dataflow.getCalls().getBackward(invoke), currentContext), callsiteContextSensitiveDepth);
    //	}
    //	
    //	private Context widenContext(Context context, int depth) {
    //		if (depth <= 0) {
    //			return WidenedContext.Instance;
    //		} else if (context.getParentContext() == null) {
    //			return context;
    //		} else {
    //			return context.replaceParentContext(widenContext(context.getParentContext(), depth-1));
    //		}
    //	}
}