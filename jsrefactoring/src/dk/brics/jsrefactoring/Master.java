package dk.brics.jsrefactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowResult;
import dk.brics.jscontrolflow.analysis.flowsolver.ForwardFlowSolver;
import dk.brics.jscontrolflow.analysis.reachdef.ArgumentsArrayVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ParameterVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.SelfVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.StatementVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.UninitializedVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinitionVisitor;
import dk.brics.jscontrolflow.ast2cfg.AstBinding;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.statements.AbstractStatementVisitor;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.CallProperty;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.IPropertyAccessStatement;
import dk.brics.jscontrolflow.statements.IVariableAccessStatement;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteStatement;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.AInBinop;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AStringConst;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AWithStmt;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.PConst;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jspointers.JSPointerAnalysis;
import dk.brics.jspointers.cfg2dataflow.Controlflow2DataflowBinding;
import dk.brics.jspointers.dataflow.AbstractFlowNodeVisitor;
import dk.brics.jspointers.dataflow.CoerceToPrimitive;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.IDynamicPropertyAccessFlowNode;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.IPropertyAccessFlowNode;
import dk.brics.jspointers.dataflow.IStoreFlowNode;
import dk.brics.jspointers.dataflow.IVariableAccessFlowNode;
import dk.brics.jspointers.dataflow.IVariableWriteFlowNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadDynamicNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.dataflow.StoreDynamicNode;
import dk.brics.jspointers.dataflow.StoreIfPresentNode;
import dk.brics.jspointers.dataflow.StoreNode;
import dk.brics.jspointers.dataflow.StubNode;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.FunctionInstanceKey;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.keys.NativeArgKey;
import dk.brics.jspointers.lattice.keys.VariableKey;
import dk.brics.jspointers.lattice.values.AllocObjectValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.DOMNative;
import dk.brics.jspointers.lattice.values.natives.EvalNative;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jspointers.lattice.values.natives.FunctionNative;
import dk.brics.jspointers.parametric.StatementAllocSite;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.movetoprototype.DataDependencyAnalysis;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsutil.CollectionUtil;
import dk.brics.jsutil.MultiMap;
import dk.brics.jsutil.Pair;

/**
 * Contains ASTs, controlflow graphs, and the dataflow graph for a set of JavaScript files.
 * <p/>
 * This class can answer queries at the AST level, abstracting away the details of the intermediate
 * representations.
 * 
 * @author asf
 * @author max.schaefer@comlab.ox.ac.uk
 */
public class Master {
    private List<InputFile> userFiles;
    private List<InputFile> harnessFiles;
    private List<InputFile> allFiles;
    private JSPointerAnalysis pointers;
    private Map<Start,InputFile> ast2inputFile = new HashMap<Start,InputFile>();
    private MultiMap<Key, Value> flatkey2value = new MultiMap<Key, Value>();
    private MultiMap<Function, Context> reachableContexts = new MultiMap<Function, Context>();
    private Controlflow2DataflowBinding binding = new Controlflow2DataflowBinding();
    private AstBinding astBinding = new AstBinding();

    public Master(File... files) {
    	init(LoadFileUtil.loadInputFiles(astBinding, files));
    }
    
    public Master(Start[] scripts, File[] files) {
    	init(LoadFileUtil.loadInputFiles(astBinding, scripts, files));
    }
    
    private void init(List<InputFile> files) {
        userFiles = files;
        harnessFiles = LoadFileUtil.loadHarnessFiles(astBinding);
        allFiles = new ArrayList<InputFile>();
        allFiles.addAll(userFiles);
        allFiles.addAll(harnessFiles);
        
        for (final InputFile f : allFiles) {
        	f.getAst().apply(new DepthFirstAdapter() {
        		@Override
        		public void defaultIn(Node node) {
        			if (node.getRoot() != f.getAst()) {
        				throw new RuntimeException("Invalid root in " + node);
        			}
        		}
        	});
        }

        for (InputFile file : allFiles) {
            ast2inputFile.put(file.getAst(), file);
        }

        pointers = new JSPointerAnalysis(
                LoadFileUtil.getTopLevelFunctions(userFiles),
                LoadFileUtil.getTopLevelFunctions(harnessFiles),
                binding);

        for (Map.Entry<Key,Set<Value>> en : pointers.getResult().entrySet()) {
            flatkey2value.addAll(en.getKey().makeContextInsensitive(), en.getValue());
            if (en.getKey() instanceof FunctionInstanceKey) {
                FunctionInstanceKey inst = (FunctionInstanceKey)en.getKey();
                reachableContexts.add(inst.getFunction(), inst.getContext());
            }
        }
    }

    public List<InputFile> getUserFiles() {
        return userFiles;
    }
    
    public List<InputFile> getAllInputFiles() {
    	return allFiles;
    }
    
    public Set<Value> lookupContextSensitive(Key key) {
        return pointers.getResultAt(key);
    }
    public <T> Set<T> lookupContextSensitive(Key key, Class<T> clazz) {
        return pointers.getResultAt(key, clazz);
    }
    
    public Set<Value> lookupContextInsensitive(Key key) {
    	return flatkey2value.getView(key);
    }
    public <T> Set<T> lookupContextInsensitive(Key key, Class<T> clazz) {
    	return CollectionUtil.filter(flatkey2value.getView(key), clazz);
    }
    
