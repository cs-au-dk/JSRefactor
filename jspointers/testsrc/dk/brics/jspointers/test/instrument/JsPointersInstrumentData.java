package dk.brics.jspointers.test.instrument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jsparser.analysis.Analysis;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.IExpOrStmt;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jspointers.Main;
import dk.brics.jspointers.Main.InputFile;
import dk.brics.jspointers.dataflow.AbstractFlowNodeVisitor;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.keys.VariableKey;
import dk.brics.jspointers.lattice.values.AllocObjectValue;
import dk.brics.jspointers.lattice.values.ArgumentsArrayValue;
import dk.brics.jspointers.lattice.values.FunctionPrototypeValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.parametric.StatementAllocSite;
import dk.brics.jsutil.MultiMap;

public class JsPointersInstrumentData implements InstrumentData {
	private Main main;
	private Map<Statement,AllocSite> stmt2alloc = new HashMap<Statement, AllocSite>();

	public JsPointersInstrumentData(final Main main) {
		this.main = main;
		
		visitUserCode(new DepthFirstAdapter() {
			@Override
			public void inAObjectLiteralExp(AObjectLiteralExp node) {
				doAlloc(node, new ObjLiteralAllocSite(node));
			}
			@Override
			public void inANewExp(ANewExp node) {
				doAlloc(node, new NewExpAllocSite(node));
			}
			@Override
			public void inAArrayLiteralExp(AArrayLiteralExp node) {
				doAlloc(node, new ArrayLiteralAllocSite(node));
			}
			@Override
			public void inARegexpExp(ARegexpExp node) {
				doAlloc(node, new RegExpAllocSite(node));
			}
			@Override
			public void inAFunctionExp(AFunctionExp node) {
				handleFunction(node);
			}
			@Override
			public void inAFunctionDeclStmt(AFunctionDeclStmt node) {
				handleFunction(node);
			}
			void handleFunction(IFunction func) {
				for (CreateFunction stmt : main.getAstBinding().getFunctions(func)) {
					stmt2alloc.put(stmt, new FunctionAllocSite(func));
				}
			}
			void doAlloc(PExp exp, AllocSite site) {
				for (Assignment stmt : main.getAstBinding().getAllocation(exp)) {
					stmt2alloc.put(stmt, site);
				}
			}
		});
	}
	
	void visitUserCode(Analysis v) {
		for (InputFile file : main.getUserFiles()) {
			file.getAst().apply(v);
		}
	}
	

	@Override
	public boolean isNative(NodeInterface node) {
		Start start = node.getAncestor(Start.class);
		for (InputFile in : main.getHarness()) {
			if (in.getAst() == start)
				return true;
		}
		return false;
	}
	@Override
	public Set<IFunction> getTargets(AInvokeExp invoke) {
		final Set<IFunction> result = new HashSet<IFunction>();
		for (InvokeStatement stmt : main.getAstBinding().getInvokeStatements(invoke)) {
			for (IInvocationFlowNode flow : main.getDataflowBinding().getInvoke(stmt)) {
				flow.apply(new AbstractFlowNodeVisitor() {
					@Override
					public void caseInvoke(InvokeNode node) {
						visitValues(main.lookupIns(node.getFunc(), UserFunctionValue.class));
					}
					@Override
					public void caseLoadAndInvoke(LoadAndInvokeNode node) {
						Set<ObjectValue> directRcv = main.lookupIns(node.getBase(), ObjectValue.class);
						Set<ObjectValue> base = main.getAllPrototypes(directRcv, true);
						for (ObjectValue obj : base) {
							visitValues(main.lookupSens(new NamedPropertyKey(obj, node.getProperty()), UserFunctionValue.class));
							visitValues(main.lookupSens(obj.getDynamicStoreProperty(), UserFunctionValue.class));
						}
					}
					private void visitValues(Set<UserFunctionValue> funcs) {
						for (UserFunctionValue uf : funcs) {
							result.add(main.getAstBinding().getFunctionNode(uf.getFunction()));
						}
					}
				});
			}
		}
		return result;
	}
	@Override
	public Set<AllocSite> getResultAllocationSites(PExp exp) {
		return getAllocSites(main.objects(exp));
	}
	@Override
	public Set<AllocSite> getVariableAllocationSites(IScopeBlockNode scope, String var, IExpOrStmt afterThisGuy) {
		IFunction func = scope.getAncestor(IFunction.class);
		if (func == null) {
			return getAllocSites(main.lookupSens(new NamedPropertyKey(GlobalObjectValue.Instance, var), ObjectValue.class));
		} else {
			Set<AllocSite> result = new HashSet<AllocSite>();
			Scope scop = main.getAstBinding().getScope(scope);
			Function function = main.getAstBinding().getFunction(func);
			ReachingDefinitions reachingDefinitions = main.getDataflowBinding().getReachingDefinitions(function);
			for (Statement stmt : main.getAstBinding().getCompletionPoint(afterThisGuy)) {
				for (VariableDefinition def : reachingDefinitions.getReachingDefinitions(stmt, var, scop)) {
					for (OutputPoint op : main.getDataflowBinding().getDefinitions().getView(def)) {
						result.addAll(getAllocSites(main.lookupIns(op, ObjectValue.class)));
					}
				}
			}
			result.addAll(getAllocSites(main.lookupIns(new VariableKey(var, main.getAstBinding().getScope(scope), NullContext.Instance), ObjectValue.class)));
			return result;
		}
		
	}
	@Override
	public Set<Start> getAst() {
		Set<Start> asts = new HashSet<Start>();
		for (InputFile file : main.getAllInputs()) {
			asts.add(file.getAst());
		}
		return asts;
	}
	
