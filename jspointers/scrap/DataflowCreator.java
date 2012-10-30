package dk.brics.jspointers;

import java.awt.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jsparser.node.Node;
import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.CoerceToPrimitive;
import dk.brics.jspointers.dataflow.ConstNode;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.FunctionInstanceNode;
import dk.brics.jspointers.dataflow.IdentityNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InitializeNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InterscopeIdentityNode;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadDynamicNode;
import dk.brics.jspointers.dataflow.LoadNode;
import dk.brics.jspointers.dataflow.NativeCallNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.dataflow.PlusNode;
import dk.brics.jspointers.dataflow.ReturnNode;
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
import dk.brics.jspointers.flowgraph.analysis.Liveness;
import dk.brics.jspointers.flowgraph.analysis.TypeAnalysis;
import dk.brics.jspointers.lattice.values.ApplyFunctionValue;
import dk.brics.jspointers.lattice.values.BooleanValue;
import dk.brics.jspointers.lattice.values.CallFunctionValue;
import dk.brics.jspointers.lattice.values.DOMObjectValue;
import dk.brics.jspointers.lattice.values.FunctionFunctionValue;
import dk.brics.jspointers.lattice.values.NullValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.ObjectFunctionValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UndefinedValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jsutil.MultiMap;

public class DataflowCreator {
    public static DataflowGraph createDataflowGraph(Collection<Function> topLevels, Collection<Function> harness) {
        final MultiMap<Assignment, OutputPoint> stmt2output = new MultiMap<Assignment, OutputPoint>();
        final MultiMap<Assignment, InputPoint> stmt2dest = new MultiMap<Assignment, InputPoint>();
        final MultiMap<WithScope, InterscopeIdentityNode> with2interscopes = new MultiMap<WithScope, InterscopeIdentityNode>();
        final Map<WithScope, StubNode> with2stub = new HashMap<WithScope, StubNode>();
        
        final DataflowGraph dataflow = new DataflowGraph(topLevels);
        
        // create the initializer node
        InitializeNode init = new InitializeNode();
        dataflow.getNodes().add(init);
        dataflow.setInitializer(init);
        
        for (final Function topLevel : topLevels) {
        	PrivateVariables privateVars = new PrivateVariables(topLevel);
	        for (final Function function : topLevel.getTransitiveInnerFunctions(true)) {
	        	final Liveness liveness = new Liveness(fgd, function, privateVars);
	            final ReachingDefs reachingDefs = new ReachingDefs(fgd, function, liveness, privateVars);
	            final TypeAnalysis ta = new TypeAnalysis(fgd, function, liveness);
	            final Liveness live = new Liveness(fgd, function, privateVars, ta);
	            if (flowgraph.getHarnessFunctions().contains(function) && function.getName() != null) {
	            	dataflow.getNamedHarnessFunctions().put(function.getName(), function);
	            }
	            
	            final LinkedList<FlowNode> nodes = new LinkedList<FlowNode>();
	            
	            // maps a CatchNode to all the output points it can take exception instances from
	            final MultiMap<CatchNode,OutputPoint> catchNodeSrcPoints = new MultiMap<CatchNode, OutputPoint>();
	            
	            // only create one var read/write per local variable
	            final Map<String, VarReadNode> localVarReads = new HashMap<String, VarReadNode>();
	            final Map<String, VarWriteNode> localVarWrites = new HashMap<String, VarWriteNode>();
	            
	            
	            // create some unique nodes for this function
	            // FunctionInstanceNode
	            final FunctionInstanceNode funcInstanceNode = new FunctionInstanceNode(function);
	            nodes.add(funcInstanceNode);
	            dataflow.getFunctionInstanceFlowNodes().put(function, funcInstanceNode);
	            // normal ReturnNode
	            final ReturnNode normalReturn = new ReturnNode(function, false);
	            nodes.add(normalReturn);
	            dataflow.getNormalReturns().put(function, normalReturn);
	            // exceptional ReturnNode
	            final ReturnNode exceptionalReturn = new ReturnNode(function, true);
	            nodes.add(exceptionalReturn);
	            dataflow.getExceptionalReturns().put(function, exceptionalReturn);
	            // coercion node for each exception handler
	            final Map<Node,CoerceToPrimitive> exhandler2coercion = new HashMap<Node, CoerceToPrimitive>();
	            
	            // create parameter VarReadNodes for private parameters
	            for (int i=0; i<function.getParameterNames().size(); i++) {
	            	String name = function.getParameterNames().get(i);
	            	if (privateVars.contains(name)) {
	            		VarReadNode read = new VarReadNode(name, function);
	            		read.getFunctionInstance().addSource(funcInstanceNode.getResult());
	            		localVarReads.put(name, read);
	            		nodes.add(read);
	            		
	            		stmt2output.add(new ParameterAssignment(function, i), read.getResult());
	            	}
	            }
	            
	            // convert all statements in function body
	            for (BasicBlock block : function.getBlocks()) {
	            	final boolean isHarness = flowgraph.getHarnessFunctions().contains(function) || flowgraph.getToplevelHarnessBlocks().contains(block);
	                for (final Node node : block.getNodes()) {
	                    node.visitBy(new AbstractNodeVisitor() {
	                    	private void coerceToPrimitive(int var) {
	                    		// TODO make TypeAnalysis distinguish strings and objects
	                    		if (!ta.getVariables(node).contains(var))
	                    			return; // cannot be object, no need to coerce
	                    		Node exhandler = findExceptionHandler(node);
	                    		CoerceToPrimitive coerce = exhandler2coercion.get(exhandler);
	                    		if (coerce == null) {
	                    			coerce = new CoerceToPrimitive();
	                    			nodes.add(coerce);
	                    			exhandler2coercion.put(exhandler, coerce);
	                    			
	                    			if (exhandler instanceof CatchNode) {
	                    				CatchNode cn = (CatchNode)exhandler;
	                    				catchNodeSrcPoints.add(cn, coerce.getExceptionalResult());
	                    			} else {
	                    				exceptionalReturn.getValue().addSource(coerce.getExceptionalResult());
	                    			}
	                    		}
	                    		link(var, coerce.getValue());
	                    	}
	                    	private ConstNode constant(Value value) {
	                    		ConstNode c = new ConstNode(value, function);
	                    		c.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                    		nodes.add(c);
	                    		return c;
	                    	}
	                    	private Node findExceptionHandler(Node node) {
	                    		BasicBlock exblock = node.getBlock().getExceptionHandler();
	                    		while (exblock != null) {
	                    			Node first = exblock.getFirstNode();
	                    			if (first instanceof ExceptionalReturnNode || first instanceof CatchNode) {
	                    				return first;
	                    			}
	                    			exblock = exblock.getExceptionHandler();
	                    		}
	                    		return (ExceptionalReturnNode)function.getExceptionalExit().getFirstNode();
	                    	}
	                    	private void linkOutput(AssignmentNode asn, OutputPoint output) {
	                    		stmt2output.add(new TempVarAssignment(asn), output);
	                    	}
	                        @Override
	                        public void visit(ReadPropertyNode n, Void a) {
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            if (n.getPropertyStr() != null) {
	                                LoadNode load = new LoadNode(n.getPropertyStr());
	                                link(n.getBaseVar(), load.getBase());
	                                linkOutput(n, load.getResult());
	                                nodes.add(load);
	                            } else {
	                                LoadDynamicNode load = new LoadDynamicNode();
	                                link(n.getBaseVar(), load.getBase());
	                                link(n.getPropertyVar(), load.getProperty());
	                                linkOutput(n, load.getResult());
	                                nodes.add(load);
	                            }
	                        }
	                        @Override
	                        public void visit(WritePropertyNode n, Void a) {
	                            if (n.getPropertyStr() != null) {
	                                StoreNode store = new StoreNode(n.getPropertyStr());
	                                link(n.getBaseVar(), store.getBase());
	                                link(n.getValueVar(), store.getValue());
	                                nodes.add(store);
	                            } else {
	                                StoreDynamicNode store = new StoreDynamicNode();
	                                link(n.getBaseVar(), store.getBase());
	                                link(n.getPropertyVar(), store.getProperty());
	                                link(n.getValueVar(), store.getValue());
	                                nodes.add(store);
	                            }
	                        }
	                        private void handleReadHarnessVariable(ReadVariableNode n) {
	                            String varname = n.getVarName();
	                            if (varname.equals("$number") || varname.equals("$int")) {
	                            	linkOutput(n, constant(NumberValue.Instance).getOutput());
	                            }
	                            else if (varname.equals("$string")) {
	                            	linkOutput(n, constant(StringValue.Instance).getOutput());
	                            }
	                            else if (varname.equals("$bool")) {
	                            	linkOutput(n, constant(BooleanValue.Instance).getOutput());
	                            }
	                            else {
	                                throw new RuntimeException("Unknown harness variable " + varname);
	                            }
	                        }
	                        @Override
	                        public void visit(EnterWithNode n, Void a) {
	                        	StubNode stub = new StubNode();
	                        	link(n.getObjectVar(), stub.getInput());
	                        	nodes.add(stub);
	                        	with2stub.put(new WithScope(n), stub);
	                        }
	                        
	                        @Override
	                        public void visit(ReadVariableNode n, Void a) {
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            if (isHarness && n.getVarName().startsWith("$")) {
	                                handleReadHarnessVariable(n); // in the harness files, treat variables starting with $ specially
	                                return;
	                            }
	                            if (!isHarness && n.getVarName().startsWith("$")) {
	                            	System.out.printf("Non-harness variable %s at %s\n", n.getVarName(), n.getSourceLocation());
	                            }
	                            Scope scope = scopes.getScopeOfStmt(n);
	                            boolean definite = false;
	                            int depth = 0;
	                            while (scope != null && !definite) {
	                                if (scope instanceof WithScope) {
	                                    WithScope w = (WithScope)scope;
	                                    LoadNode load = new LoadNode(n.getVarName());
	                                    if (depth == 0) {
		                                    link(w.getNode(), w.getNode().getObjectVar(), load.getBase());
	                                    } else {
	                                    	InterscopeIdentityNode interscope = new InterscopeIdentityNode(depth);
	                                    	interscope.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                                    	with2interscopes.add(w, interscope);
	                                    	load.getBase().addSource(interscope.getResult());
	                                    	nodes.add(interscope);
	                                    }
	                                    linkOutput(n, load.getResult());
	                                    nodes.add(load);
	                                } else if (scope instanceof FunctionScope) {
	                                    FunctionScope f = (FunctionScope)scope;
	                                    if (f.getFunction().getOuterFunction() == null) {
	                                    	VarReadGlobalNode read = new VarReadGlobalNode(n.getVarName());
	                                    	read.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                                    	linkOutput(n, read.getResult());
	                                    	nodes.add(read);
	                                    	definite = true;
	                                    }
	                                    else if (scopeVars.contains(scope, n.getVarName())) {
	                                    	if (depth == 0) {
	                                    		VarReadNode read = localVarReads.get(n.getVarName());
	                                    		if (read == null) {
	                                    			read = new VarReadNode(n.getVarName(), function);
	                                    			read.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                                    			nodes.add(read);
	                                    			localVarReads.put(n.getVarName(), read);
	                                    		}
	                                    		linkOutput(n, read.getResult());
	                                    	} else {
	                                    		VarReadInterscopeNode read = new VarReadInterscopeNode(n.getVarName(), f.getFunction(), depth);
	                                    		read.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                                    		linkOutput(n, read.getResult());
	                                            nodes.add(read);
	                                    	}
	                                        definite = true;
	                                    }
	                                    depth++;
	                                } else {
	                                    throw new RuntimeException("Unknown scope kind: " + scope.getClass());
	                                }
	                                scope = scopes.getParentScope(scope);
	                            }
	                        }
	                        @Override
	                        public void visit(WriteVariableNode n, Void a) {
	                            Scope scope = scopes.getScopeOfStmt(n);
	                            boolean definite = false;
	                            int depth = 0;
	                            while (scope != null && !definite) {
	                                if (scope instanceof WithScope) {
	                                    WithScope ws = (WithScope)scope;
	                                    StoreIfPresentNode store = new StoreIfPresentNode(n.getVarName());
	                                    if (depth == 0) {
	                                    	link(ws.getNode(), ws.getNode().getObjectVar(), store.getBase());
	                                    } else {
	                                    	InterscopeIdentityNode interscope = new InterscopeIdentityNode(depth);
	                                    	interscope.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                                    	with2interscopes.add(ws, interscope);
	                                    	store.getBase().addSource(interscope.getResult());
	                                    	nodes.add(interscope);
	                                    }
	                                    link(n.getValueVar(), store.getValue());
	                                    nodes.add(store);
	                                } else if (scope instanceof FunctionScope) {
	                                    FunctionScope f = (FunctionScope)scope;
	                                    if (f.getFunction() == graph.getMain()) {
	                                    	VarWriteGlobalNode write = new VarWriteGlobalNode(n.getVarName());
	                                    	link(n.getValueVar(), write.getValue());
	                                    	nodes.add(write);
	                                    }
	                                    else if (scopeVars.contains(scope, n.getVarName())) {
	                                    	if (depth == 0) {
	                                    		VarWriteNode write = localVarWrites.get(n.getVarName());
	                                    		if (write == null) {
	                                    			write = new VarWriteNode(n.getVarName(), function);
	                                        		nodes.add(write);
	                                        		localVarWrites.put(n.getVarName(), write);
	                                    		}
	                                    		link(n.getValueVar(), write.getValue());
	                                    	} else {
		                                        VarWriteInterscopeNode write = new VarWriteInterscopeNode(n.getVarName(), f.getFunction(), depth);
		                                        link(n.getValueVar(), write.getValue());
		                                        write.getFunctionInstance().addSource(funcInstanceNode.getResult());
		                                        nodes.add(write);
	                                    	}
	                                        definite = true;
	                                    }
	                                    depth++;
	                                } else {
	                                    throw new RuntimeException("Unknown scope kind: " + scope.getClass());
	                                }
	                                scope = scopes.getParentScope(scope);
	                            }
	                        }
	                        @Override
	                        public void visit(ConstantNode n, Void a) {
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            switch (n.getType()) {
	                            case NUMBER:
	                            	linkOutput(n, constant(NumberValue.Instance).getOutput());
	                                break;
	                            case STRING:
	                            	linkOutput(n, constant(StringValue.Instance).getOutput());
	                                break;
	                            case BOOLEAN:
	                            	linkOutput(n, constant(BooleanValue.Instance).getOutput());
	                                break;
	                            case NULL:
	                            	linkOutput(n, constant(NullValue.Instance).getOutput());
	                                break;
	                            case UNDEFINED:
	                            	linkOutput(n, constant(UndefinedValue.Instance).getOutput());
	                                break;
	                            case FUNCTION:
	                                InitializeFunctionNode init = new InitializeFunctionNode(n.getFunction());
	                                init.getOuterFunction().addSource(funcInstanceNode.getResult());
	                                nodes.add(init);
	                                linkOutput(n, init.getResult());
	                                break;
	                            default:
	                                throw new RuntimeException("Unknown constant kind: " + n.getType());
	                            }
	                        }
	                        @Override
	                        public void visit(DeclareFunctionNode n, Void a) {
	                            InitializeFunctionNode init = new InitializeFunctionNode(n.getFunction());
	                            init.getOuterFunction().addSource(funcInstanceNode.getResult());
	                            nodes.add(init);
	                            linkOutput(n, init.getResult());
	                            // write function to scope
	                            if (n.getFunction().getName() != null) {
	                            	if (function.getOuterFunction() == null) {
	                            		VarWriteGlobalNode write = new VarWriteGlobalNode(n.getFunction().getName());
	                            		write.getValue().addSource(init.getResult());
	                            		nodes.add(write);
	                            	} else {
		                                VarWriteNode write = new VarWriteNode(n.getFunction().getName(), function);
		                                write.getValue().addSource(init.getResult());
		                                nodes.add(write);
	                            	}
	                            }
	                        }
	                        private void addBinopCoercions(BinaryOperatorNode n) {
	                        	switch (n.getOperator()) {
	                        	case IN:
	                        	case INSTANCEOF:
	                        		return; // no primitive coercion
	                        	}
	                        	coerceToPrimitive(n.getArg1Var());
	                        	coerceToPrimitive(n.getArg2Var());
	                        }
	                        @Override
	                        public void visit(BinaryOperatorNode n, Void a) {
	                        	addBinopCoercions(n);
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            switch (n.getOperator()) {
	                            case ADD:
	                                if (ta.getVariables(n).contains(n.getArg1Var()) || ta.getVariables(n).contains(n.getArg2Var())) {
	                                    PlusNode plus = new PlusNode();
	                                    link(n.getArg1Var(), plus.getArgument());
	                                    link(n.getArg2Var(), plus.getArgument());
	                                    linkOutput(n, plus.getOutput());
	                                    nodes.add(plus);
	                                } else {
	                                	linkOutput(n, constant(NumberValue.Instance).getOutput());
	                                }
	                                break;
	                            case EQ: // definitely boolean result
	                            case SEQ:
	                            case NE:
	                            case SNE:
	                            case GT:
	                            case GE:
	                            case LT:
	                            case LE:
	                            case INSTANCEOF:
	                            case IN:
	                            	linkOutput(n, constant(BooleanValue.Instance).getOutput());
	                                break;
	                            case AND: // definitely number result
	                            case OR:
	                            case XOR:
	                            case DIV:
	                            case MUL:
	                            case SUB:
	                            case USHR:
	                            case SHR:
	                            case SHL:
	                            case REM:
	                            	linkOutput(n, constant(NumberValue.Instance).getOutput());
	                                break;
	                            default:
	                                throw new RuntimeException("Unexpected binary operator: " + n.getOperator());
	                            }
	                        }
	                        @Override
	                        public void visit(UnaryOperatorNode n, Void a) {
	                        	coerceToPrimitive(n.getArgVar());
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            switch (n.getOperator()) {
	                            case NOT: // boolean result
	                            	linkOutput(n, constant(BooleanValue.Instance).getOutput());
	                                break;
	                            case MINUS: // number result
	                            case PLUS:
	                            case COMPLEMENT:
	                            	linkOutput(n, constant(NumberValue.Instance).getOutput());
	                                break;
	                            default:
	                                throw new RuntimeException("Unexpected unary operator: " + n.getOperator());
	                            }
	                        }
	                        @Override
	                        public void visit(TypeofNode n, Void a) {
	                            if (!live.isLiveAfter(n, n.getResultVar()))
	                                return;
	                            linkOutput(n, constant(StringValue.Instance).getOutput());
	                        }
	                        private boolean tryHandleCallNodeAsLoadAndInvoke(CallNode n) {
	                        	Set<Assignment> asns = reachingDefs.getReachingDefsForTempVar(n, n.getFunctionVar());
	                        	if (asns.size() != 1)
	                        		return false;
	                        	Assignment asn = asns.iterator().next();
	                        	if (!(asn instanceof TempVarAssignment))
	                        	    return false;
	                        	TempVarAssignment tasn = (TempVarAssignment)asn;
	                        	if (!(tasn.getNode() instanceof ReadPropertyNode))
	                        		return false;
	                        	ReadPropertyNode read = (ReadPropertyNode)tasn.getNode();
	                        	if (read.getPropertyStr() == null)
	                        		return false;
	                        	if (!reachingDefs.getReachingDefsForTempVar(read, read.getBaseVar()).equals(reachingDefs.getReachingDefsForTempVar(n, n.getBaseVar())))
	                        		return false;
	                        	LoadAndInvokeNode loadAndInvoke = new LoadAndInvokeNode(read.getPropertyStr(), n.getNumberOfArgs(), n);
	                        	link(read.getBaseVar(), loadAndInvoke.getBase());
	                        	for (int i=0; i<n.getNumberOfArgs(); i++) {
	                        		link(n.getArgVar(i), loadAndInvoke.getArguments().get(i));
	                        	}
	                        	InvokeResultNode res = new InvokeResultNode(false, loadAndInvoke);
	                        	link(n.getFunctionVar(), res.getFunc());
	                        	link(read.getBaseVar(), res.getThisArg()); // TODO: Refactor InvokeResult to share more with its IInvocation
	                            linkOutput(n, res.getResult());
	                            // link exceptional result
	                            Node exhandler = findExceptionHandler(n);
	                            if (exhandler instanceof CatchNode) {
	                            	CatchNode cn = (CatchNode)exhandler;
	                            	catchNodeSrcPoints.add(cn, res.getExceptionalResult());
	                            } else if (exhandler instanceof ExceptionalReturnNode) {
	                            	exceptionalReturn.getValue().addSource(res.getExceptionalResult());
	                            } else {
	                            	throw new RuntimeException("Unexpected exception handler: " + exhandler);
	                            }
	                        	nodes.add(loadAndInvoke);
	                        	nodes.add(res);
	                        	dataflow.getCalls().add(n, loadAndInvoke);
	                        	return true;
	                        }
	                        @Override
	                        public void visit(CallNode n, Void a) {
	                        	if (tryHandleCallNodeAsLoadAndInvoke(n)) {
	                        		return;
	                        	}
	                        	
	                        	// FIXME: Fix the Rhino->FlowGraph converter to get a more robust way of detecting omitted this arg!
	                        	boolean omittedThisArg = reachingDefs.getReachingDefsForTempVar(n, n.getBaseVar()).isEmpty();
	                        	Object callsiteId = n;
	                            InvokeNode invoke = new InvokeNode(n.getNumberOfArgs(), n.isConstructorCall(), omittedThisArg, callsiteId);
	                            InvokeResultNode res = new InvokeResultNode(n.isConstructorCall(), invoke);
	                            
	                            // link this arg
	                            if (!n.isConstructorCall()) {
	                                link(n.getBaseVar(), invoke.getBase());
	                            } else {
	                            	AllocNode alloc = new AllocNode(n, function);
	                            	invoke.getBase().addSource(alloc.getResult());
	                            	res.getThisArg().addSource(alloc.getResult());
	                            	alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                            	nodes.add(alloc);
	                            }
	                            
	                            // link function arg
	                            link(n.getFunctionVar(), invoke.getFunc());
	                            link(n.getFunctionVar(), res.getFunc());
	                            
	                            // link actual arguments
	                            for (int i=0; i<n.getNumberOfArgs(); i++) {
	                                link(n.getArgVar(i), invoke.getArguments().get(i));
	                            }
	                            
	                            // link normal result
	                            linkOutput(n, res.getResult());
	                            
	                            // link exceptional result
	                            Node exhandler = findExceptionHandler(n);
	                            if (exhandler instanceof CatchNode) {
	                            	CatchNode cn = (CatchNode)exhandler;
	                            	catchNodeSrcPoints.add(cn, res.getExceptionalResult());
	                            } else if (exhandler instanceof ExceptionalReturnNode) {
	                            	exceptionalReturn.getValue().addSource(res.getExceptionalResult());
	                            } else {
	                            	throw new RuntimeException("Unexpected exception handler: " + exhandler);
	                            }
	                            
	//                            call2node.put(n, invoke);
	                            nodes.add(invoke);
	                            nodes.add(res);
	                            dataflow.getCalls().add(n, invoke);
	                        }
	                        @Override
	                        public void visit(dk.brics.tajs.flowgraph.nodes.ReturnNode n, Void a) {
	                            if (n.getValueVar() != Node.NO_VALUE) {
	                            	if (!isHarness) {
	                            		System.out.print("");
	                            	}
	                            	link(n.getValueVar(), normalReturn.getValue());
	                            }
	                        }
	                        @Override
	                        public void visit(NewObjectNode n, Void a) {
	                        	AllocNode alloc = new AllocNode(n, function);
	                        	linkOutput(n, alloc.getResult());
	                        	alloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
	                        	nodes.add(alloc);
	                        }
	                        
	                        @Override
	                        public void visit(ThrowNode n, Void a) {
	                        	Node exhandler = findExceptionHandler(n);
	                        	if (exhandler instanceof CatchNode) {
	                        		CatchNode cn = (CatchNode)exhandler;
	                        		IdentityNode in = new IdentityNode();
	                        		link(n, n.getValueVar(), in.getValue());
	                        		catchNodeSrcPoints.add(cn, in.getResult());
	                        		nodes.add(in);
	                        	} else if (exhandler instanceof ExceptionalReturnNode) {
	                        		link(n, n.getValueVar(), exceptionalReturn.getValue());
	                        	} else {
	                        		throw new RuntimeException("Unexpected exception handler: " + exhandler);
	                        	}
	                        }
	
	                        private void link(int var, InputPoint input) {
	                            link(node, var, input);
	                        }
	                        private void link(Node node, int var, InputPoint input) {
	                            if (input == null)
	                                throw new RuntimeException("input arg was null");
	                            for (Assignment asn : reachingDefs.getReachingDefsForTempVar(node, var)) {
	                                stmt2dest.add(asn, input);
	                            }
	                        }
	                    }, null);
	                }
	            } // end of block loop
	            
	            // setup CatchNodes
	            for (CatchNode cn : catchNodeSrcPoints.keySet()) {
	            	Assignment an = new TempVarAssignment(cn);
	            	if (cn.getTempVar() != Node.NO_VALUE) {
	            		// the exception is stored in the temporary variable
	            		stmt2output.addAll(an, catchNodeSrcPoints.getView(cn));
	            	} else {
	            		// an object 'ScopeObj' is allocated, and the exception is stored as a property on that
	            		// with the name of the property being the variable name of the CatchNode
	            		// This type of CatchNode is followed by a 'with' statement to model the catch block's scope
	            		AllocNode scopeObjAlloc = new AllocNode(cn, function);
	            		scopeObjAlloc.getFunctionInstance().addSource(funcInstanceNode.getResult());
	            		StoreNode store = new StoreNode(cn.getVarName());
	            		store.getBase().addSource(scopeObjAlloc.getResult());
	            		for (OutputPoint op : catchNodeSrcPoints.getView(cn)) {
	            			store.getValue().addSource(op);
	            		}
	            		stmt2output.add(an, scopeObjAlloc.getResult());
	            		nodes.add(scopeObjAlloc);
	            		nodes.add(store);
	            	}
	            }
	            
	            dataflow.getNodes().addAll(nodes);
	            dataflow.getFunctionFlownodes().addAll(function, nodes);
	        } // end of function loop
        } // end of toplevel loop
        
        // connect nodes
        for (Assignment asn : stmt2dest.keySet()) {
            for (InputPoint in : stmt2dest.getView(asn)) {
                for (OutputPoint out : stmt2output.getView(asn)) {
                    in.addSource(out);
                }
            }
        }
        // set interscope nodes
        for (WithScope w : with2interscopes.keySet()) {
        	StubNode stub = with2stub.get(w);
        	if (stub == null)
        		throw new RuntimeException("With stmt " + w.getNode().getSourceLocation() + " does not get a stub node");
        	for (InterscopeIdentityNode id : with2interscopes.getView(w)) {
        		id.setForeignInputPoint(stub.getInput());
        	}
        }
        
        // create native flow nodes [TODO less error-prone way to do this?]
        dataflow.getNativeFlowNodes().put(CallFunctionValue.Instance, new NativeCallNode(CallFunctionValue.Instance));
        dataflow.getNativeFlowNodes().put(ObjectFunctionValue.Instance, new NativeCallNode(ObjectFunctionValue.Instance));
        dataflow.getNativeFlowNodes().put(FunctionFunctionValue.Instance, new NativeCallNode(FunctionFunctionValue.Instance));
        dataflow.getNativeFlowNodes().put(ApplyFunctionValue.Instance, new NativeCallNode(ApplyFunctionValue.Instance));
        dataflow.getNativeFlowNodes().put(DOMObjectValue.Instance, new NativeCallNode(DOMObjectValue.Instance));
        dataflow.getNodes().addAll(dataflow.getNativeFlowNodes().values());
        
        return dataflow;
    }
}
