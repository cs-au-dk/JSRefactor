package dk.brics.jspointers.cfg2dataflow;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.analysis.reachdef.ArgumentsArrayVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ParameterVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.SelfVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.StatementVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.UninitializedVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.scope.CatchScope;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.Assertion;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.BooleanConst;
import dk.brics.jscontrolflow.statements.Call;
import dk.brics.jscontrolflow.statements.CallConstructor;
import dk.brics.jscontrolflow.statements.CallProperty;
import dk.brics.jscontrolflow.statements.CallVariable;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeclareVariable;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.EnterCatch;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.ExceptionalReturn;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jscontrolflow.statements.LeaveScope;
import dk.brics.jscontrolflow.statements.NewArray;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.NewRegexp;
import dk.brics.jscontrolflow.statements.Nop;
import dk.brics.jscontrolflow.statements.NullConst;
import dk.brics.jscontrolflow.statements.NumberConst;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadThis;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.Return;
import dk.brics.jscontrolflow.statements.ReturnVoid;
import dk.brics.jscontrolflow.statements.StatementVisitor;
import dk.brics.jscontrolflow.statements.StringConst;
import dk.brics.jscontrolflow.statements.Throw;
import dk.brics.jscontrolflow.statements.UnaryOperation;
import dk.brics.jscontrolflow.statements.UndefinedConst;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jspointers.DataflowUtil;
import dk.brics.jspointers.dataflow.AbstractFlowNodeVisitor;
import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.AllocNode.PrototypeKind;
import dk.brics.jspointers.dataflow.CoerceToObject;
import dk.brics.jspointers.dataflow.CoerceToPrimitive;
import dk.brics.jspointers.dataflow.ConstNode;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.DeleteNode;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.FunctionInstanceNode;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
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
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.values.BasicType;
import dk.brics.jspointers.lattice.values.BooleanValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NullValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UndefinedValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.DOMNative;
import dk.brics.jspointers.lattice.values.natives.EvalNative;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jspointers.lattice.values.natives.FunctionNative;
import dk.brics.jspointers.lattice.values.natives.ObjectCreateNative;
import dk.brics.jspointers.lattice.values.natives.ObjectGetPrototypeOfNative;
import dk.brics.jspointers.lattice.values.natives.ObjectNative;
import dk.brics.jspointers.parametric.StatementAllocSite;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jspointers.solver.Solver;
import dk.brics.jsutil.MultiMap;

public class DataflowCreator {

    private DataflowGraph dataflow;
    private IControlflow2DataflowBinding binding;
    private Map<String, Function> namedHarnessFunctions = new HashMap<String, Function>();

    public static DataflowGraph convert(Set<Function> nonHarness, Set<Function> harness, IControlflow2DataflowBinding binding) {
        DataflowCreator o = new DataflowCreator(binding);
        o.convertx(nonHarness, harness);
        return o.dataflow;
    }

    public DataflowCreator(IControlflow2DataflowBinding binding) {
        this.binding = binding;
    }

    private void convertx(Set<Function> nonHarness, Set<Function> harness) {
        dataflow = new DataflowGraph();
        dataflow.getTopLevels().addAll(nonHarness);
        dataflow.getTopLevels().addAll(harness);
        for (Function harnessFunc : harness) {
        	for (final Function function : harnessFunc.getTransitiveInnerFunctions(true)) {
                if (function.getName() != null) {
    				namedHarnessFunctions.put(function.getName(), function);
                }
            }
        }
        dataflow.getNamedHarnessFunctions().putAll(namedHarnessFunctions); // TODO move this out of here - refactor DataflowGraph to be less of a kitchen sink
        for (Function harnessFunc : harness) {
            convert(harnessFunc, true);
        }
        for (Function func : nonHarness) {
            convert(func, false);
        }
        dataflow.getHarnessFunctions().addAll(harness);
        InitializeNode init = new InitializeNode();
        dataflow.setInitializer(init);
        
        dataflow.getEntryFunctions().addAll(nonHarness);
        dataflow.getEntryFunctions().addAll(harness);
    }

