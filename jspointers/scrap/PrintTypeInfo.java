package dk.brics.jspointers.display;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.StatementVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.statements.AbstractStatementVisitor;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeclareVariable;
import dk.brics.jscontrolflow.statements.WriteDynamicProperty;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.IInvocation;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.FunctionInstanceKey;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.keys.VariableKey;
import dk.brics.jspointers.lattice.values.ApplyFunctionValue;
import dk.brics.jspointers.lattice.values.BooleanValue;
import dk.brics.jspointers.lattice.values.CallFunctionValue;
import dk.brics.jspointers.lattice.values.FunctionPrototypeValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.NumberValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jsutil.MultiMap;

public class PrintTypeInfo {
	public static void printTypeInfo(final DataflowGraph dataflow, AnalysisResult<Key,Set<Value>> result) {
		final MultiMap<Key,Value> map = DisplayUtil.makeContextInsensitive(result);
		
		final Set<Function> calledAsFunction = new HashSet<Function>();
		final Set<Function> calledAsConstructor = new HashSet<Function>();
		DisplayUtil util = new DisplayUtil(map, dataflow);
		for (FlowNode node : dataflow.getNodes()) {
			if (node instanceof IInvocation) {
				IInvocation invoke = (IInvocation) node;
				for (Value funcval : util.getFunctionArgs(invoke)) {
					if (funcval instanceof UserFunctionValue) {
						UserFunctionValue func = (UserFunctionValue) funcval;
						if (invoke.isConstructor()) {
							calledAsConstructor.add(func.getFunction());
						} else {
							calledAsFunction.add(func.getFunction());
						}
					}
				}
			}
		}
		for (Value value : map.getView(CallFunctionValue.Instance.getThisArg(NullContext.Instance))) {
			if (value instanceof UserFunctionValue) {
				UserFunctionValue func = (UserFunctionValue) value;
				calledAsFunction.add(func.getFunction());
			}
		}
		for (Value value : map.getView(ApplyFunctionValue.Instance.getThisArg(NullContext.Instance))) {
			if (value instanceof UserFunctionValue) {
				UserFunctionValue func = (UserFunctionValue) value;
				calledAsFunction.add(func.getFunction());
			}
		}
		
		for (Function toplevel : dataflow.getTopLevels()) {
		    
    		PrivateVariables privateVariables = new PrivateVariables(toplevel);
    		
    		for (final Function func : toplevel.getTransitiveInnerFunctions(true)) {
    			if (map.getView(new FunctionInstanceKey(func, NullContext.Instance)).isEmpty())
    				continue; // function is not used - everything is undefined
    			if (dataflow.getHarnessFunctions().contains(func))
    				continue;
    			final boolean isGlobalScope = func.getOuterFunction() == null;
    			//Set<String> privateVars = privateVariables.getPrivateVariables(func);
    			//final Liveness liveness = new Liveness(fgd, func, privateVars);
    			//final ReachingDefs defs = new ReachingDefs(fgd, func, liveness, privateVars);
    			final ReachingDefinitions defs = new ReachingDefinitions(func, privateVariables, func != toplevel);
    			for (Block block : func.getBlocks()) {
    				for (final Statement node : block.getStatements()) {
    					node.apply(new AbstractStatementVisitor() {
    						@Override
    						public void caseDeclareVariable(DeclareVariable n) {
    							Key key;
    							if (isGlobalScope) {
    								key = new NamedPropertyKey(GlobalObjectValue.Instance, n.getVarName());
    							} else {
    								key = new VariableKey(n.getVarName(), func, NullContext.Instance);
    							}
    							printInfo(n.getVarName(), map.getView(key));
    						}
//    						@Override
//    						public void visit(DeclareFunctionNode n, Void a) {
//    							if (n.getFunction().getName() != null) {
//    								printInfo(n.getFunction().getName(), n.getFunction());
//    							}
//    						}
    						@Override
    						public void caseWriteDynamicProperty(WriteDynamicProperty n) {
    							boolean isPrototypeAssign = false;
    							Set<VariableDefinition> sources = defs.getReachingDefinitions(n, n.getBaseVar());
    							for (VariableDefinition asn : sources) {
    							    if (asn instanceof StatementVariableDefinition) {
    							        StatementVariableDefinition tasn = (StatementVariableDefinition)asn;
        								if (tasn.getStatement() instanceof ReadProperty) {
        									ReadProperty read = (ReadProperty) tasn.getStatement();
        									if (read.getProperty().equals("prototype")) {
        										isPrototypeAssign = true;
        										break;
        									}
        								}
    							    }
    							}
    							if (!isPrototypeAssign)
    								return;
    							for (VariableDefinition asn : defs.getReachingDefinitions(n, n.getValueVar())) {
    							    if (!(asn instanceof StatementVariableDefinition))
    							        continue;
    							    StatementVariableDefinition tasn = (StatementVariableDefinition)asn;
    								Function func;
    								if (tasn.getStatement() instanceof CreateFunction) {
    									CreateFunction decl = (CreateFunction) tasn.getStatement();
    									func = decl.getFunction();
    								} else {
    									func = null;
    								}
    								if (func != null) {
    									printInfo(n.getProperty(), func);
    								}
    							}
    						}
    						
    						private void printInfo(String name, Function function) {
    							if (dataflow.getHarnessFunctions().contains(function))
    								return; // skip harness functions
    							String filename = function.getLocation().getFile().getName();
    							System.out.printf("%s:%d %s: %s\n", filename, function.getLocation().getLineNumber(), name, computeFunctionSignature(function));
    						}
    						private void printInfo(String name, Set<Value> values) {
//    							String filename = new File(node.getSourceLocation().getFileName()).getName();
//    							System.out.printf("%s:%d %s: %s\n", filename, node.getSourceLocation().getLineNumber(), name, getArgumentTypeString(values));
    						}
    						
    						private Set<Function> active = new HashSet<Function>();
    						
    						private String computeFunctionSignature(Function function) {
    							if (!active.add(function))
    								return "cyclic-function-type";
    							boolean func = calledAsFunction.contains(function);
    							boolean constructor = calledAsConstructor.contains(function);
    							if (!func && !constructor) {
    								return "unused function";
    							}
    							// format: <return type> function(<arg1>, <arg2>, ...)
    							StringBuilder b = new StringBuilder();
    							if (func) { // do not print return type for functions only used as constructors
    								b.append(getReturnTypeString(map.getView(dataflow.getNormalReturns().get(function).getValue().getKey(NullContext.Instance))));
    								b.append(" ");
    							}
    							if (func && constructor) {
    								b.append("function&constructor");
    							} else if (func) {
    								b.append("function");
    							} else {
    								b.append("constructor");
    							}
    							b.append("(");
    							for (int i=0; i<function.getParameterNames().size(); i++) {
    								String pname = function.getParameterNames().get(i);
    								if (i > 0)
    									b.append(", ");
    								b.append(getArgumentTypeString(map.getView(new VariableKey(pname, function, NullContext.Instance))));
    							}
    							b.append(")");
    							// print types of this argument
    							b.append(" [thisarg=");
    							b.append(getArgumentTypeString(map.getView(new VariableKey("this", function, NullContext.Instance))));
    							b.append("]");
    							active.remove(function);
    							return b.toString();
    						}
    						
    						private String set2string(Set<String> strings) {
    							if (strings.isEmpty())
    								return "undefined";
    							else if (strings.size() == 1)
    								return strings.iterator().next();
    							else {
    								StringBuilder b = new StringBuilder("<");
    								boolean first=true;
    								for (String s : strings) {
    									if (!first)
    										b.append("|");
    									else
    										first=false;
    									b.append(s);
    								}
    								b.append(">");
    								return b.toString();
    							}
    						}
    						private String getArgumentTypeString(Set<Value> values) {
    							Set<String> typenames = getTypeNames(values);
    							return set2string(typenames);
    						}
    						private String getReturnTypeString(Set<Value> values) {
    							Set<String> typenames = getTypeNames(values);
    							if (typenames.isEmpty())
    								return "void";
    							else
    								return set2string(typenames);
    						}
    						private Set<String> getTypeNames(Set<Value> values) {
    							Set<String> typenames = new TreeSet<String>();
    							for (Value value : values) {
    								if (value instanceof NumberValue)
    									typenames.add("number");
    								else if (value instanceof BooleanValue)
    									typenames.add("boolean");
    								else if (value instanceof StringValue)
    									typenames.add("string");
    //								else if (value instanceof NullValue)
    //									typenames.add("null");
    								else if (value instanceof UserFunctionValue) {
    									UserFunctionValue uf = (UserFunctionValue)value;
    									Function func = uf.getFunction();
    									typenames.add(computeFunctionSignature(func));
    								} else if (value instanceof NativeFunctionValue) {
    									typenames.add("function"); // TODO
    								} else if (value instanceof ObjectValue) {
    									typenames.add(getObjectClassName((ObjectValue)value));
    								}
    							}
    							return typenames;
    						}
    						
    						private String getObjectClassName(ObjectValue obj) {
    							if (obj instanceof FunctionValue) {
    								return "function";
    							}
    							Set<String> names = new TreeSet<String>();
    							for (Value protoval : map.getView(obj.getPrototypeProperty())) {
    								String name = null;
    								if (protoval instanceof FunctionPrototypeValue) {
    									FunctionPrototypeValue proto = (FunctionPrototypeValue) protoval;
    									if (proto.getFunction() instanceof UserFunctionValue) {
    										UserFunctionValue uf = (UserFunctionValue) proto.getFunction();
    										if (uf.getFunction().getName() != null) {
    											name = uf.getFunction().getName();
    										} else {
    											name = "object";
    										}
    									}
    								}
    								if (name != null)
    									names.add(name);
    							}
    							if (names.isEmpty())
    								return "object";
    							else if (names.size() == 1)
    								return names.iterator().next();
    							else
    								return names.toString();
    						}
    						
    					});
    				}
    			}
    		}
        }
	}
}