	public Set<AllocSite> getAllocSites(Set<? extends ObjectValue> objects) {
		Set<AllocSite> result = new HashSet<AllocSite>();
		for (ObjectValue obj : objects) {
			if (obj instanceof AllocObjectValue) {
				AllocObjectValue al = (AllocObjectValue) obj;
				StatementAllocSite site = (StatementAllocSite) al.getAllocsite();
				if (stmt2alloc.containsKey(site.getStatement())) {
					result.add(stmt2alloc.get(site.getStatement()));
				}
			}
			else if (obj instanceof UserFunctionValue) {
				UserFunctionValue uf = (UserFunctionValue) obj;
				result.add(new FunctionAllocSite(main.getAstBinding().getFunctionNode(uf.getFunction())));
			}
			else if (obj instanceof FunctionPrototypeValue) {
				FunctionPrototypeValue fp = (FunctionPrototypeValue) obj;
				if (fp.getFunction() instanceof UserFunctionValue) {
					UserFunctionValue uf = (UserFunctionValue) fp.getFunction();
					result.add(new FunctionProtoAllocSite(main.getAstBinding().getFunctionNode(uf.getFunction())));
				}
			}
			else if (obj instanceof ArgumentsArrayValue) {
				ArgumentsArrayValue av = (ArgumentsArrayValue) obj;
				result.add(new ArgumentsArrayAllocSite(main.getAstBinding().getFunctionNode(av.getFunction())));
			}
		}
		return result;
	}
	
	private Set<ObjectValue> getObjectsAllocatedAt(AllocSite site) {
		final Set<ObjectValue> result = new HashSet<ObjectValue>();
		return site.apply(new AllocSiteAnswerVisitor<Set<ObjectValue>>() {
			@Override
			public Set<ObjectValue> caseArguments(ArgumentsArrayAllocSite site) {
				Function function = main.getAstBinding().getFunction(site.getExp());
				for (Context ctx : main.getFunctionReachableContexts().getView(function)) {
					result.add(new ArgumentsArrayValue(function, ctx));
				}
				return result;
			}

			@Override
			public Set<ObjectValue> caseArrayLiteral(ArrayLiteralAllocSite site) {
				return main.objects(site.getExp());
			}

			@Override
			public Set<ObjectValue> caseFunction(FunctionAllocSite site) {
				for (CreateFunction stm : main.getAstBinding().getFunctions(site.getExp())) {
					for (InitializeFunctionNode flow : main.getDataflowBinding().getFunctionNodes(stm)) {
						result.addAll(main.lookupIns(flow.getResult(), ObjectValue.class));
					}
				}
				return result;
			}

			@Override
			public Set<ObjectValue> caseFunctionProto(FunctionProtoAllocSite site) {
				for (CreateFunction stm : main.getAstBinding().getFunctions(site.getExp())) {
					for (InitializeFunctionNode flow : main.getDataflowBinding().getFunctionNodes(stm)) {
						for (UserFunctionValue uf : main.lookupIns(flow.getResult(), UserFunctionValue.class)) {
							result.add(uf.getFunctionPrototype());
						}
					}
				}
				return result;
			}

			@Override
			public Set<ObjectValue> caseNewExp(NewExpAllocSite site) {
				for (InvokeStatement stm : main.getAstBinding().getInvokeStatements(site.getExp())) {
					for (IInvocationFlowNode flow : main.getDataflowBinding().getInvoke(stm)) {
						result.addAll(main.lookupIns(flow.getBase(), ObjectValue.class));
					}
				}
				return result;
			}

			@Override
			public Set<ObjectValue> caseObjLiteral(ObjLiteralAllocSite site) {
				return main.objects(site.getExp());
			}

			@Override
			public Set<ObjectValue> caseRegExp(RegExpAllocSite site) {
				return main.objects(site.getExp());
			}
		});
	}
	
	@Override
	public Set<AllocSite> getPrototypeOf(AllocSite site) {
		return getAllocSites(main.getDirectPrototypes(getObjectsAllocatedAt(site)));
	}
	
	@Override
	public MultiMap<String, AllocSite> getPointsTo(AllocSite site) {
		MultiMap<String,AllocSite> result = new MultiMap<String, AllocSite>();
		for (ObjectValue obj : getObjectsAllocatedAt(site)) {
			addProperties(result, obj);
		}
		return result;
	}

    private void addProperties(MultiMap<String, AllocSite> result, ObjectValue obj) {
        for (String prty : main.getKnownPropertyNames()) {
        	result.addAll(prty, getAllocSites(main.lookupSens(new NamedPropertyKey(obj, prty), ObjectValue.class)));
        	result.addAll(prty, getAllocSites(main.lookupSens(obj.getDynamicStoreProperty(), ObjectValue.class)));
        }
    }
    
    @Override
    public Set<AllocSite> getArgumentAllocationSites(IFunction function, int argIndex) {
        Function func = main.getAstBinding().getFunction(function);
        if (func.getParameterNames().size() < argIndex)
            throw new IllegalArgumentException("Can only query declared parameters");
        return getAllocSites(main.lookupIns(new VariableKey(func.getParameterNames().get(argIndex), func, NullContext.Instance), ObjectValue.class));
    }
    
    @Override
    public Set<AllocSite> getThisAllocationSites(IFunction function) {
        Function func = main.getAstBinding().getFunction(function);
        return getAllocSites(main.lookupIns(new VariableKey("this", func, NullContext.Instance), ObjectValue.class));
    }
}