    // TODO: don't expose this (currently needed in ClosureFlowAnalysis)
    public AstBinding getAstBinding() {
    	return astBinding;
    }
    
    public <T> Set<T> lookup(InputPoint ip, Class<T> type) {
    	Set<T> result = new HashSet<T>();
    	for (OutputPoint op : ip.getSources()) {
    		for (Value v : flatkey2value.getView(op.getKey(NullContext.Instance))) {
    			if (type.isInstance(v)) {
    				result.add(type.cast(v));
    			}
    		}
    	}
        return result;
    }
    private <T> Set<T> lookup(InputPoint ip, Context context, Class<T> type) {
    	assert context != NullContext.Instance : "Don't use NullContext with this method";
    	Set<T> result = new HashSet<T>();
    	for (OutputPoint op : ip.getSources()) {
    		for (Value v : pointers.getResultAt(op.getKey(context))) {
    			if (type.isInstance(v)) {
    				result.add(type.cast(v));
    			}
    		}
    	}
        return result;
    }
    
    private <T> Set<T> lookup(OutputPoint op, Class<T> type) {
    	return CollectionUtil.filter(flatkey2value.getView(op.getKey(NullContext.Instance)), type);
    }
    private <T> Set<T> lookup(OutputPoint op, Context context, Class<T> type) {
    	return pointers.getResultAt(op.getKey(context), type);
    }
    
    /**
     * Returns true if no object with the given label ever has an own property with the given name,
     * or false if it might be present on some object with that label.
     * <p/>
     * Does not consider the prototype chain.
     * @param obj an object label
     * @param propertyName a property name
     * @return true if definite, false if maybe
     */
    public boolean isPropertyDefinitelyAbsent(ObjectValue obj, String propertyName) {
    	return lookupContextSensitive(new NamedPropertyKey(obj, propertyName)).isEmpty()
			&& lookupContextSensitive(obj.getDynamicStoreProperty()).isEmpty();
    }
     
    /**
     * Returns true if the specified node is part of the harness files.
     * The harness files model some of the native functions in JavaScript.
     * @param node an AST node
     * @return boolean
     */
    public boolean isNativeCode(NodeInterface node) {
    	return harnessFiles.contains(ast2inputFile.get(((Node)node).getRoot()));
    }
    
    /**
     * Returns true if the specified function is defined in the harness files.
     * @param function a function value
     * @return boolean
     */
    public boolean isNativeCode(UserFunctionValue function) {
        return isNativeCode(astBinding.getFunctionNode(function.getFunction()));
    }
    
    /**
     * Returns the function AST node which instantiated the given function object.
     * @param function a function value
     * @return an AST node
     */
    public IFunction getFunctionNode(UserFunctionValue function) {
        return astBinding.getFunctionNode(function.getFunction());
    }
    
    /**
     * Returns the set of object names that may be given as right-hand argument to the
     * given IN expression.
     * @param exp a binop expression with {@link AInBinop} as its operator
     * @return unmodifiable set
     */
    public Set<ObjectValue> getInExpObjectArgs(ABinopExp exp) {
    	if (exp.getOp().kindPBinop() != EBinop.IN)
    		throw new IllegalArgumentException("Not an IN expression: " + exp);
    	Set<ObjectValue> result = new HashSet<ObjectValue>();
    	for (BinaryOperation stm : astBinding.getBinaryOperations(exp)) {
    		for (StubNode node : binding.getInExpObjectArg(stm)) {
    			result.addAll(lookup(node.getInput(), ObjectValue.class));
    		}
    	}
    	return result;
    }
    
    /**
     * Returns the set of object names that may be allocated by the object literal
     * containing the specified property initializee.
     * @param property a property initializer
     * @return unmodifiable set
     */
    public Set<ObjectValue> getInitializedObjects(ANormalObjectLiteralProperty property) {
        Set<ObjectValue> result = new HashSet<ObjectValue>();
        for (WriteProperty write : astBinding.getObjectLiteralProperty(property)) {
            for (IStoreFlowNode store : binding.getWriteProperty(write)) {
                result.addAll(lookup(store.getBase(), ObjectValue.class));
            }
        }
        return result;
    }
    
    /**
     * Given an expression <i>base[prty]</i>, returns true if <i>prty</i> definitely
     * evaluates to a number at runtime.
     * <p/>
     * Note: The receiver does not have to be an <tt>Array</tt> for this to be true.
     * @param exp an expression
     * @return true if definite, false if maybe not
     */
    public boolean isDefinitelyArrayLookup(ADynamicPropertyExp exp) {
        for (IPropertyAccessStatement stmt : astBinding.getPropertyAccesses(exp))
            for (IPropertyAccessFlowNode node : binding.getPropertyAccess(stmt))
                if(!isDefinitelyArrayLookup(node))
                	return false;
        return true;
    }

	public boolean isDefinitelyArrayLookup(IPropertyAccessFlowNode node) {
		if (node instanceof IDynamicPropertyAccessFlowNode) {
		    IDynamicPropertyAccessFlowNode dyn = (IDynamicPropertyAccessFlowNode)node;
		    for (Value val : lookup(dyn.getProperty(), Value.class))
		        if (val instanceof StringValue || val instanceof ObjectValue)
		            return false;
		    return true;
		} else {
		    // property name is a constant, cannot be an array lookup
		    return false;
		}
	}
    