    private void convert(final Function topLevel, final boolean enableHarnessVars) {
        final MultiMap<WithScope, InterscopeIdentityNode> with2interscopes = new MultiMap<WithScope, InterscopeIdentityNode>();
        final Map<WithScope, StubNode> with2stub = new HashMap<WithScope, StubNode>();
        
        final Function arrayFunction = namedHarnessFunctions.get("Array");
        final Function regexpFunction = namedHarnessFunctions.get("RegExp");
        
        assert arrayFunction != null : "Missing Array function";
        assert regexpFunction != null : "Missing RegExp function";

		final UserFunctionValue arrayFunctionVal = new UserFunctionValue(arrayFunction, MainContext.Instance);
		final UserFunctionValue regexpFunctionVal = new UserFunctionValue(regexpFunction, MainContext.Instance);

        final PrivateVariables privateVars = new PrivateVariables(topLevel);
        for (final Function function : topLevel.getTransitiveInnerFunctions(true)) {
            final MultiMap<VariableDefinition, OutputPoint> stmt2output = new MultiMap<VariableDefinition, OutputPoint>();
            final MultiMap<VariableDefinition, InputPoint> stmt2dest = new MultiMap<VariableDefinition, InputPoint>();

            final ReachingDefinitions reachingDefs = new ReachingDefinitions(function, privateVars, function != topLevel);
            
            binding.setReachingDefinitions(function, privateVars, reachingDefs);
            
            // contains all nodes added to this function
            final LinkedList<FlowNode> nodes = new LinkedList<FlowNode>();

            // function instance
            final FunctionInstanceNode funcInstanceNode = new FunctionInstanceNode(function);
            nodes.add(funcInstanceNode);
            dataflow.getFunctionInstanceFlowNodes().put(function, funcInstanceNode);
            stmt2output.add(SelfVariableDefinition.Instance, funcInstanceNode.getResult());

            // normal ReturnNode
            final ReturnNode normalReturn = new ReturnNode(function, false);
            nodes.add(normalReturn);
            dataflow.getNormalReturns().put(function, normalReturn);

            // exceptional ReturnNode
            final ReturnNode exceptionalReturn = new ReturnNode(function, true);
            nodes.add(exceptionalReturn);
            dataflow.getExceptionalReturns().put(function, exceptionalReturn);

            // coercion node for each exception handler
            final Map<Statement,CoerceToPrimitive> exhandler2coercion = new HashMap<Statement, CoerceToPrimitive>();

            // maps a CatchNode to all the output points it can take exception instances from
            final MultiMap<Catch,OutputPoint> catchNodeSrcPoints = new MultiMap<Catch, OutputPoint>();

            // only create one var read/write per local variable
            final Map<String, VarReadNode> localVarReads = new HashMap<String, VarReadNode>();
            final Map<String, VarWriteNode> localVarWrites = new HashMap<String, VarWriteNode>();

            // create parameter VarReadNodes for private parameters
            for (int i=0; i<function.getParameterNames().size(); i++) {
                String name = function.getParameterNames().get(i);
                if (!privateVars.getInnerAssignedVars(function).contains(name)) {
                    VarReadNode read = new VarReadNode(name, function);
                    read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                    localVarReads.put(name, read);
                    nodes.add(read);

                    stmt2output.add(new ParameterVariableDefinition(i), read.getResult());
                }
            }
            // create VarReadNodes for arguments array if it is not explicitly declared
            if (!function.hasExplicitArgumentsDeclaration() && function.getOuterFunction() != null) {
                // note: no need to check for privateness, because it is impossible for inner functions
                // to reference the "arguments" variable of an outer function, since such an inner
                // function itself must have "arguments" in its scope
                VarReadNode read = new VarReadNode("arguments", function);
                read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                localVarReads.put("arguments", read);
                nodes.add(read);

                stmt2output.add(ArgumentsArrayVariableDefinition.Instance, read.getResult());
            }

            // we need at most one ConstNode per value, so keep track of those we have created
            final Map<Value,ConstNode> constNodes = new HashMap<Value, ConstNode>();
            
            // create undefined const for uninitialized variables
            {
            	ConstNode ct = new ConstNode(UndefinedValue.Instance);
            	ct.getFunctionInstance().addSource(funcInstanceNode.getResult());
            	nodes.add(ct);
            	stmt2output.add(UninitializedVariableDefinition.Instance, ct.getResult());
            	
            	constNodes.put(UndefinedValue.Instance, ct);
            }
            
            // read variable statements that have been removed due to def-use optimization
            // they are not in the def-use graph, but must added to binding.addVariableOutputPoint
            final List<ReadVariable> optimizedReadVarStmts = new ArrayList<ReadVariable>();
            final List<Phi> phiStmts = new ArrayList<Phi>();
            
            for (Block block : function.getBlocks()) {
                for (final Statement stmt : block.getStatements()) {
                    stmt.apply(new StatementVisitor() {
                        private void addBinopCoercions(BinaryOperation stm) {
                            switch (stm.getOperator()) {
                            case IN:
                            case INSTANCEOF:
                                return; // no primitive coercion
                            }
                            coerceToPrimitive(stm.getArg1Var());
                            coerceToPrimitive(stm.getArg2Var());
                        }
                        private StubNode addStub(int var) {
                        	StubNode stub = new StubNode(true);
                        	link(var, stub.getInput());
                        	nodes.add(stub);
                        	return stub;
                        }
                        @Override
                        public void caseBinaryOperation(BinaryOperation stm) {
                            addBinopCoercions(stm);
                            switch (stm.getOperator()) {
                            case PLUS: {
                                PlusNode plus = new PlusNode();
                                link(stm.getArg1Var(), plus.getArgument());
                                link(stm.getArg2Var(), plus.getArgument());
                                linkOutput(stm, plus.getResult());
                                nodes.add(plus);
                                break;
                            }
                            case LESS:
                            case LESS_EQUAL:
                            case GREATER:
                            case GREATER_EQUAL:
                            case EQUAL:
                            case NOT_EQUAL:
                            case STRICT_EQUAL:
                            case STRICT_NOT_EQUAL:
                            case INSTANCEOF:
                            case IN:
                                linkOutput(stm, constant(BooleanValue.Instance).getResult());
                                break;
                            case BITWISE_AND:
                            case BITWISE_OR:
                            case BITWISE_XOR:
                            case MINUS:
                            case TIMES:
                            case DIVIDE:
                            case MODULO:
                            case SHIFT_LEFT:
                            case SHIFT_RIGHT:
                            case USHIFT_RIGHT:
                                linkOutput(stm, constant(NumberValue.Instance).getResult());
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown binop: " + stm.getOperator());
                            }
                            binding.addBinaryOperation(stm, addStub(stm.getArg1Var()), addStub(stm.getArg2Var()));
                        }

                        @Override
                        public void caseBooleanConst(BooleanConst stm) {
                            linkOutput(stm, constant(BooleanValue.Instance).getResult());
                        }

                        @Override
                        public void caseCall(Call stm) {
                            InvokeNode invoke = new InvokeNode(stm.getArguments().size(), false, true, stm);
                            InvokeResultNode res = new InvokeResultNode(false, invoke);

                            link(stm.getFuncVar(), invoke.getFunc());
                            link(stm.getFuncVar(), res.getFunc());
                            for (int i=0; i<stm.getArguments().size(); i++) {
                                link(stm.getArguments().get(i), invoke.getArguments().get(i));
                            }

                            linkOutput(stm, res.getResult());

                            linkExceptionalResult(stm, res);

                            nodes.add(invoke);
                            nodes.add(res);
                            dataflow.getCalls().add(stm, invoke);
                            binding.addCall(stm, invoke);
                        }
                        @Override
                        public void caseCallVariable(CallVariable stm) {
                            VariableAccess access = resolveVariableAccess(stm.getScope(), stm.getVarName());

                            for (WithScopeAccess withacc : access.withAccesses) {
                                InvokeNode withInvoke = new InvokeNode(stm.getArguments().size(), false, false, stm);
                                InvokeResultNode withResult = new InvokeResultNode(false, withInvoke);

                                LoadNode load = new LoadNode(stm.getVarName());
                                EnterWith w = withacc.scope.getStatement();
                                if (withacc.depth == 0) {
                                    link(w, w.getObjectVar(), load.getBase());
                                    link(w, w.getObjectVar(), withInvoke.getBase());
                                } else {
                                    InterscopeIdentityNode interscope = new InterscopeIdentityNode(withacc.depth);
                                    interscope.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    with2interscopes.add(withacc.scope, interscope);
                                    load.getBase().addSource(interscope.getResult());
                                    withInvoke.getBase().addSource(interscope.getResult());
                                    nodes.add(interscope);
                                }
                                withInvoke.getFunc().addSource(load.getResult());
                                withResult.getFunc().addSource(load.getResult());
                                for (int i=0; i<stm.getArguments().size(); i++) {
                                    link(stm.getArguments().get(i), withInvoke.getArguments().get(i));
                                }

                                linkOutput(stm, withResult.getResult());
                                linkExceptionalResult(stm, withResult);

                                nodes.add(withInvoke);
                                nodes.add(withResult);
                                nodes.add(load);
                                dataflow.getCalls().add(stm, withInvoke);
                                binding.addCallVariable(stm, load, withacc.scope, withInvoke);
                            }

                            InvokeNode invoke = new InvokeNode(stm.getArguments().size(), false, true, stm);
                            InvokeResultNode result = new InvokeResultNode(false, invoke);
                            if (access instanceof GlobalVariableAccess) {
                                VarReadGlobalNode read = new VarReadGlobalNode(stm.getVarName());
                                read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                invoke.getFunc().addSource(read.getResult());
                                result.getFunc().addSource(read.getResult());
                                nodes.add(read);
                                binding.addCallVariable(stm, read, invoke);
                            } else {
                                NonGlobalVariableAccess nonglobal = (NonGlobalVariableAccess)access;
                                if (nonglobal.depth == 0) {
                                    if (privateVars.getInnerAssignedVars(function).contains(stm.getVarName())) {
                                        VarReadNode read = new VarReadNode(stm.getVarName(), nonglobal.scope);
                                        read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                        invoke.getFunc().addSource(read.getResult());
                                        result.getFunc().addSource(read.getResult());
                                        nodes.add(read);
                                        binding.addCallVariable(stm, read, invoke);
                                    } else {
                                        link(stm.getVarName(), nonglobal.scope, invoke.getFunc());
                                        link(stm.getVarName(), nonglobal.scope, result.getFunc());
                                        binding.addCallVariable(stm, invoke);
                                    }
                                } else {
                                    VarReadInterscopeNode read = new VarReadInterscopeNode(stm.getVarName(), nonglobal.scope, nonglobal.depth);
                                    read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    invoke.getFunc().addSource(read.getResult());
                                    result.getFunc().addSource(read.getResult());
                                    nodes.add(read);
                                    binding.addCallVariable(stm, read, invoke);
                                }
                            }
                            for (int i=0; i<stm.getArguments().size(); i++) {
                                link(stm.getArguments().get(i), invoke.getArguments().get(i));
                            }
                            linkOutput(stm, result.getResult());
                            linkExceptionalResult(stm, result);

                            nodes.add(invoke);
                            nodes.add(result);
                            dataflow.getCalls().add(stm, invoke);
                        }
                        @Override
                        public void caseCallProperty(CallProperty stm) {
                            final IInvocationFlowNode invoke;
                            final InvokeResultNode res;
                            
                            CoerceToObject coerce = coerceToObject(stm.getBaseVar());
                            String propertyStr = getConstantString(stm, stm.getPropertyVar());
                            if (propertyStr != null) {
                                LoadAndInvokeNode loadInvoke = new LoadAndInvokeNode(propertyStr, stm.getArguments().size(), stmt);
                                res = new InvokeResultNode(false, loadInvoke);
                                loadInvoke.setResultNode(res);
                                
                                loadInvoke.getBase().addSource(coerce.getResult());

                                res.getFunc().addSource(loadInvoke.getInvokedFunction());

                                invoke = loadInvoke;

                                binding.addCallProperty(stm, loadInvoke);
                            } else {
                                InvokeNode iinvoke = new InvokeNode(stm.getArguments().size(), false, false, stm);
                                res = new InvokeResultNode(false, iinvoke);

                                LoadDynamicNode load = new LoadDynamicNode();
                                load.getBase().addSource(coerce.getResult());
                                link(stm.getPropertyVar(), load.getProperty());
                                iinvoke.getFunc().addSource(load.getResult());
                                res.getFunc().addSource(load.getResult());
                                nodes.add(load);

                                invoke = iinvoke;
                                binding.addCallProperty(stm, load, iinvoke);
                            }
                            
                            invoke.getBase().addSource(coerce.getResult());
                            for (int i=0; i<stm.getArguments().size(); i++) {
                                link(stm.getArguments().get(i), invoke.getArguments().get(i));
                            }
                            linkOutput(stm, res.getResult());
                            linkExceptionalResult(stm, res);

                            nodes.add((FlowNode)invoke);
                            nodes.add(res);
                            dataflow.getCalls().add(stm, invoke);
                        }
                        private void linkExceptionalResult(InvokeStatement stm, InvokeResultNode res) {
                            Statement exhandler = findExceptionHandler(stm);
                            if (exhandler instanceof Catch) {
                                Catch cn = (Catch)exhandler;
                                catchNodeSrcPoints.add(cn, res.getExceptionalResult());
                            } else if (exhandler instanceof ExceptionalReturn) {
                                exceptionalReturn.getValue().addSource(res.getExceptionalResult());
                            } else {
                                throw new RuntimeException("Unexpected exception handler: " + exhandler);
                            }
                        }
                        @Override
                        public void caseConstructorCall(CallConstructor stm) {
                            AllocNode alloc = new AllocNode(new StatementAllocSite(stm), PrototypeKind.NONE);
                            alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            
                            LoadDirectNode loadProto = new LoadDirectNode("prototype");
                            link(stm.getFuncVar(), loadProto.getBase());
                            SetPrototype setProto = new SetPrototype();
                            setProto.getBase().addSource(alloc.getResult());
                            setProto.getValue().addSource(loadProto.getResult());
                            nodes.add(loadProto);
                            nodes.add(setProto);

                            InvokeNode invoke = new InvokeNode(stm.getArguments().size(), true, true, stm);
                            InvokeResultNode res = new InvokeResultNode(true, invoke);

                            invoke.getBase().addSource(alloc.getResult());
                            res.getAllocatedObject().addSource(alloc.getResult());

                            link(stm.getFuncVar(), invoke.getFunc());
                            link(stm.getFuncVar(), res.getFunc());
                            for (int i=0; i<stm.getArguments().size(); i++) {
                                link(stm.getArguments().get(i), invoke.getArguments().get(i));
                            }
                            linkOutput(stm, res.getResult());
                            linkExceptionalResult(stm, res);

                            nodes.add(invoke);
                            nodes.add(res);
                            nodes.add(alloc);
                            dataflow.getCalls().add(stm, invoke);

                            binding.addCallConstructor(stm, alloc, invoke);
                        }

                        @Override
                        public void caseCatch(Catch stm) {
                            // do nothing here - catch statements are handled below
                        }

                        @Override
                        public void caseCreateFunction(CreateFunction stm) {
                            InitializeFunctionNode init = new InitializeFunctionNode(stm.getFunction());
                            init.getOuterFunction().addSource(funcInstanceNode.getResult());
                            nodes.add(init);
                            linkOutput(stm, init.getResult());
                            binding.addCreateFunction(stm, init);
                        }

                        @Override
                        public void caseDeleteProperty(DeleteProperty stm) {
                            linkOutput(stm, constant(BooleanValue.Instance).getResult());
                            DeleteNode node = new DeleteNode();
                            link(stm.getBaseVar(), node.getBase());
                            binding.addDeleteProperty(stm, node);
                        }

                        @Override
                        public void caseGetNextProperty(GetNextProperty stm) {
                            ConstNode str = new ConstNode(StringValue.Instance);
                            str.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            LoadDynamicNode load = new LoadDynamicNode(); // TODO add to dataflow->CFG binding
                            link(stm.getObjectVar(), load.getBase());
                            load.getProperty().addSource(str.getResult());
                            nodes.add(str);
                            nodes.add(load);
                            linkOutput(stm, load.getResult());
                            binding.addGetNextProperty(stm, load);
                        }

                        @Override
                        public void caseNewArray(NewArray stm) {
                            AllocNode alloc = new AllocNode(new StatementAllocSite(stm), PrototypeKind.ARRAY);
                            linkOutput(stm, alloc.getResult());
                            alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            nodes.add(alloc);
                            
                            // set the allocated object's prototype to Array.prototype
                            SetPrototype proto = new SetPrototype();
                            proto.getBase().addSource(alloc.getResult());
							proto.getValue().addSource(constant(arrayFunctionVal.getFunctionPrototype()).getResult());
							nodes.add(proto);
                        }

                        @Override
                        public void caseNewObject(NewObject stm) {
                            AllocNode alloc = new AllocNode(new StatementAllocSite(stm), PrototypeKind.OBJECT);
                            linkOutput(stm, alloc.getResult());
                            alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            nodes.add(alloc);
                            binding.addNewObject(stm, alloc);
                            
                            // set the allocated object's prototype to Object.prototype
                            SetPrototype proto = new SetPrototype();
                            proto.getBase().addSource(alloc.getResult());
                            proto.getValue().addSource(constant(ObjectNative.Instance.getFunctionPrototype()).getResult());
                            nodes.add(proto);
                        }

                        @Override
                        public void caseNewRegexp(NewRegexp stm) {
                            AllocNode alloc = new AllocNode(new StatementAllocSite(stm), PrototypeKind.REGEXP);
                            linkOutput(stm, alloc.getResult());
                            alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            nodes.add(alloc);
                            
                            // set the allocated object's prototype to RegExp.prototype
                            SetPrototype proto = new SetPrototype();
                            proto.getBase().addSource(alloc.getResult());
                            proto.getValue().addSource(constant(regexpFunctionVal.getFunctionPrototype()).getResult());
                            nodes.add(proto);
                        }

                        @Override
                        public void caseNullConst(NullConst stm) {
                            linkOutput(stm, constant(NullValue.Instance).getResult());
                        }

                        @Override
                        public void caseNumberConst(NumberConst stm) {
                            linkOutput(stm, constant(NumberValue.Instance).getResult());
                        }

                        @Override
                        public void casePhi(Phi stm) {
                            // do nothing - reaching definitions see through Phi statements
                            // except notify the binding where the phi node fits in the def-use graph
                            phiStmts.add(stm);
                        }

                        @Override
                        public void caseReadProperty(ReadProperty stm) {
                        	CoerceToObject coerce = coerceToObject(stm.getBaseVar());
                            String propertyStr = getConstantString(stm, stm.getPropertyVar());
                            if (propertyStr != null) {
                                LoadNode load = new LoadNode(propertyStr);
                                load.getBase().addSource(coerce.getResult());
                                linkOutput(stm, load.getResult());
                                nodes.add(load);
                                dataflow.getLoads().add(stm, load);
                                binding.addReadProperty(stm, load);
                            } else {
                                LoadDynamicNode load = new LoadDynamicNode();
                                load.getBase().addSource(coerce.getResult());
                                link(stm.getPropertyVar(), load.getProperty());
                                linkOutput(stm, load.getResult());
                                nodes.add(load);
                                dataflow.getLoads().add(stm, load);
                                binding.addReadProperty(stm, load);
                            }
                        }

                        @Override
                        public void caseStringConst(StringConst stm) {
                            linkOutput(stm, constant(StringValue.Instance).getResult());
                        }

                        @Override
                        public void caseUndefinedConst(UndefinedConst stm) {
                            linkOutput(stm, constant(UndefinedValue.Instance).getResult());
                        }

                        @Override
                        public void caseUnaryOperation(UnaryOperation stm) {
                            coerceToPrimitive(stm.getArgVar());
                            switch (stm.getOperator()) {
                            case NOT: // boolean result
                                linkOutput(stm, constant(BooleanValue.Instance).getResult());
                                break;
                            case MINUS: // number result
                            case PLUS:
                            case COMPLEMENT:
                            case INCREMENT:
                            case DECREMENT:
                                linkOutput(stm, constant(NumberValue.Instance).getResult());
                                break;
                            case TYPEOF: // string result
                                linkOutput(stm, constant(StringValue.Instance).getResult());
                                break;
                            case VOID: // "undefined" result
                            	linkOutput(stm, constant(UndefinedValue.Instance).getResult());
                            	break;
                            default:
                                throw new RuntimeException("Unexpected unary operator: " + stm.getOperator());
                            }
                        }
                        
                        @Override
                        public void caseReadThis(ReadThis stm) {
                        	// TODO: If we are in an event handler, this should return DOMNative
                            if (function.getOuterFunction() == null) {
                                linkOutput(stm, constant(GlobalObjectValue.Instance).getResult());
                            } else {
                                // TODO: Replace VarReadNode with a ReadThisNode
                                VarReadNode read = new VarReadNode("this", function);
                                read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                linkOutput(stm, read.getResult());
                                nodes.add(read);
                            }
                        }

                        private void handleReadHarnessVariable(ReadVariable stm) {
                            String varname = stm.getVarName();
                            if (varname.equals("$number") || varname.equals("$int")) {
                                linkOutput(stm, constant(NumberValue.Instance).getResult());
                            }
                            else if (varname.equals("$string")) {
                                linkOutput(stm, constant(StringValue.Instance).getResult());
                            }
                            else if (varname.equals("$bool")) {
                                linkOutput(stm, constant(BooleanValue.Instance).getResult());
                            }
                            else if (varname.equals("$dom")) {
                            	linkOutput(stm, constant(DOMNative.Instance).getResult());
                            }
                            else if (varname.equals("$global")) {
                            	linkOutput(stm, constant(GlobalObjectValue.Instance).getResult());
                            }
                            else if (varname.equals("$Object")) {
                                linkOutput(stm, constant(ObjectNative.Instance).getResult());
                            }
                            else if (varname.equals("$Function")) {
                                linkOutput(stm, constant(FunctionNative.Instance).getResult());
                            }
                            else if (varname.equals("$GetPrototypeOf")) {
                                linkOutput(stm, constant(ObjectGetPrototypeOfNative.Instance).getResult());
                            }
                            else if (varname.equals("$Create")) {
                                linkOutput(stm, constant(ObjectCreateNative.Instance).getResult());
                            }
                            else if (varname.equals("$Call")) {
                                linkOutput(stm, constant(FunctionCallNative.Instance).getResult());
                            }
                            else if (varname.equals("$Apply")) {
                                linkOutput(stm, constant(FunctionApplyNative.Instance).getResult());
                            }
                            else if (varname.equals("$Bind")) {
                                linkOutput(stm, constant(FunctionBindNative.Instance).getResult());
                            }
                            else if (varname.equals("$Eval")) {
                                linkOutput(stm, constant(EvalNative.Instance).getResult());
                            }
                            else {
                                throw new RuntimeException("Unknown harness variable " + varname);
                            }
                        }
                        @Override
                        public void caseReadVariable(ReadVariable stm) {
                            if (enableHarnessVars && stm.getVarName().startsWith("$")) {
                                handleReadHarnessVariable(stm);
                                return;
                            }
                            VariableAccess access = resolveVariableAccess(stm.getScope(), stm.getVarName());

                            for (WithScopeAccess wa : access.withAccesses) {
                                WithScope w = wa.scope;
                                int depth = wa.depth;
                                LoadNode load = new LoadNode(stm.getVarName());
                                if (depth == 0) {
                                    link(w.getStatement(), w.getStatement().getObjectVar(), load.getBase());
                                } else {
                                    InterscopeIdentityNode interscope = new InterscopeIdentityNode(depth);
                                    interscope.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    with2interscopes.add(w, interscope);
                                    load.getBase().addSource(interscope.getResult());
                                    nodes.add(interscope);
                                }
                                linkOutput(stm, load.getResult());
                                nodes.add(load);
                                binding.addReadVariable(stm, load, w);
                            }

                            if (access instanceof GlobalVariableAccess) {
                                VarReadGlobalNode read = new VarReadGlobalNode(stm.getVarName());
                                read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                linkOutput(stm, read.getResult());
                                nodes.add(read);
                                binding.addReadVariable(stm, read);
                            } else {
                                NonGlobalVariableAccess nonglobal = (NonGlobalVariableAccess)access;
                                if (nonglobal.depth == 0) {
                                    if (privateVars.getInnerAssignedVars(function).contains(stm.getVarName())) {
                                        VarReadNode read = localVarReads.get(stm.getVarName());
                                        if (read == null) {
                                            read = new VarReadNode(stm.getVarName(), nonglobal.scope);
                                            read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                            nodes.add(read);
                                            localVarReads.put(stm.getVarName(), read);
                                            binding.addReadVariable(stm, read);
                                        }
                                        linkOutput(stm, read.getResult());
                                    } else {
                                        optimizedReadVarStmts.add(stm);
                                    }
                                } else {
                                    VarReadInterscopeNode read = new VarReadInterscopeNode(stm.getVarName(), nonglobal.scope, nonglobal.depth);
                                    read.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    linkOutput(stm, read.getResult());
                                    nodes.add(read);
                                    binding.addReadVariable(stm, read);
                                }
                            }
                        }

                        @Override
                        public void caseDeclareVariable(DeclareVariable stm) {
                            // do nothing
                        }

                        @Override
                        public void caseEnterWith(EnterWith stm) {
                            StubNode stub = new StubNode(false);
                            link(stm.getObjectVar(), stub.getInput());
                            nodes.add(stub);
                            with2stub.put((WithScope)stm.getInnerScope(), stub);
                            binding.addWith(stm, stub);
                        }

                        @Override
                        public void caseExceptionalReturn(ExceptionalReturn stm) {
                            // do nothing
                        }

                        @Override
                        public void caseLeaveScope(LeaveScope stm) {
                            // do nothing
                        }

                        @Override
                        public void caseNop(Nop stm) {
                            // do nothing
                        }

                        @Override
                        public void caseReturn(Return stm) {
                            link(stm.getArgVar(), normalReturn.getValue());
                        }

                        @Override
                        public void caseReturnVoid(ReturnVoid stm) {
                        	normalReturn.getValue().addSource(constant(UndefinedValue.Instance).getResult());
                        }

                        @Override
                        public void caseThrow(Throw stm) {
                            Statement exhandler = findExceptionHandler(stm);
                            if (exhandler instanceof Catch) {
                                Catch cn = (Catch)exhandler;
                                IdentityNode in = new IdentityNode();
                                link(stm, stm.getArgVar(), in.getValue());
                                catchNodeSrcPoints.add(cn, in.getResult());
                                nodes.add(in);
                            } else if (exhandler instanceof ExceptionalReturn) {
                                link(stm, stm.getArgVar(), exceptionalReturn.getValue());
                            } else {
                                throw new RuntimeException("Unexpected exception handler: " + exhandler);
                            }
                        }

                        @Override
                        public void caseWriteProperty(WriteProperty stm) {
                        	CoerceToObject coerce = coerceToObject(stm.getBaseVar());
                            String propertyStr = getConstantString(stm, stm.getPropertyVar());
                            if (propertyStr != null) {
                                StoreNode store = new StoreNode(propertyStr);
                                store.getBase().addSource(coerce.getResult());
                                link(stm.getValueVar(), store.getValue());
                                nodes.add(store);
                                dataflow.getStores().add(stm, store);
                                binding.addWriteProperty(stm, store);
                            } else {
                                StoreDynamicNode store = new StoreDynamicNode();
                                store.getBase().addSource(coerce.getResult());
                                link(stm.getPropertyVar(), store.getProperty());
                                link(stm.getValueVar(), store.getValue());
                                nodes.add(store);
                                binding.addWriteProperty(stm, store);
                            }
                        }

                        @Override
                        public void caseWriteVariable(WriteVariable stm) {
                            VariableAccess access = resolveVariableAccess(stm.getScope(), stm.getVarName());

                            for (WithScopeAccess wa : access.withAccesses) {
                                WithScope ws = wa.scope;
                                int depth = wa.depth;
                                StoreIfPresentNode store = new StoreIfPresentNode(stm.getVarName());
                                if (depth == 0) {
                                    link(ws.getStatement(), ws.getStatement().getObjectVar(), store.getBase());
                                } else {
                                    InterscopeIdentityNode interscope = new InterscopeIdentityNode(depth);
                                    interscope.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    with2interscopes.add(ws, interscope);
                                    store.getBase().addSource(interscope.getResult());
                                    nodes.add(interscope);
                                }
                                link(stm.getValueVar(), store.getValue());
                                nodes.add(store);
                                binding.addWriteVariable(stm, store, ws);
                            }

                            if (access instanceof GlobalVariableAccess) {
                                VarWriteGlobalNode write = new VarWriteGlobalNode(stm.getVarName());
                                link(stm.getValueVar(), write.getValue());
                                nodes.add(write);
                                binding.addWriteVariable(stm, write);
                            } else {
                                NonGlobalVariableAccess nonglobal = (NonGlobalVariableAccess)access;
                                if (nonglobal.depth == 0) {
                                    if (privateVars.getInnerReadVars(function).contains(stm.getVarName())) {
                                        VarWriteNode write = localVarWrites.get(stm.getVarName());
                                        if (write == null) {
                                            write = new VarWriteNode(stm.getVarName(), nonglobal.scope);
                                            nodes.add(write);
                                            localVarWrites.put(stm.getVarName(), write);
                                        }
                                        link(stm.getValueVar(), write.getValue());
                                        binding.addWriteVariable(stm, write);
                                    }
                                } else {
                                    VarWriteInterscopeNode write = new VarWriteInterscopeNode(stm.getVarName(), nonglobal.scope, nonglobal.depth);
                                    link(stm.getValueVar(), write.getValue());
                                    write.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                    nodes.add(write);
                                    binding.addWriteVariable(stm, write);
                                }
                            }
                        }

                        @Override
                        public void caseAssertion(Assertion stm) {
                            // do nothing
                        }

                        @Override
                        public void caseEnterCatch(EnterCatch stm) {
                            // do nothing
                        }

                        private CoerceToObject coerceToObject(int var) {
                        	CoerceToObject coerce = new CoerceToObject();
                        	link(var, coerce.getArgument());
                        	nodes.add(coerce);
                        	return coerce;
                        }
                        private void coerceToPrimitive(int var) {
                            Statement exhandler = findExceptionHandler(stmt);
                            CoerceToPrimitive coerce = exhandler2coercion.get(exhandler);
                            if (coerce == null) {
                                coerce = new CoerceToPrimitive();
                                nodes.add(coerce);
                                exhandler2coercion.put(exhandler, coerce);

                                if (exhandler instanceof Catch) {
                                    Catch cn = (Catch)exhandler;
                                    catchNodeSrcPoints.add(cn, coerce.getExceptionalResult());
                                } else {
                                    exceptionalReturn.getValue().addSource(coerce.getExceptionalResult());
                                }
                            }
                            link(var, coerce.getValue());
                        }

                        private Statement findExceptionHandler(Statement stm) {
                            return stm.getBlock().getExceptionHandler().getFirst();
                        }

                        private String getConstantString(Statement stm, int tempVar) {
                            Set<VariableDefinition> defs = reachingDefs.getReachingDefinitions(stm, tempVar);
                            if (defs.size() != 1) {
                                return null;
                            }
                            VariableDefinition def = defs.iterator().next();
                            if (!(def instanceof StatementVariableDefinition)) {
                                return null;
                            }
                            StatementVariableDefinition stmdef = (StatementVariableDefinition)def;
                            if (stmdef.getStatement() instanceof StringConst) {
                                return ((StringConst)stmdef.getStatement()).getString();
                            } else {
                                return null;
                            }
                        }

                        private void linkOutput(Assignment asn, OutputPoint output) {
                            stmt2output.add(new StatementVariableDefinition(asn), output);
                            binding.addVariableOutputPoint(function, asn.getResultVar(), output);
                        }

                        private ConstNode constant(Value value) {
                            ConstNode c = constNodes.get(value);
                            if (c == null) {
                                c = new ConstNode(value);
                                c.getFunctionInstance().addSource(funcInstanceNode.getResult());
                                nodes.add(c);
                                constNodes.put(value, c);
                            }
                            return c;
                        }
                        private void link(int var, InputPoint input) {
                            link(stmt, var, input);
                        }
                        private void link(Statement node, int var, InputPoint input) {
                            if (input == null) {
                                throw new RuntimeException("input arg was null");
                            }
                            for (VariableDefinition def : reachingDefs.getReachingDefinitions(node, var)) {
                                stmt2dest.add(def, input);
                            }
                        }
                        private void link(String varName, Scope scope, InputPoint input) {
                            link(stmt, varName, scope, input);
                        }
                        private void link(Statement node, String varName, Scope scope, InputPoint input) {
                            if (input == null) {
                                throw new RuntimeException("input arg was null");
                            }
                            for (VariableDefinition def : reachingDefs.getReachingDefinitions(node, varName, scope)) {
                                stmt2dest.add(def, input);
                            }
                        }

                        private VariableAccess resolveVariableAccess(Scope scope, String varName) {
                            List<WithScopeAccess> withAccesses = new ArrayList<WithScopeAccess>();
                            int depth = 0;
                            while (scope != null) {
                                if (scope instanceof WithScope) {
                                    WithScope ws = (WithScope)scope;
                                    withAccesses.add(new WithScopeAccess(depth, ws));
                                } else if (scope instanceof Function) {
                                    Function f = (Function)scope;
                                    if (f == topLevel) {
                                        return new GlobalVariableAccess(withAccesses);
                                    }
                                    else if (f.getDeclaredVariables().contains(varName) || varName.equals("arguments")) {
                                        return new NonGlobalVariableAccess(withAccesses, depth, f);
                                    }
                                    depth++;
                                } else if (scope instanceof CatchScope) {
                                    if (scope.getDeclaredVariables().contains(varName)) {
                                        return new NonGlobalVariableAccess(withAccesses, depth, scope);
                                    }
                                } else {
                                    throw new RuntimeException("Unknown scope kind: " + scope.getClass());
                                }
                                scope = scope.getParentScope();
                            }
                            throw new RuntimeException("Loop did not encounter global scope");
                        }
                    });
                }
            } // end block loop

            // setup CatchNodes
            for (Catch cn : catchNodeSrcPoints.keySet()) {
                VariableDefinition def = new StatementVariableDefinition(cn);
                stmt2output.addAll(def, catchNodeSrcPoints.getView(cn));
            }

            // connect nodes
            for (VariableDefinition def : stmt2dest.keySet()) {
                for (InputPoint in : stmt2dest.getView(def)) {
                    for (OutputPoint out : stmt2output.getView(def)) {
                        in.addSource(out);
                    }
                }
            }
            
            // add bindings for the evaluation of ReadVariable statements that were captured by def-use
            for (ReadVariable readVarStmt : optimizedReadVarStmts) {
                for (VariableDefinition def : reachingDefs.getReachingDefinitions(readVarStmt, readVarStmt.getVarName(), readVarStmt.getScope().getDeclaringScope(readVarStmt.getVarName()))) {
                    for (OutputPoint op : stmt2output.getView(def)) {
                        binding.addVariableOutputPoint(function, readVarStmt.getResultVar(), op);
                    }
                }
            }
            // add bindings for phi nodes
            for (Phi phi : phiStmts) {
                for (VariableDefinition def : reachingDefs.getReachingDefinitions(phi, phi.getArg1Var())) {
                    for (OutputPoint op : stmt2output.getView(def)) {
                        binding.addVariableOutputPoint(function, phi.getResultVar(), op);
                    }
                }
                for (VariableDefinition def : reachingDefs.getReachingDefinitions(phi, phi.getArg2Var())) {
                    for (OutputPoint op : stmt2output.getView(def)) {
                        binding.addVariableOutputPoint(function, phi.getResultVar(), op);
                    }
                }
            }

            Set<FlowNode> reachable = DataflowUtil.getReachableFrom(funcInstanceNode);

            // run type analysis
            final AnalysisResult<OutputPoint, EnumSet<BasicType>> types = Solver.solve(new BasicTypeAnalysis(funcInstanceNode));
            for (FlowNode node : reachable) {
                node.apply(new AbstractFlowNodeVisitor() {
                    @Override
                    public void casePlus(PlusNode node) {
                        if (types.get(node.getResult()).size() != 1) {
                            return;
                        }
                        BasicType type = types.get(node.getResult()).iterator().next();
                        if (type == BasicType.STRING) {
                            replaceBy(node.getResult(), constant(BooleanValue.Instance).getResult());
                        } else {
                            replaceBy(node.getResult(), constant(NumberValue.Instance).getResult());
                        }
                    }
                    @Override
                    public void caseCoerceToPrimitive(CoerceToPrimitive node) {
                        for (OutputPoint op : new LinkedList<OutputPoint>(node.getValue().getSources())) {
                            Set<BasicType> typ = types.get(op);
                            if (typ == null || !typ.contains(BasicType.OBJECT)) {
                                node.getValue().removeSource(op);
                            }
                        }
                    }
                    private ConstNode constant(Value value) {
                        ConstNode c = constNodes.get(value);
                        if (c == null) {
                            c = new ConstNode(value);
                            c.getFunctionInstance().addSource(funcInstanceNode.getResult());
                            nodes.add(c);
                            constNodes.put(value, c);
                        }
                        return c;
                    }
                    private void replaceBy(OutputPoint op, OutputPoint newOp) {
                        for (InputPoint ip : op.getDestinations()) {
                            ip.addSource(newOp);
                        }
                        for (InputPoint ip : op.getPostfixDestinations()) {
                        	// note: postfix source don't actually exist at this point,
                        	// but we include this for good housekeeping
                        	ip.addPostfixSource(newOp);
                        }
                        op.clearDestinations();
                    }
                });
            }
            
            // remove dead nodes
            Set<FlowNode> live = getLiveNodes(reachable);
            for (FlowNode node : reachable) {
                if (!live.contains(node)) {
                    for (InputPoint ip : node.getInputPoints()) {
                        // currently disabled due to lack of stratification or explicit hotspots
                        // although the results are not needed for the fixpoint, the
                        // result may be needed by the user of the analysis (eg. rename refactoring)
//                    	ip.reduceAllSourcesToPostfixSources();
                    }
                }
            }
            
            binding.setDefUse(stmt2output, stmt2dest);
            
            dataflow.getFunctionFlownodes().addAll(function, nodes);
        } // end function loop

        // set interscope nodes
        for (WithScope w : with2interscopes.keySet()) {
            StubNode stub = with2stub.get(w);
            if (stub == null) {
                throw new RuntimeException("With stmt " + w.getStatement() + " did not get a stub node");
            }
            for (InterscopeIdentityNode id : with2interscopes.getView(w)) {
                id.setForeignInputPoint(stub.getInput());
            }
        }

    }

