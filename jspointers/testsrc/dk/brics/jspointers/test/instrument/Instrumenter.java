package dk.brics.jspointers.test.instrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.AnalysisAdapter;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jspointers.test.NodeFactory;
import dk.brics.jspointers.test.PrettyPrinter;
import dk.brics.jsutil.MultiMap;
import dk.brics.jsutil.Pair;

public class Instrumenter {
	private MultiMap<IFunction, Integer> func2callees = new MultiMap<IFunction, Integer>();
	private Map<AInvokeExp, Integer> invoke2callid = new HashMap<AInvokeExp, Integer>();
	private Map<AllocSite,Integer> allocsite2id = new HashMap<AllocSite, Integer>();
	private InstrumentData data;
	private ArrayList<AbstractObj> objectGraph = new ArrayList<AbstractObj>();
	
	public static class AbstractObj {
		MultiMap<String,Integer> pointsTo = new MultiMap<String, Integer>();
		Set<Integer> proto = new HashSet<Integer>();
	}
	
	public Instrumenter(InstrumentData data) {
		this.data = data;
	}
	
	public void instrument() {
		buildIDs();
		buildCallGraph();
		buildObjectGraph();
		doInstrumentation();
	}

	private void buildObjectGraph() {
		allAllocSites = new ArrayList<AllocSite>();
		for (Start start : data.getAst()) {
			start.apply(new DepthFirstAdapter() {
				@Override
				public void inANewExp(ANewExp node) {
					allAllocSites.add(new NewExpAllocSite(node));
				}
				@Override
				public void inAObjectLiteralExp(AObjectLiteralExp node) {
					allAllocSites.add(new ObjLiteralAllocSite(node));
				}
				@Override
				public void inAArrayLiteralExp(AArrayLiteralExp node) {
					allAllocSites.add(new ArrayLiteralAllocSite(node));
				}
				@Override
				public void inARegexpExp(ARegexpExp node) {
					allAllocSites.add(new RegExpAllocSite(node));
				}
				@Override
				public void inAFunctionDeclStmt(AFunctionDeclStmt node) {
					allAllocSites.add(new FunctionAllocSite(node));
					allAllocSites.add(new FunctionProtoAllocSite(node));
					allAllocSites.add(new ArgumentsArrayAllocSite(node));
				}
				@Override
				public void inAFunctionExp(AFunctionExp node) {
					allAllocSites.add(new FunctionAllocSite(node));
					allAllocSites.add(new FunctionProtoAllocSite(node));
					allAllocSites.add(new ArgumentsArrayAllocSite(node));
				}
			});
		}
		int nextAllocSiteId = 0;
		for (AllocSite site : allAllocSites) {
			allocsite2id.put(site, nextAllocSiteId++);
		}
		for (AllocSite site : allAllocSites) {
			final AbstractObj ab = new AbstractObj();
			MultiMap<String, AllocSite> pointsTo = data.getPointsTo(site);
			for (String prty : pointsTo.keySet()) {
				for (AllocSite dst : pointsTo.getView(prty)) {
					ab.pointsTo.add(prty, allocsite2id.get(dst));
				}
			}
			for (AllocSite proto : data.getPrototypeOf(site)) {
				assert allocsite2id.get(proto) != null;
				ab.proto.add(allocsite2id.get(proto));
			}
			
			// compensate for non-standard properties of V8
			site.apply(new AbstractAllocSiteVisitor() {
			    @Override
			    public void caseFunction(FunctionAllocSite site) {
			        ab.pointsTo.add("arguments", allocsite2id.get(new ArgumentsArrayAllocSite(site.getExp())));
			    }
			    @Override
			    public void caseArguments(ArgumentsArrayAllocSite site) {
			        ab.pointsTo.add("callee", -1);
			    }
			});
			
			objectGraph.add(ab);
		}
	}

	int nextCallId = 1;
	private List<AllocSite> allAllocSites;
	private void buildIDs() {
		for (Start start : data.getAst()) {
			if (data.isNative(start))
				continue;
			start.apply(new DepthFirstAdapter() {
				@Override
				public void inAInvokeExp(AInvokeExp node) {
					invoke2callid.put(node, nextCallId++);
				}
//				@Override
//				public void inANewExp(ANewExp node) {
//					exp2allocid.put(node, nextAllocId++);
//				}
//				@Override
//				public void inAObjectLiteralExp(AObjectLiteralExp node) {
//					exp2allocid.put(node, nextAllocId++);
//				}
//				@Override
//				public void inARegexpExp(ARegexpExp node) {
//					exp2allocid.put(node, nextAllocId++);
//				}
//				@Override
//				public void inAArrayLiteralExp(AArrayLiteralExp node) {
//					exp2allocid.put(node, nextAllocId++);
//				}
//				@Override
//				public void inAFunctionExp(AFunctionExp node) {
//					handleFunction(node);
//				}
//				@Override
//				public void inAFunctionDeclStmt(AFunctionDeclStmt node) {
//					handleFunction(node);
//				}
//				void handleFunction(IFunction node) {
//					func2allocid.put(node, nextAllocId++);
//					funcproto2allocid.put(node, nextAllocId++);
//				}
			});
		}
	}
	