    /**
     * Given a property access (either simple or dynamic), determines whether that access could
     * refer to a given name.
     */
    public boolean mayHaveName(IPropertyAccessNode exp, String name) {
    	if(exp instanceof ADynamicPropertyExp)
    		return !isDefinitelyArrayLookup((ADynamicPropertyExp)exp);
    	return ((APropertyExp)exp).getName().getText().equals(name);
    }
    
    public boolean mayHaveName(ABinopExp inexp, String name) {
    	assert inexp.getOp().kindPBinop() == EBinop.IN;
    	PExp left = inexp.getLeft();
    	if(left instanceof AConstExp) {
    		PConst konst = ((AConstExp)left).getConst();
    		if(konst instanceof AStringConst) {
        		String name2 = Literals.parseStringLiteral(((AStringConst)konst).getStringLiteral().getText());
    			return name.equals(name2);
    		}
    		// non-string literal
    		return false;
    	}
    	return true;
    }

    /**
     * Returns the set of object labels that may be the receiver of the given property access expression.
     * @param exp a property access
     * @return a newly created set
     */
    public Set<ObjectValue> getAccessedObjects(IPropertyAccessNode exp) {
        Set<ObjectValue> result = new HashSet<ObjectValue>();
        for (IPropertyAccessStatement stmt : astBinding.getPropertyAccesses(exp)) {
            for (IPropertyAccessFlowNode node : binding.getPropertyAccess(stmt)) {
                result.addAll(lookup(node.getBase(), ObjectValue.class));
            }
        }
        return result;
    }
    
    public Set<ObjectValue> getReceivers(IPropertyAccessNode exp) {
        Set<ObjectValue> receivers = getAccessedObjects(exp);
        if (AstUtil.isRValue((PExp)exp))
        	receivers = getAllPrototypes(receivers, true);
    	return receivers;
    }

    /**
     * Returns the set of object labels that may be resolved to by the given name
     * expression as a result of <tt>with</tt> scopes.
     * <p/>
     * Always returns the empty set if the expression does not occur in a <tt>with</tt>
     * scope.
     * @param exp a name expression
     * @return unmodifiable set
     */
    public Set<ObjectValue> getWithScopeReceivers(ANameExp exp) {
        Set<ObjectValue> result = new HashSet<ObjectValue>();
        for (IVariableAccessStatement stm : astBinding.getVariableAccesses(exp)) {
            for (IPropertyAccessFlowNode node : binding.getWithScopeAccess(stm)) {
                result.addAll(lookup(node.getBase(), ObjectValue.class));
            }
        }
        return result;
    }
    
    public Set<Scope> getDeclaringScopes(ANameExp exp) {
		return getDeclaringScopes(exp, Literals.getName(exp));
    }
    
    public Set<Scope> getDeclaringScopes(ANameExp exp, String name) {
        Set<Scope> result = new HashSet<Scope>();
        for (IVariableAccessStatement stm : astBinding.getVariableAccesses(exp))
			result.add(stm.getScope().getDeclaringScope(name));
        return result;
    }
    
    /**
     * Returns the set of parameters and local variables declared in the given function;
     * see {@link Scope#getDeclaredVariables()}.
     * 
     * @param fun a function declaration or expression
     */
    public Set<String> getDeclaredVariables(IFunction fun) {
        return getDeclaredVariables(fun.getBody());
    }
    
    public Set<String> getDeclaredVariables(IScopeBlockNode node) {
        return astBinding.getScope(node).getDeclaredVariables();
    }
    
    /**
     * Returns the set of {@link Function} objects this function node may correspond to at runtime.
     */
    public Set<Function> getFunctions(IFunction fun) {
    	Set<Function> result = new HashSet<Function>();
    	for(CreateFunction cf : astBinding.getFunctions(fun))
    		result.add(cf.getFunction());
    	return result;
    }
    
    /**
     * Returns the set of objects that the given function may be invoked on, i.e.
     * the possible values of "this" inside the function body.
     * 
     * @param fun a function declaration or expression
     */
    public Set<ObjectValue> getReceivers(IFunction fun) {
    	Set<ObjectValue> result = new HashSet<ObjectValue>();
    	for(CreateFunction cf : astBinding.getFunctions(fun))
    		result.addAll(getReceivers(cf.getFunction()));
    	return result;
	}

	public Set<ObjectValue> getReceivers(Function fun) {
		VariableKey key = new VariableKey("this", fun, NullContext.Instance);
		return CollectionUtil.filter(flatkey2value.getView(key), ObjectValue.class);
	}
    
    /**
     * Returns the set of objects that may be stored in the given property of the given object.
     */
    public Set<Value> getPropertyValue(ObjectValue obj, String name) {
    	return lookupContextSensitive(new NamedPropertyKey(obj, name));
    }

    /**
     * Returns the set of objects that may be the prototype of the given object.
     * @param obj an object label
     * @return unmodifiable set
     */
    public Set<ObjectValue> getDirectPrototypes(ObjectValue obj) {
        return pointers.getResultAt(obj.getPrototypeProperty(), ObjectValue.class);
    }

