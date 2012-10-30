package dk.brics.jsrefactoring.encapsulateprty;

import static dk.brics.jsrefactoring.NodeFactory.createAssignExp;
import static dk.brics.jsrefactoring.NodeFactory.createBlock;
import static dk.brics.jsrefactoring.NodeFactory.createExpStmt;
import static dk.brics.jsrefactoring.NodeFactory.createFunctionExp;
import static dk.brics.jsrefactoring.NodeFactory.createInvokeExp;
import static dk.brics.jsrefactoring.NodeFactory.createNameExp;
import static dk.brics.jsrefactoring.NodeFactory.createPropertyExp;
import static dk.brics.jsrefactoring.NodeFactory.createReturnStmt;
import static dk.brics.jsrefactoring.NodeFactory.createThisExp;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.APostfixUnopExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.EExp;
import dk.brics.jsparser.node.EPostfixUnop;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.AccessFinder;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFactory;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.PrettyPrinter;
import dk.brics.jsrefactoring.PropertyNameFamily;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.changes.InsertExpIntoArglist;
import dk.brics.jsrefactoring.changes.InsertStmtIntoBlock;
import dk.brics.jsrefactoring.changes.ReplaceExp;
import dk.brics.jsrefactoring.family.FamilyClosure;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.NameExpAccess;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsutil.CollectionUtil;