    class WithScopeAccess {
        int depth;
        WithScope scope;
        public WithScopeAccess(int depth, WithScope scope) {
            this.depth = depth;
            this.scope = scope;
        }
    }
    abstract class VariableAccess {
        List<WithScopeAccess> withAccesses;
        public VariableAccess(List<WithScopeAccess> withAccesses) {
            this.withAccesses = withAccesses;
        }
    }
    class GlobalVariableAccess extends VariableAccess {
        public GlobalVariableAccess(List<WithScopeAccess> withAccesses) {
            super(withAccesses);
        }
    }
    class NonGlobalVariableAccess extends VariableAccess {
        int depth;
        Scope scope;
        public NonGlobalVariableAccess(List<WithScopeAccess> withAccesses, int depth, Scope scope) {
            super(withAccesses);
            this.depth = depth;
            this.scope = scope;
        }
    }

    private Set<FlowNode> getLiveNodes(Set<FlowNode> reachable) {
        Set<FlowNode> live = new HashSet<FlowNode>();
        LinkedList<FlowNode> queue = new LinkedList<FlowNode>();
        for (FlowNode node : reachable) {
            if (!node.isPurelyLocal()) {
                live.add(node);
                queue.add(node);
            }
        }
        while (!queue.isEmpty()) {
            FlowNode node = queue.removeFirst();
            for (InputPoint ip : node.getInputPoints()) {
                for (OutputPoint op : ip.getSources()) {
                    if (live.add(op.getFlowNode())) {
                        queue.add(op.getFlowNode());
                    }
                }
            }
        }
        return live;
    }

}