    /**
     * Returns the set of objects thay may be reachable on the prototype chain of
     * any of the given objects.
     * @param initialObjects set of object labels
     * @param includeSelf if true, the given object will included itself
     * @return newly created set
     */
    public Set<ObjectValue> getAllPrototypes(Set<? extends ObjectValue> initialObjects, boolean includeSelf) {
        Set<ObjectValue> prototypes = new HashSet<ObjectValue>();
        LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>();
        queue.addAll(initialObjects);
        if (includeSelf) {
            prototypes.addAll(initialObjects);
        }
        while (!queue.isEmpty()) {
            ObjectValue obj = queue.removeFirst();
            for (ObjectValue proto : getDirectPrototypes(obj)) {
                if (prototypes.add(proto)) {
                    queue.add(proto);
                }
            }
        }
        return prototypes;
    }

    /**
     * Returns the set of objects thay may be reachable on the given object's prototype chain.
     * @param initialObject an object label
     * @param includeSelf if true, the given object will included itself
     * @return newly created set
     */
    public Set<ObjectValue> getAllPrototypes(ObjectValue initialObject, boolean includeSelf) {
        return getAllPrototypes(Collections.singleton(initialObject), includeSelf);
    }
    
	/**
	 * <p>
	 * Convenience method for invoking {@link NodeFinder#getAllNodesOfType(Class)}.
	 * This effectively negates the caching performed by that method, so use this
	 * method only where performance is not an issue.
	 * </p>
	 */
	public <T> Set<T> getAllNodesOfType(Class<T> type) {
		return new NodeFinder(this, type).getAllNodesOfType(type);
	}
	
	/**
	 * Returns the set of function instances that may be allocated by the given AST node.
	 * @param function a function AST node
	 * @return unmodifiable set
	 */
	public Set<FunctionValue> getInitializedFunctionInstances(IFunction function) {
		Set<FunctionValue> result = new HashSet<FunctionValue>();
		for (CreateFunction create : astBinding.getFunctions(function)) {
			for (InitializeFunctionNode node : binding.getFunctionNodes(create)) {
				result.addAll(lookup(node.getResult(), FunctionValue.class));
			}
		}
		return result;
	}
	
	/**
	 * Returns the objects that may be created by a constructor call to some instance of
	 * the given function.
	 * @param function a function AST node
	 * @return unmodifiable set
	 */
	public Set<ObjectValue> getConstructedBy(IFunction function) {
		return getConstructedBy(getInitializedFunctionInstances(function));
	}
	