/**
 * Implementation of the <span style="font-variant: small-caps">Encapsulate Property</span> refactoring.
 * <p>
 * This refactoring encapsulates as many accesses to a property as possible to go through accessor methods,
 * which are inserted into the constructor function.
 * </p>
 * <p>
 * For instance, consider the following program:
 * <pre>
 * function Rectangle(w, h) {
 *   this.width = w;
 *   this.height = h;
 * }
 * 
 * var r = new Rectangle(23, 42);
 * var w = r.width;
 * r.width = 56;
 * </pre>
 * Encapsulating property <tt>width</tt> of <tt>Rectangle</tt> yields
 * <pre>
 * function Rectangle(w, h) {
 *   var width;
 *   this.getWidth = function() { return width; };
 *   this.setWidth = function(width) { return width = width; };
 *   width = w;
 *   this.height = h;
 * }
 * 
 * var r = new Rectangle(23, 42);
 * var w = r.getWidth();
 * r.setWidth(56);
 * </pre>
 * </p>
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class EncapsulateProperty extends Refactoring {
	private final PropertyExpAccess target;
	private final String targetName, ucaseTargetName, getterName, setterName;
	
	public EncapsulateProperty(Master input, APropertyExp target) {
		super(input);
		this.target = new PropertyExpAccess(target);
		this.targetName = Literals.getName(target);
        this.ucaseTargetName = targetName.substring(0, 1).toUpperCase() + targetName.substring(1);
		this.getterName = "get" + ucaseTargetName;
        this.setterName = "set" + ucaseTargetName;
	}
	
	@Override
	public List<Change> getChanges() {
		List<Change> changes = new LinkedList<Change>();
		
    	// if the expression is unreachable, don't bother
        if(target.getReceivers(input).isEmpty())
        	log.warn(target.getNode(), "Expression %s is unreachable", target.getNode());
        
        // TODO: this test should be more disciplined
		if(targetName.equals("toString") || targetName.equals("valueOf")) {
			log.fatal(target.getNode(), "Cannot encapsulate intrinsic property.");
			return changes;
		}
        
        NodeFinder finder = new NodeFinder(input, IPropertyAccessNode.class, ANameExp.class, IFunction.class,
        										  ANormalObjectLiteralProperty.class, ABinopExp.class);
        FamilyClosure<ObjectValue, AccessWithName> familyClosure = 
        	FamilyClosure.compute(new PropertyNameFamily(input, targetName),
                			  	  AccessFinder.getNamedAccesses(finder, targetName),
                			  	  Collections.singleton(target), Collections.<ObjectValue>emptySet());
        Set<ObjectValue> affectedObjects = familyClosure.getAffectedObjects();
        Set<AccessWithName> affectedNames = familyClosure.getAffectedFamilies();
        
        IFunction ctor = target.getNode().getAncestor(IFunction.class);
        final Set<Function> ctorFuns = input.getFunctions(ctor);
        if(ctor == null || !new InitialiserAnalysis(input, finder).isInitialiser(ctor)) {
        	log.fatal("Unable to find suitable constructor.");
        	return changes;
        }
        if(!input.getReceivers(ctor).containsAll(affectedObjects))
        	log.warn(target.getNode(), "Some affected objects may not be initialized by %s.", Literals.getName(ctor));
        
        Set<PropertyExpAccess> inner = new HashSet<PropertyExpAccess>();
        Set<AccessWithName> get = new HashSet<AccessWithName>(),
        			        set = new HashSet<AccessWithName>();
		ScopeConsistencyAnalysis analysis = new ScopeConsistencyAnalysis(input, finder, ctorFuns);
		boolean needGetter = classifyAccesses(affectedNames, inner, get, set, analysis);
        
        // check for name capture by reflection
        checkReflectiveNameCapture(affectedObjects, targetName, finder);
        
        // check for naming issues on inner
        if(!inner.isEmpty()) {
        	// check that targetName is not captured at a
        	for(AccessWithName a : inner) {
    			Scope scope = input.getScope(a.getNode().getAncestor(IScopeBlockNode.class));
    			while(scope.getParentScope() != null) {
   					if(scope.getDeclaredVariables().contains(targetName))
   						log.error("Reference to variable " + targetName +  " will be captured by a scope enclosing the callsite");
   					if(scope instanceof WithScope)
   						for(ObjectValue obj : input.getWithStmtArguments(((WithScope)scope).getStatement()))
    						if(!input.isPropertyDefinitelyAbsent(obj, targetName))
    							log.warn("Reference to variable " + targetName + " may be captured by a with-statement enclosing the callsite");
    				scope = scope.getParentScope();
    			}
        	}
        	
    		// check that no existing name expressions are captured by the inserted local variable
    		ctor.getBody().apply(new DepthFirstAdapter() {
    			@Override
    			public void caseANameExp(ANameExp node) {
    				if(Literals.getName(node).equals(targetName))
    					if(new NameExpAccess(node).getSearchedScopes(input).contains(ScopeHost.fromScope(ctorFuns.iterator().next())))
   							log.error(node, "Name %s will be captured.", node);
    				super.caseANameExp(node);
    			}
    		});
        }
        
        // check for naming issues on get
        if(!get.isEmpty()) {
        	for(AccessWithName a : get) {
        		if(!a.getBase(input, getterName).isEmpty())
        			log.error(a.getNode(), "Getter method invocation may become captured.");
        	}
    		// check for name capture
    		for(AccessWithName a : AccessFinder.getNamedAccesses(finder, getterName)) {
    			if(CollectionUtil.intersects(a.getReceivers(input), affectedObjects))
    				log.error(a.getNode(), "Access %s may be captured when inserting getter method.", a.getNode());
    		}
        }
        
        // check for naming issues on set
        if(!set.isEmpty()) {
        	for(AccessWithName a : set) {
        		if(!a.getBase(input, setterName).isEmpty())
        			log.error(a.getNode(), "Setter method invocation may become captured.");
        	}
    		// check for name capture
    		for(AccessWithName a : AccessFinder.getNamedAccesses(finder, setterName)) {
    			if(CollectionUtil.intersects(a.getReceivers(input), affectedObjects))
    				log.error(a.getNode(), "Access %s may be captured when inserting setter method.", a.getNode());
    		}
        }
        
        // create changes
        if(!set.isEmpty()) {
    		// function(newX) { return x = newX; }
    		String parmName = "new" + ucaseTargetName;
    		AFunctionExp setterBody = createFunctionExp(Collections.singletonList(parmName),
    				createBlock(createReturnStmt(createAssignExp(createNameExp(targetName),	createNameExp(parmName)))));
    		PStmt assgn = createExpStmt(createAssignExp(createPropertyExp(createThisExp(), setterName), setterBody));
    		PrettyPrinter.pp(assgn);
    		changes.add(new InsertStmtIntoBlock(ctor.getBody().getBlock(), 0, assgn));
        }
        if(needGetter) {
        	// function() { return x; }
        	AFunctionExp getterBody = createFunctionExp(Collections.<String>emptyList(), 
        			createBlock(createReturnStmt(createNameExp(targetName))));
    		PStmt assgn = createExpStmt(createAssignExp(createPropertyExp(createThisExp(), getterName), getterBody));
    		PrettyPrinter.pp(assgn);
    		changes.add(new InsertStmtIntoBlock(ctor.getBody().getBlock(), 0, assgn));        	
        }
   		// insert local variable declaration into constructor
   		AVarDeclStmt decl = NodeFactory.createVarDeclStmt(targetName);
   		PrettyPrinter.pp(decl);
   		changes.add(new InsertStmtIntoBlock(ctor.getBody().getBlock(), 0, decl));
   		
   		refactorAccesses(changes, inner, get, set);
        
        return changes;
	}

	public boolean classifyAccesses(Set<AccessWithName> affectedNames, Set<PropertyExpAccess> inner, Set<AccessWithName> get,
			Set<AccessWithName> set, ScopeConsistencyAnalysis analysis) {
		boolean needGetter = false;
		for(AccessWithName affected : affectedNames) {
        	if(input.isNativeCode(affected.getNode())) {
        		log.error("Cannot refactor intrinsic properties.");
        		continue;
        	}
        	
        	// first check whether this access could be turned into a local variable reference
        	if(affected instanceof PropertyExpAccess) {
        		PropertyExpAccess pacc = (PropertyExpAccess)affected;
        		IFunction enc = pacc.getNode().getAncestor(IFunction.class);
        		if(pacc.getNode().getBase().kindPExp() == EExp.THIS) {
        			if(!isDeleteOperand(pacc.getNode()) && enc != null && analysis.isWellScoped(enc)) {
        				inner.add(pacc);
        				continue;
        			}
        		}
        	}
        	
        	// otherwise, check whether it should become a getter or setter invocation
        	if(affected.isLValue()) {
        		if(isDeleteOperand(affected.getNode()) || !(affected instanceof PropertyExpAccess)) {
        			log.error(affected.getNode(), "Cannot refactor access %s.", affected.getNode());
        		} else {
        			if(affected.isRValue() && affected.getNode() instanceof PExp && mayHaveSideEffects((PExp)affected.getNode()))
        				log.warn(affected.getNode(), "Side effects of %s may be duplicated.", affected.getNode());
        			needGetter |= affected.isRValue();
        			set.add(affected);
        		}
        	} else {
        		if(!(affected instanceof PropertyExpAccess)) {
        			log.error(affected.getNode(), "Cannot refactor access %s.", affected.getNode());
        		} else {
        			needGetter = true;
        			get.add(affected);
        		}
        	}
        }
		return needGetter;
	}

	public void refactorAccesses(List<Change> changes,	Set<PropertyExpAccess> inner, Set<AccessWithName> get, Set<AccessWithName> set) {
		for(PropertyExpAccess a : inner)
			refactorInnerAccess(changes, a);   			
   		
   		for(AccessWithName a : get)
   			refactorGetterAccess(changes, a);   			
   		
   		for(AccessWithName a : set)
   			refactorSetterAccess(changes, a);
	}

	public void refactorInnerAccess(List<Change> changes, PropertyExpAccess a) {
		// transform this.n(es) to n.call(this, es) if the callee may use "this", otherwise to n(es)
		AInvokeExp inv = getInvocationParent(a.getNode());
		if(inv == null || !calleeMayUseThis(inv)) {
			changes.add(new ReplaceExp(a.getNode(), NodeFactory.createNameExp(targetName)));
		} else {
			changes.add(new ReplaceExp(inv.getFunctionExp(), 
									   NodeFactory.createPropertyExp(NodeFactory.createNameExp(targetName), "call")));
			changes.add(new InsertExpIntoArglist(inv.getLparen(), inv.getArguments(), inv.getRparen(), 0, NodeFactory.createThisExp()));
		}
	}

	public void refactorGetterAccess(List<Change> changes, AccessWithName a) {
		APropertyExp nd = (APropertyExp)a.getNode();
		PExp base_clone = (PExp)nd.getBase().clone();
		changes.add(new ReplaceExp(nd, createInvokeExp(createPropertyExp(base_clone, getterName), Collections.<PExp>emptyList())));
	}

	public void refactorSetterAccess(List<Change> changes, AccessWithName a) {
		APropertyExp exp = (APropertyExp)a.getNode();
		Node nd = AstUtil.getRealParent(exp);
		if(nd instanceof AAssignExp)
			refactorAssignment(changes, exp, (AAssignExp)nd);
		else if(nd instanceof APrefixUnopExp)
			refactorPrefixUnop(changes, exp, (APrefixUnopExp)nd);
		else if(nd instanceof APostfixUnopExp)
			refactorPostfixUnop(changes, exp, (APostfixUnopExp)nd);
	}

	public void refactorAssignment(List<Change> changes, APropertyExp exp, AAssignExp assgn) {
		PExp base_clone = (PExp)exp.getBase().clone(),
			 getter_call = createInvokeExp(createPropertyExp(base_clone, getterName), Collections.<PExp>emptyList()),
			 rhs_clone = (PExp)assgn.getRight().clone();
		PExp setter_arg = null;
		switch(assgn.getOp().kindPAssignOp()) {
		case BITWISE_AND:
			setter_arg = NodeFactory.createBinOp(EBinop.BITWISE_AND, getter_call, rhs_clone);
			break;
		case BITWISE_OR:
			setter_arg = NodeFactory.createBinOp(EBinop.BITWISE_OR, getter_call, rhs_clone);
			break;
		case BITWISE_XOR:
			setter_arg = NodeFactory.createBinOp(EBinop.BITWISE_XOR, getter_call, rhs_clone);
			break;
		case DIVIDE:
			setter_arg = NodeFactory.createBinOp(EBinop.DIVIDE, getter_call, rhs_clone);
			break;
		case MINUS:
			setter_arg = NodeFactory.createBinOp(EBinop.MINUS, getter_call, rhs_clone);
			break;
		case MODULO:
			setter_arg = NodeFactory.createBinOp(EBinop.MODULO, getter_call, rhs_clone);
			break;
		case NORMAL:
			setter_arg = rhs_clone;
			break;
		case PLUS:
			setter_arg = NodeFactory.createBinOp(EBinop.PLUS, getter_call, rhs_clone);
			break;
		case SHIFT_LEFT:
			setter_arg = NodeFactory.createBinOp(EBinop.SHIFT_LEFT, getter_call, rhs_clone);
			break;
		case SHIFT_RIGHT:
			setter_arg = NodeFactory.createBinOp(EBinop.SHIFT_RIGHT, getter_call, rhs_clone);
			break;
		case SHIFT_RIGHT_UNSIGNED:
			setter_arg = NodeFactory.createBinOp(EBinop.SHIFT_RIGHT_UNSIGNED, getter_call, rhs_clone);
			break;
		case TIMES:
			setter_arg = NodeFactory.createBinOp(EBinop.TIMES, getter_call, rhs_clone);
			break;
		}
		changes.add(new ReplaceExp(assgn, createInvokeExp(createPropertyExp((PExp)exp.getBase().clone(), setterName), 
														  Collections.singletonList(setter_arg))));
	}

	public void refactorPrefixUnop(List<Change> changes, APropertyExp exp, APrefixUnopExp pre) {
		PExp base_clone1 = (PExp)exp.getBase().clone(),
			 base_clone2 = (PExp)exp.getBase().clone();
		// e.getX()
		AInvokeExp getter_call = createInvokeExp(createPropertyExp(base_clone2, getterName), 
												 Collections.<PExp>emptyList());
		PExp setter_arg = null;
		if(pre.getOp().kindPPrefixUnop() == EPrefixUnop.INCREMENT)
			// e.getX()+1
			setter_arg = NodeFactory.createBinOp(EBinop.PLUS, getter_call, NodeFactory.createLiteral(1));
		else
			// e.getX()-1
			setter_arg = NodeFactory.createBinOp(EBinop.MINUS, getter_call, NodeFactory.createLiteral(1));
		// e.setX(e.getX()+-1)
		changes.add(new ReplaceExp(pre, createInvokeExp(createPropertyExp(base_clone1, setterName),	Collections.singletonList(setter_arg))));
	}

	public void refactorPostfixUnop(List<Change> changes, APropertyExp exp,	APostfixUnopExp post) {
		// postfix expressions in non-void context are tricky to refactor; we just give up
		if(!AstUtil.inVoidContext(post)) {
			log.error(post, "Cannot refactor %s.", post);
		} else {
			PExp base_clone1 = (PExp)exp.getBase().clone(),
			base_clone2 = (PExp)exp.getBase().clone();
			// e.getX()
			AInvokeExp getter_call = createInvokeExp(createPropertyExp(base_clone2, getterName), 
					Collections.<PExp>emptyList());
			PExp setter_arg = null;
			if(post.getOp().kindPPostfixUnop() == EPostfixUnop.INCREMENT)
				// e.getX()+1
				setter_arg = NodeFactory.createBinOp(EBinop.PLUS, getter_call, NodeFactory.createLiteral(1));
			else
				// e.getX()-1
				setter_arg = NodeFactory.createBinOp(EBinop.MINUS, getter_call, NodeFactory.createLiteral(1));
			// e.setX(e.getX()+-1)
			changes.add(new ReplaceExp(post, createInvokeExp(createPropertyExp(base_clone1, setterName), Collections.singletonList(setter_arg))));
		}
	}
}