	private void buildCallGraph() {
		for (Start start : data.getAst()) {
			if (data.isNative(start))
				continue;
			start.apply(new DepthFirstAdapter() {
				@Override
				public void inAInvokeExp(AInvokeExp invoke) {
					int id = invoke2callid.get(invoke);
					for (IFunction func : data.getTargets(invoke)) {
						if (data.isNative(func))
							continue;
						func2callees.add(func, id);
					}
				}
			});
		}
	}
	
	private int getAllocSiteId(AllocSite site) {
//		if (allocsite2id.containsKey(site)) {
//			return allocsite2id.get(site);
//		} else {
//			int id = nextAllocSiteId++;
//			allocsite2id.put(site, id);
//			return id;
//		}
		return allocsite2id.get(site);
	}
	
	
	private void doInstrumentation() {
		for (Start start : data.getAst()) {
			if (data.isNative(start))
				continue;
			AstUtil.fillTokens(start);
			start.apply(new DepthFirstAdapter() {
				@Override
				public void outAFunctionExp(AFunctionExp node) {
					//    function(..) {..}
					// => (function(..) {..}).$init(id, protoId, [callers, ...], info)
					AArrayLiteralExp callers = makeCallerArray(node);
					String name = node.getName() == null ? "<anon>" : node.getName().getText();
					int id = getAllocSiteId(new FunctionAllocSite(node));
					int protoId = getAllocSiteId(new FunctionProtoAllocSite(node));
					AInvokeExp exp = NodeFactory.createInvokeExp(
							NodeFactory.createPropertyExp(NodeFactory.createParenExp(AstUtil.clone(node)), "$init"), 
							Arrays.asList(
									NodeFactory.createLiteral(id),
									NodeFactory.createLiteral(protoId),
									callers,
									NodeFactory.createStringLiteral(name + ":" + node.getFunction().getLine())));
					AstUtil.replaceNode(node, exp);
				}
				@Override
				public void outAFunctionDeclStmt(AFunctionDeclStmt node) {
					//    {
					//		...
					//    	function Foo(..) {..}
					//		...
					//	  }
					// => {
					//		Foo.$init(id, protoId, [callers, ...], info);
					//		...
					//		function Foo(..) {..}
					//		...
					//	  }
					ABody body = node.getAncestor(ABody.class);
					AArrayLiteralExp callers = makeCallerArray(node);
					int id = getAllocSiteId(new FunctionAllocSite(node));
					int protoId = getAllocSiteId(new FunctionProtoAllocSite(node));
					AInvokeExp exp = NodeFactory.createInvokeExp(
							NodeFactory.createPropertyExp(NodeFactory.createNameExp(Literals.getName(node)), "$init"),
							Arrays.asList(
									NodeFactory.createLiteral(id),
									NodeFactory.createLiteral(protoId),
									callers, 
									NodeFactory.createStringLiteral(node.getName().getText() + ":" + node.getName().getLine())));
					PrettyPrinter.insertStmtIntoBlock(body.getBlock(), 0, NodeFactory.createExpStmt(exp));
				}
				
				private AArrayLiteralExp makeCallerArray(IFunction func) {
					List<PExp> args = new LinkedList<PExp>();
					for (int callId : func2callees.getView(func)) {
						args.add(NodeFactory.createLiteral(callId));
					}
					return NodeFactory.createArrayLiteralExp(args);
				}
				
				@Override
				public void outAInvokeExp(final AInvokeExp invoke) {
					final int id = invoke2callid.get(invoke);
					PExp rcv = unparen(invoke.getFunctionExp());
					rcv.apply(new AnalysisAdapter() {
						@Override
						public void caseAPropertyExp(APropertyExp node) {
							methodInvoke(AstUtil.clone(node.getBase()), NodeFactory.createStringLiteral(Literals.getName(node)));
						}
						@Override
						public void caseADynamicPropertyExp(ADynamicPropertyExp node) {
							methodInvoke(AstUtil.clone(node.getBase()), AstUtil.clone(node.getPropertyExp()));
						}
						@Override
						public void defaultPExp(PExp node) {
							functionInvoke(AstUtil.clone(node));
						}
						
						void methodInvoke(PExp base, PExp name) {
							//    base[name](arg1, arg2, ...)
							// => base.$invoke_method(name, <callID>, [arg1, arg2, ...])
							APropertyExp newFuncExp = NodeFactory.createPropertyExp(base, "$invoke_method");
							AConstExp callIdExp = NodeFactory.createLiteral(id);
							AArrayLiteralExp arrayExp = makeArgumentsArray(invoke);
							AstUtil.replaceNode(invoke, NodeFactory.createInvokeExp(newFuncExp, Arrays.asList(name, callIdExp, arrayExp)));
						}
						void functionInvoke(PExp exp) {
							//    foo(arg1, arg2, ...)
							// => $invoke_function(foo, <callID>, [arg1, arg2, ...])
							PExp newFuncExp = NodeFactory.createNameExp("$invoke_function");
							AConstExp callIdExp = NodeFactory.createLiteral(id);
							AArrayLiteralExp arrayExp = makeArgumentsArray(invoke);
							AstUtil.replaceNode(invoke, NodeFactory.createInvokeExp(newFuncExp, Arrays.asList(exp, callIdExp, arrayExp)));
						}
					});
				}

				private AArrayLiteralExp makeArgumentsArray(final IInvocationNode invoke) {
					List<PExp> arrayArgs = new ArrayList<PExp>();
					for (PExp arg : invoke.getArguments()) {
						arrayArgs.add(AstUtil.clone(arg));
					}
					AArrayLiteralExp arrayExp = NodeFactory.createArrayLiteralExp(arrayArgs);
					return arrayExp;
				}
				
				private PExp unparen(PExp exp) {
					while (exp instanceof AParenthesisExp) {
						exp = ((AParenthesisExp)exp).getExp();
					}
					return exp;
				}
				
				@Override
				public void outANewExp(ANewExp node) {
					//    new Foo(arg1, ...)
					// => $construct(Foo, id, [arg1, ...])
					int id = getAllocSiteId(new NewExpAllocSite(node));
					AArrayLiteralExp arrayExp = makeArgumentsArray(node);
					AInvokeExp invoke = NodeFactory.createInvokeExp(
							NodeFactory.createNameExp("$construct"), 
							Arrays.asList(
									AstUtil.clone(node.getFunctionExp()),
									NodeFactory.createLiteral(id),
									arrayExp));
					AstUtil.replaceNode(node, invoke);
				}

				private void simpleAllocator(PExp node, int id) {
					AInvokeExp invoke = NodeFactory.createInvokeExp(
							NodeFactory.createPropertyExp(AstUtil.clone(node), "$init"), 
							Arrays.asList(NodeFactory.createLiteral(id)));
					AstUtil.replaceNode(node, invoke);
				}
				@Override
				public void outAObjectLiteralExp(AObjectLiteralExp node) {
					//    { prty1: val1, ... }
					// => { prty1: val1, ... }.$init(id)
					int id = getAllocSiteId(new ObjLiteralAllocSite(node));
					simpleAllocator(node, id);
				}
				
				public void outAArrayLiteralExp(AArrayLiteralExp node) {
					//    [ x1, ... ]
					//    [ x1, ... ].$init(id)
					int id = getAllocSiteId(new ArrayLiteralAllocSite(node));
					simpleAllocator(node, id);
				}
				
				@Override
				public void outARegexpExp(ARegexpExp node) {
					//    /foo.../
					// => /foo.../.$init(id)
					int id = getAllocSiteId(new RegExpAllocSite(node));
					simpleAllocator(node, id);
				}
				
				@Override
				public void outABody(ABody node) {
					IFunction function = node.getAncestor(IFunction.class);
					if (function == null) {
						addGraphDefinition(node);
					} else {
						// insert: arguments.$_obj_id = id
						int id = getAllocSiteId(new ArgumentsArrayAllocSite(function));
						PExp exp = NodeFactory.createAssignExp(
								NodeFactory.createPropertyExp(NodeFactory.createNameExp("arguments"), "$_obj_id"),
								NodeFactory.createLiteral(id));
						PrettyPrinter.insertStmtIntoBlock(node.getBlock(), 0, NodeFactory.createExpStmt(exp));
						
						int maxDepth = 2;
						// insert: $check_object_graph([this,arg1,...],[[id1,..],[id,..]...],"line X",["this","arg1",...],maxDepth)
						List<PExp> objs = new LinkedList<PExp>();
						List<PExp> idss = new LinkedList<PExp>();
						List<PExp> descriptions = new LinkedList<PExp>();
						
						// first add 'this'
						objs.add(NodeFactory.createThisExp());
						idss.add(makeObjIdArray(data.getThisAllocationSites(function)));
						descriptions.add(NodeFactory.createStringLiteral("this"));
						
						// add parameters
						for (int i=0; i<function.getParameters().size(); i++) {
						    String name = Literals.parseIdentifier(function.getParameters().get(i).getText());
						    objs.add(NodeFactory.createNameExp(name));
						    idss.add(makeObjIdArray(data.getArgumentAllocationSites(function, i)));
						    descriptions.add(NodeFactory.createStringLiteral(name));
						}
						
						String infoStr = "line " + function.getFunction().getLine();
						
						AInvokeExp invoke = NodeFactory.createInvokeExp(
						        NodeFactory.createNameExp("$check_object_graph"), 
						        Arrays.asList(
						                NodeFactory.createArrayLiteralExp(objs),
                                        NodeFactory.createArrayLiteralExp(idss),
                                        NodeFactory.createStringLiteral(infoStr),
                                        NodeFactory.createArrayLiteralExp(descriptions),
                                        NodeFactory.createLiteral(maxDepth)
						        ));
						PrettyPrinter.insertStmtIntoBlock(node.getBlock(), 1, NodeFactory.createExpStmt(invoke));
					}
				}
				
				private AArrayLiteralExp makeObjIdArray(Collection<? extends AllocSite> sites) {
				    List<Integer> num = new LinkedList<Integer>();
				    for (AllocSite site : sites) {
				        num.add(allocsite2id.get(site));
				    }
				    return numberArray(num);
				}
				
//				@Override
//				public void caseAExpStmt(AExpStmt node) {
//					// compute source string before instrumenting children
//					String infoStr = "line " + AstUtil.getFirstAndLastToken(node).first.getLine();
//					String expStr;
//					if (node.getExp() instanceof AAssignExp) {
//					    AAssignExp as = (AAssignExp) node.getExp();
//					    expStr = AstUtil.toSourceString(as.getLeft());
//					} else {
//					    expStr = AstUtil.toSourceString(node.getExp());
//					}
//                    Set<AllocSite> resultAllocationSites = data.getResultAllocationSites(node.getExp());
//					
//					node.getExp().apply(this); // recurse
//					
//					// exp
//					// $check_object_graph(exp, [id1, ...], "linenr", "...")
//					List<PExp> ids = new LinkedList<PExp>();
//                    for (AllocSite site : resultAllocationSites) {
//						ids.add(NodeFactory.createLiteral(getAllocSiteId(site)));
//					}
//					if (expStr.length() > 55) {
//						expStr = expStr.substring(0, 52) + "...";
//					}
//					AInvokeExp invoke = NodeFactory.createInvokeExp(
//							NodeFactory.createNameExp("$check_object_graph"), 
//							Arrays.<PExp>asList(
//									AstUtil.clone(node.getExp()),
//									NodeFactory.createArrayLiteralExp(ids),
//									NodeFactory.createStringLiteral(infoStr),
//									NodeFactory.createStringLiteral(expStr)));
//					AstUtil.replaceNode(node.getExp(), invoke);
//				}
				
				private AArrayLiteralExp numberArray(Collection<Integer> nums) {
					List<PExp> exps= new LinkedList<PExp>();
					for (int num : nums) {
						exps.add(NodeFactory.createLiteral(num));
					}
					return NodeFactory.createArrayLiteralExp(exps);
				}
				
				private void addGraphDefinition(ABody node) {
					// $graph = [
					//   {"$_proto":[proto1,...], "prty1":[prty1,...], ...},
					//   ....
					//   ];
					List<PExp> abstractObjs = new LinkedList<PExp>();
					for (AbstractObj ab : objectGraph) {
						List<Pair<String,PExp>> prtys = new LinkedList<Pair<String,PExp>>();
						prtys.add(new Pair<String,PExp>("$_proto", numberArray(ab.proto)));
						for (String prty : ab.pointsTo.keySet()) {
							prtys.add(new Pair<String,PExp>(prty, numberArray(ab.pointsTo.getView(prty))));
						}
						abstractObjs.add(NodeFactory.createObjectLiteral(prtys));
					}
					AArrayLiteralExp array = NodeFactory.createArrayLiteralExp(abstractObjs);
					PExp assign = NodeFactory.createAssignExp(
							NodeFactory.createNameExp("$graph"), 
							array);
					PrettyPrinter.insertStmtIntoBlock(node.getBlock(), 0, NodeFactory.createExpStmt(assign));
				}
			});
		}
	}

}