	/**
	 * Returns the objects that may have been created by a constructor call to a function
	 * in the given set.
	 * @param functions set of function instances
	 * @return unmodifiable set
	 */
	public Set<ObjectValue> getConstructedBy(Set<? extends Value> functions) {
		Set<ObjectValue> result = new HashSet<ObjectValue>();
		for (ANewExp exp : getAllNodesOfType(ANewExp.class)) {
			for (InvokeStatement invoke : astBinding.getInvokeStatements(exp))  {
				for (IInvocationFlowNode node : binding.getInvoke(invoke)) {
					InvokeNode inode = (InvokeNode)node;
					for (Context ctx : reachableContexts.getView(invoke.getBlock().getFunction())) {
						if (CollectionUtil.intersects(lookup(inode.getFunc(), ctx, ObjectValue.class), functions)) {
							result.addAll(lookup(inode.getBase(), ctx, ObjectValue.class));
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the line number of the given node, as it appears in its source or HTML file.
	 * If it appears in an HTML file, the number is translated appropriately.
	 * @param node an AST node
	 * @return absolute line number in the source or HTML file
	 */
	public int getTranslatedLineNumber(NodeInterface node) {
	    InputFile file = ast2inputFile.get(((Node)node).getRoot());
	    TokenPair tokens = AstUtil.getFirstAndLastToken((Node)node);
	    return tokens.first.getLine() + file.getStartLineNumber();
	}
	
	/**
	 * Returns the file from which the given AST node originated.
	 * @param node an AST node
	 * @return a JavaScript or HTML file
	 */
	public File getSourceFile(NodeInterface node) {
        InputFile file = ast2inputFile.get(((Node)node).getRoot());
        return file.getFile();
	}
	
	/**
	 * Returns the set of functions that may be invoked by the given invocation.
	 * @param invoke an invocation node
	 * @return unmodifiable set
	 */
	public Set<FunctionValue> getCalledFunctions(IInvocationNode invoke) {
	    Set<FunctionValue> result = new HashSet<FunctionValue>();
	    for (InvokeStatement stm : astBinding.getInvokeStatements(invoke)) {
	        for (IInvocationFlowNode node : binding.getInvoke(stm)) {
	            result.addAll(CollectionUtil.filter(getFunctionArgs(node), FunctionValue.class));
	        }
	    }
	    return result;
	}
	
	/**
	 * Returns the set of objects whose properties may be iterated through by the given for-in statement.
	 * Does not include the prototype chain (but for-in loops <i>do</i> use the prototype chain, 
	 * so consider using {@link #getAllPrototypes(ObjectValue, boolean) getAllPrototypes})
	 * @param stmt a for-in AST node
	 * @return unmodifiable set
	 */
	public Set<ObjectValue> getForInObjects(AForInStmt stmt) {
	    Set<ObjectValue> result = new HashSet<ObjectValue>();
	    for (GetNextProperty stm : astBinding.getGetNextProperty(stmt)) {
	        for (LoadDynamicNode node : binding.getGetNextPropertyNodes(stm)) {
	            result.addAll(lookup(node.getBase(), ObjectValue.class));
	        }
	    }
	    return result;
	}
    
    /**
     * Returns a pair of
     * <ol>
     * <li>A set of for-in statements that may define the property name used by the given property access.
     * <li>True if the above set contains <i>all</i> the possible definitions of the property name used by the given
     *      property access.
     * </ol>
     * @param stmt a property access statement
     * @return unmodifiable set and a boolean; not null
     */
    public Pair<Set<AForInStmt>,Boolean> getForInStatementsThatMayDefinePropertyName(ADynamicPropertyExp exp) {
        // XXX: The method name is kind of long. Can we find a shorter name for it?
        Set<AForInStmt> forins = new HashSet<AForInStmt>();
        boolean onlyForIn = true;
        for (IPropertyAccessStatement stmt : astBinding.getPropertyAccesses(exp)) {
            Function func = stmt.getBlock().getFunction();
            ReachingDefinitions reachingDefs = binding.getReachingDefinitions(func);
            for (VariableDefinition def : reachingDefs.getReachingDefinitions((Statement)stmt, stmt.getPropertyVar())) {
                if (!(def instanceof StatementVariableDefinition)) {
                    onlyForIn = false;
                    continue;
                }
                StatementVariableDefinition sdef = (StatementVariableDefinition)def;
                if (!(sdef.getStatement() instanceof GetNextProperty)) {
                    onlyForIn = false;
                    continue;
                }
                GetNextProperty getprty = (GetNextProperty)sdef.getStatement();
                forins.add(astBinding.getForInStmt(getprty));
            }
        }
        return Pair.make(forins, onlyForIn);
    }
	
	/**
	 * Returns a pair of
	 * <ol>
	 * <li>A set of dynamic property expressions whose property name may originate from the
	 *     for-in loop's iterated property name.
	 * <li>True if the above set contains <i>all</i> the possible uses of the property names
	 *     iterated by the given for-in statement.
	 * </ol>
	 * @param stmt a for-in statement
	 * @return unmodifiable set and a boolean; not null
	 */
	public Pair<Set<ADynamicPropertyExp>,Boolean> getForInNameUses(AForInStmt stmt) {
	    final boolean[] onlyPropertyAccess = new boolean[] {true};
	    final Set<ADynamicPropertyExp> exps = new HashSet<ADynamicPropertyExp>();
	    for (final GetNextProperty getprty : astBinding.getGetNextProperty(stmt)) {
	        Function func = getprty.getBlock().getFunction();
	        final ReachingDefinitions reachingDefs = binding.getReachingDefinitions(func);
	        final VariableDefinition def = new StatementVariableDefinition(getprty);
	        for (Block block : func.getBlocks()) {
    	        for (final Statement stm : block.getStatements()) {
    	            stm.apply(
    	                    new AbstractStatementVisitor() {
    	                private boolean reachVar(int var) {
    	                    return reachingDefs.getReachingDefinitions(stm, var).contains(def);
    	                }
    	                private void addAccess(IPropertyAccessStatement stm) {
    	                    exps.add((ADynamicPropertyExp)astBinding.getPropertyAccessNode(stm));
    	                }
    	                @Override
    	                public void caseReadProperty(ReadProperty stm) {
    	                    if (reachVar(stm.getPropertyVar())) {
    	                        addAccess(stm);
    	                    }
    	                    if (reachVar(stm.getBaseVar())) {
    	                        onlyPropertyAccess[0] = false;
    	                    }
    	                }
    	                @Override
    	                public void caseWriteProperty(WriteProperty stm) {
    	                    if (reachVar(stm.getPropertyVar())) {
    	                        addAccess(stm);
    	                    }
    	                    if (reachVar(stm.getBaseVar()) || reachVar(stm.getValueVar())) {
    	                        onlyPropertyAccess[0] = false;
    	                    }
    	                }
    	                @Override
    	                public void caseCallProperty(CallProperty stm) {
    	                    if (reachVar(stm.getPropertyVar())) {
    	                        addAccess(stm);
    	                    }
    	                    if (reachVar(stm.getBaseVar())) {
    	                        onlyPropertyAccess[0] = false;
    	                    }
    	                    for (int arg : stm.getArguments()) {
    	                        if (reachVar(arg)) {
    	                            onlyPropertyAccess[0] = false;
    	                        }
    	                    }
    	                }
    	                @Override
    	                public void caseDeleteProperty(DeleteProperty stm) {
    	                    if (reachVar(stm.getPropertyVar())) {
    	                        addAccess(stm);
    	                    }
    	                    if (reachVar(stm.getBaseVar())) {
    	                        onlyPropertyAccess[0] = false;
    	                    }
    	                }
    	                @Override
    	                public void caseWriteVariable(WriteVariable stm) {
    	                    if (reachVar(stm.getValueVar()) && !reachingDefs.isTransparentVariableAssignment(stm)) {
    	                        onlyPropertyAccess[0] = false;
    	                    }
    	                }
    	                @Override
    	                public void casePhi(Phi stm) {
    	                    // don't call defaultCase for phi nodes
    	                }
    	                @Override
    	                public void defaultCase(Statement stm) {
    	                    for (int var : stm.getReadVariables()) {
    	                        if (reachVar(var)) {
    	                            onlyPropertyAccess[0] = false;
    	                        }
    	                    }
    	                }
    	            });
    	        }
	        }
	    }
	    return Pair.make(exps, onlyPropertyAccess[0]);
	}
	
	private Set<Value> getFunctionArgs(IInvocationFlowNode invoke) {
        if (invoke instanceof InvokeNode) {
            InvokeNode in = (InvokeNode) invoke;
            return lookup(in.getFunc(), Value.class);
        } else {
            Set<Value> result = new HashSet<Value>();
            LoadAndInvokeNode lin = (LoadAndInvokeNode) invoke;
            for (ObjectValue obj : lookup(lin.getBase(), ObjectValue.class)) {
                for (ObjectValue proto : getAllPrototypes(obj, true)) {
                    result.addAll(lookupContextSensitive(new NamedPropertyKey(proto, lin.getProperty())));
                    result.addAll(lookupContextSensitive(obj.getDynamicStoreProperty()));
                }
            }
            return result;
        }
    }
	
	/**
	 * Determine the scope at a property expression.
	 */
	public Scope getScope(APropertyExp node) {
	    return astBinding.getScope(node.getAncestor(IScopeBlockNode.class));
	}
	
	public Scope getFunctionScope(IFunction function) {
	    return astBinding.getScope(function.getBody());
	}
	public Scope getCatchScope(ACatchClause node) {
		return astBinding.getScope(node);
	}
	
	/**
	 * Determines the scope at a variable declaration.
	 * 
	 * <p>
	 * <b>NB</b>: This is <i>not necessarily</i> the scope in which the variable is declared; for instance, in
	 * <pre>
	 * var o = {};
	 * with(o) {
	 *   var x = 23;
	 * }
	 * </pre>
	 * variable <code>x</code> is declared in global scope, yet the scope at the variable declaration is
	 * a <code>with</code> scope induced by the surrounding block.
	 *</p>
	 * 
	 * @param f
	 * @return a scope
	 */
	public Scope getScope(AVarDecl decl) {
		return astBinding.getScope(decl.getAncestor(IScopeBlockNode.class));
	}
	public Scope getScope(IScopeBlockNode node) {
		return astBinding.getScope(node);
	}
	
	public Set<ObjectValue> getWithStmtArguments(AWithStmt stmt) {
		HashSet<ObjectValue> result = new HashSet<ObjectValue>();
		for (EnterWith enter : astBinding.getEnterWith(stmt)) {
			result.addAll(getWithStmtArguments(enter));
		}
		return result;
	}
	public Set<ObjectValue> getWithStmtArguments(EnterWith enter) {
		return lookup(binding.getWithNode(enter).getInput(), ObjectValue.class);
	}
	
	public List<ScopeHost> getSearchedScopes(ANameExp exp, String name) {
	    for (IVariableAccessStatement stm : astBinding.getVariableAccesses(exp)) {
	    	// all statements will have the same scopes
	    	return getEnclosingScopes(stm.getScope(), name);
	    }
	    throw new RuntimeException("No statement is associated with that expression");
	}
	public List<ScopeHost> getSearchedScopes(AVarDecl decl, String name) {
		Scope scope = astBinding.getScope(decl.getAncestor(IScopeBlockNode.class));
		if (decl.getInit() != null) {
			return getEnclosingScopes(scope, name);
		} else {
			return Collections.singletonList(ScopeHost.fromScope(scope));
		}
	}

	private List<ScopeHost> getEnclosingScopes(Scope scope, String name) {
		List<ScopeHost> scopes = new ArrayList<ScopeHost>();
		while (scope != null && !scope.getDeclaredVariables().contains(name)) {
			scopes.add(new ScopeHost(scope));
			scope = scope.getParentScope();
		}
		if (scope != null && scope.getParentScope() == null)
			scope = null; // make global scope
		scopes.add(new ScopeHost(scope));
		return scopes;
	}
	
	/**
	 * Returns the set of objects the right hand side of the assignment may evaluate to.
	 */
	public Set<ObjectValue> getAssignmentRHS(AAssignExp exp) {
		Set<ObjectValue> result = new HashSet<ObjectValue>();
		for(WriteStatement write : astBinding.getWrites(exp)) {
			if(write instanceof WriteProperty) {
				for (IStoreFlowNode store : binding.getWriteProperty((WriteProperty)write)) {
					result.addAll(lookup(store.getValue(), ObjectValue.class));
				}
			} else {
				for (IVariableAccessFlowNode nd : binding.getVariableAccess((WriteVariable)write)) {
					result.addAll(lookup(((IVariableWriteFlowNode)nd).getValue(), ObjectValue.class));
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the reaching definitions of the right hand side of the assignment.
	 */
	public Set<VariableDefinition> getRHSReachingDefs(AAssignExp exp) {
		Set<VariableDefinition> result = new HashSet<VariableDefinition>();
		for(WriteStatement write : astBinding.getWrites(exp)) {
			Function f = write.getBlock().getFunction();
			ReachingDefinitions reachingDefinitions = binding.getReachingDefinitions(f);
			result.addAll(reachingDefinitions.getReachingDefinitions(write, write.getValueVar()));
		}
		return result;
	}

	public IFunction getFunctionNode(Function function) {
		return astBinding.getFunctionNode(function);
	}
	
	public Function getHarnessNativeFunction(String name) {
		Function func = pointers.getDataflow().getNamedHarnessFunctions().get(name);
		if (func == null) {
			throw new IllegalArgumentException("No native harness function is called " + name);
		}
		return func;
	}

	public Set<Value> getValuesOfVariable(ScopeHost declaringScope, String name) {
		if (declaringScope.isGlobal()) {
			return lookupContextSensitive(new NamedPropertyKey(GlobalObjectValue.Instance, name));
		} else {
			return flatkey2value.getView(new VariableKey(name, declaringScope.getScope(), NullContext.Instance));
		}
	}

	/**
	 * Determines whether the given property expression has intra-procedural data dependencies, i.e.
	 * whether it is preceded in the control-flow graph by a read, delete or write that could refer to the
	 * same property. Function invocations are conservatively assumed to write all properties.
	 */
	public boolean hasDataDependencies(APropertyExp exp) {
		// TODO: if exp is a read, we should only look for deletes and writes
		String name = Literals.getName(exp);
		Set<ObjectValue> bases = new PropertyExpAccess(exp).getBase(this, name);
		DataDependencyAnalysis analysis = new DataDependencyAnalysis(this, binding, bases, name);
        for(IPropertyAccessStatement stmt : astBinding.getPropertyAccesses(exp)) {
            Function func = stmt.getBlock().getFunction();
            ForwardFlowResult<Set<Statement>> result = ForwardFlowSolver.solve(func, analysis);
            if(!result.getBefore((Statement)stmt).isEmpty())
            	return true;
        }
        return false;
	}
	
	/**
	 * Returns true if <tt>eval</tt> may be invoked; false if it definitely is never invoked.
	 */
	public boolean isEvalUsed() {
		return !flatkey2value.getView(new NativeArgKey(EvalNative.Instance, NullContext.Instance, 0)).isEmpty();
	}
	
	/**
	 * Returns true if <tt>Function</tt> may be invoked; false if it definitely is never invoked.
	 */
	public boolean isFunctionConstructorUsed() {
		return !flatkey2value.getView(new NativeArgKey(FunctionNative.Instance, NullContext.Instance, 0)).isEmpty();
	}
	
	/**
	 * Returns true if <tt>innerHTML</tt> may be assigned to on a DOM object.
	 */
	public boolean isInnerHTMLAssignedTo() {
		final boolean[] b = new boolean[1];
		for (Function f : pointers.getDataflow().getFunctionFlownodes().keySet()) {
			for (FlowNode node : pointers.getDataflow().getFunctionFlownodes().getView(f)) {
				node.apply(new AbstractFlowNodeVisitor() {
					@Override
					public void caseStore(StoreNode node) {
						if (node.getProperty().equals("innerHTML") && lookup(node.getBase(), ObjectValue.class).contains(DOMNative.Instance)) {
							b[0] = true;
						}
					}
					@Override
					public void caseStoreIfPresent(StoreIfPresentNode node) {
						if (node.getProperty().equals("innerHTML") && lookup(node.getBase(), ObjectValue.class).contains(DOMNative.Instance)) {
							b[0] = true;
						}
					}
					@Override
					public void caseStoreDynamic(StoreDynamicNode node) {
						Set<Value> s = lookup(node.getProperty(), Value.class);
						if (s.contains(StringValue.Instance) || CollectionUtil.containsInstanceOf(s, ObjectValue.class)) {
							if (lookup(node.getBase(), ObjectValue.class).contains(DOMNative.Instance)) {
								b[0] = true;
							}
						}
					}
				});
			}
		}
		return b[0];
	}
	
	/**
	 * Returns true if either {@link #isEvalUsed()}, {@link #isFunctionConstructorUsed()}, or {@link #isInnerHTMLAssignedTo()}
	 * is true.
	 */
	public boolean isDynamicCodeUsed() {
		return isEvalUsed() || isFunctionConstructorUsed() || isInnerHTMLAssignedTo();
	}
	
	/**
	 * Given a variable <i>V</i> declared in <i>F</i>, we say <i>V</i> refers to a common closure function if:
	 * <blockquote>
	 * At any point, for any instance <i>F<sub>1</sub></i> of <i>F</i>, the variable <i>V</i> in <i>F<sub>1</sub></i> is either uninitialized, or refers to
	 * a function instance whose lexical environment is enclosed by the lexical environment of <i>F<sub>1</sub></i>. 
	 * </blockquote>
	 * Calling such a method from within <i>F</i> ensures that all closure variable declared by <i>F</i> or
	 * its ancestor scopes are read from the same closure instance.
	 * @param varName a variable name, declared in the given function
	 * @param func a function
	 * @return true for definite, false for maybe not
	 */
	public boolean refersToCommonClosureFunction(final String varName, final Function func) {
		if (func.getOuterFunction() == null)
			return true; // global scope
		assert func.getDeclaredVariables().contains(varName);
		final boolean result[] = new boolean[] { true };
		for (final Function f : func.getTransitiveInnerFunctions(true)) {
			for (Block b : f.getBlocks()) {
				for (Statement stm : b.getStatements()) {
					stm.apply(new AbstractStatementVisitor(){
						@Override
						public void caseWriteVariable(WriteVariable stm) {
							if (!stm.getVarName().equals(varName) || stm.getScope().getDeclaringScope(varName) != func)
								return;
							ReachingDefinitions reachingDefinitions = binding.getReachingDefinitions(f);
							for (VariableDefinition def : reachingDefinitions.getReachingDefinitions(stm, stm.getValueVar())) {
								def.apply(new VariableDefinitionVisitor() {
									@Override
									public void caseUninitialized(UninitializedVariableDefinition definition) {
									}
									@Override
									public void caseStatement(StatementVariableDefinition def) {
										if (!(def.getStatement() instanceof CreateFunction)) {
											result[0] = false;
										}
									}
									@Override
									public void caseParameter(ParameterVariableDefinition def) {
										result[0] = false;
									}
									@Override
									public void caseArgumentsArray(ArgumentsArrayVariableDefinition def) {
										result[0] = false;
									}
									@Override
									public void caseSelf(SelfVariableDefinition def) {
									}
								});
							}
						}
					});
				}
			}
		}
		return result[0];
	}
	
	/**
	 * Returns a set of <i>this-unique</i> functions. Such functions can be invoked at most
	 * once per value of <tt>this</tt>.
	 */
	public Set<Function> getThisUniqueFunctions() {
		final Set<Function> functions = new HashSet<Function>(pointers.getDataflow().getFunctionFlownodes().keySet());
		
		// TODO: Handle calls to superconstructors [currently sound]
		
		// remove all functions invoked as a non-constructor
		for (InputFile file : allFiles) {
			for (Function func : file.getCfg().getTransitiveInnerFunctions(true)) {
				for (FlowNode node : pointers.getDataflow().getFunctionFlownodes().getView(func)) {
					node.apply(new AbstractFlowNodeVisitor() {
						private void removeFunctionsAtKeys(Key ... keys) {
							for (Key key : keys) {
								for (UserFunctionValue uf : lookupContextSensitive(key, UserFunctionValue.class)) {
									functions.remove(uf.getFunction());
								}
							}
						}
						@Override
						public void caseCoerceToPrimitive(CoerceToPrimitive node) {
							for (ObjectValue obj : lookup(node.getValue(), ObjectValue.class)) {
								removeFunctionsAtKeys(
										new NamedPropertyKey(obj, "toString"),
										new NamedPropertyKey(obj, "valueOf"),
										obj.getDynamicStoreProperty());
							}
						}
						@Override
						public void caseInvoke(InvokeNode node) {
							if (node.isConstructor())
								return;
							for (UserFunctionValue uf : lookup(node.getFunc(), UserFunctionValue.class)) {
								functions.remove(uf.getFunction());
							}
						}
						@Override
						public void caseLoadAndInvoke(LoadAndInvokeNode node) {
							if (node.isConstructor())
								return;
							for (ObjectValue obj : lookup(node.getBase(), ObjectValue.class)) {
								removeFunctionsAtKeys(
										new NamedPropertyKey(obj, node.getProperty()),
										obj.getDynamicStoreProperty());
							}
						}
					});
				}
			}
		}
		// handle all indirect calls through .call or .apply or .bind
		for (NativeFunctionValue nativ : new NativeFunctionValue[] {FunctionCallNative.Instance, FunctionApplyNative.Instance, FunctionBindNative.Instance}) {
			for (UserFunctionValue uf : lookupContextInsensitive(nativ.getThisArg(NullContext.Instance), UserFunctionValue.class)) {
				functions.remove(uf.getFunction());
			}
		}
		
		
		return functions;
	}
	
	public boolean isFunctionReachable(IFunction f) {
	    Function func = astBinding.getFunction(f);
	    return reachableContexts.keySet().contains(func);
	}

	// debugging function constructing a description of an object value
	// TODO: cover more cases
	public String describeObjectValue(ObjectValue v) {
		if(v instanceof AllocObjectValue) {
			Object allocsite = ((AllocObjectValue)v).getAllocsite();
			if(allocsite instanceof StatementAllocSite) {
				Statement stmt = ((StatementAllocSite)allocsite).getStatement();
				if(stmt instanceof InvokeStatement) {
					StringBuffer res = new StringBuffer();
					res.append("object allocated at line");
					for(IInvocationNode nd : getAllNodesOfType(IInvocationNode.class))
						if(astBinding.getInvokeStatements(nd).contains(stmt))
							res.append(" " + getTranslatedLineNumber((NodeInterface)nd) + " (" + nd +")");
					return res.toString();
				} else if(stmt instanceof NewObject) {
					StringBuffer res = new StringBuffer();
					res.append("object literal at line");
					for(AObjectLiteralExp lit : getAllNodesOfType(AObjectLiteralExp.class))
						if(astBinding.getObjectLiteral(lit).contains(stmt))
							res.append(" " + getTranslatedLineNumber(lit) + " (" + AstUtil.toSourceString(lit) + ")");
					return res.toString();
				} else {
					return "object allocated at statement of type " + stmt.getClass();
				}
			} else {
				return "object allocated at location of class " + allocsite.getClass();
			}
		} else {
			return "object value of class " + v.getClass();
		}
	}
	
	public String describeObjectValues(Collection<? extends ObjectValue> vs) {
		StringBuffer desc = new StringBuffer("[");
		boolean fst = true;
		for(ObjectValue v : vs) {
			if(fst)
				fst = false;
			else
				desc.append(", ");
			desc.append(describeObjectValue(v));
		}
		desc.append("]");
		return desc.toString();
	}
}
