package dk.brics.jsrefactoring.movetoprototype;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.AnalysisAdapter;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ACommaExp;
import dk.brics.jsparser.node.AConditionalExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AEmptyExp;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APostfixUnopExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.values.FunctionPrototypeValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jsrefactoring.AccessFinder;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFactory;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.changes.InsertStmtIntoBlock;
import dk.brics.jsrefactoring.changes.RemoveStmtFromBlock;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.natives.DynamicAccessNative;
import dk.brics.jsrefactoring.natives.OwnPropertyAccessNative;
import dk.brics.jsrefactoring.nodes.Access;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsutil.CollectionUtil;

/**
 * Implementation of the <span style="font-variant: small-caps">Move to Prototype</span> refactoring.
 * <p>
 * This refactoring allows to move a property definition from a constructor into the constructor's prototype.
 * For instance, consider the following program:
 * <pre>
 * function Rectangle(w, h) {
 *   this.width = w;
 *   this.height = h;
 *   this.area = function() {
 *      return this.width * this.area;
 *   };
 * }
 * 
 * var r = new Rectangle(23, 42);
 * var a = r.area();
 * </pre>
 * We might want to move <tt>area</tt> from <tt>Rectangle</tt> to <tt>Rectangle.prototype</tt> to avoid creating
 * a separate copy of the method for every rectangle. The result of the refactoring would be
 * <pre>
 * function Rectangle(w, h) {
 *   this.width = w;
 *   this.height = h;
 * }
 * Rectangle.prototype.area = function() {
 *    return this.width * this.area;
 * };
 * 
 * var r = new Rectangle(23, 42);
 * var a = r.area();
 * </pre>
 * </p>
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class MoveToPrototype extends Refactoring {
	private final AAssignExp assgn;

	public MoveToPrototype(Master input, AAssignExp assgn) {
		super(input);
		this.assgn = assgn;
	}

	@Override
	public List<Change> getChanges() {
		List<Change> changes = new LinkedList<Change>();
		
		if(!(assgn.parent() instanceof AExpStmt)) {
			log.fatal(assgn, "The assignment % is not an assignment statement.", assgn);
			return changes;
		}
		AExpStmt stmt = (AExpStmt)assgn.parent();
		
		if(!(assgn.getLeft() instanceof APropertyExp) 
				|| !(((APropertyExp)assgn.getLeft()).getBase() instanceof AThisExp)) {
			log.fatal(assgn.getLeft(), "Left hand side must be property expression on 'this', which %s is not.", assgn.getLeft());
			return changes;
		}
		APropertyExp lhs = (APropertyExp)assgn.getLeft();
		final String name = Literals.getName(lhs);
		PExp rhs = assgn.getRight();
		
		IFunction fun = stmt.getAncestor(IFunction.class);
		if(!(fun instanceof AFunctionDeclStmt) || !(fun.parent() instanceof ABlock)) {
			log.fatal(assgn, "Assignment must appear in a function declaration in a block.");
			return changes;
		}
		ABlock fun_block = (ABlock)fun.parent();
		Set<FunctionValue> instances = input.getInitializedFunctionInstances(fun);
		Set<ObjectValue> receivers = input.getReceivers(fun);
		NodeFinder finder = new NodeFinder(input, IPropertyAccessNode.class, APrefixUnopExp.class, ANameExp.class, 
												  ANormalObjectLiteralProperty.class, ABinopExp.class);

		if(input.hasDataDependencies(lhs))
			log.warn(lhs, "The property may be read or written before this assignment.");
		checkInitializer(rhs, fun);
		PStmt prototypeAssignment = checkAccesses((AFunctionDeclStmt)fun, lhs, instances, receivers, finder);
		checkDelete(name, receivers, finder);
		checkImmutable(finder, input.getAssignmentRHS(assgn));
		
        for(OwnPropertyAccessNative problematicNative : OwnPropertyAccessNative.LIST) {
        	Function f = input.getHarnessNativeFunction(problematicNative.getMember().getCodeName());
        	UserFunctionValue obj = new UserFunctionValue(f, MainContext.Instance);
        	String argName = problematicNative.getArgumentIndex() == DynamicAccessNative.THIS ? "this" : obj.getFunction().getParameterNames().get(problematicNative.getArgumentIndex());
        	Set<ObjectValue> values = CollectionUtil.filter(input.getValuesOfVariable(ScopeHost.fromScope(f), argName), ObjectValue.class);
        	values = input.getAllPrototypes(values, true);
        	if (CollectionUtil.intersects(receivers, values))
        		log.error("An invocation of native function %s may change its result.", problematicNative.getMember().getPrettyName());
		}
		
		ABlock stmt_block = (ABlock)stmt.parent();
		int stmt_idx = stmt_block.getStatements().indexOf(stmt);
		changes.add(new RemoveStmtFromBlock(stmt_block, stmt_idx));
		
		int insert_idx = fun_block.getStatements().indexOf(prototypeAssignment == null ? fun : prototypeAssignment);
		AExpStmt stmt_clone = NodeFactory.clone(stmt);
		AstUtil.replaceNode(((APropertyExp)((AAssignExp)stmt_clone.getExp()).getLeft()).getBase(),
							NodeFactory.createPropertyExp(NodeFactory.createNameExp(Literals.getName(fun)), "prototype"));
		changes.add(new InsertStmtIntoBlock(fun_block, insert_idx+1, stmt_clone));
		
		return changes;
	}

	public PStmt checkAccesses(AFunctionDeclStmt fun, APropertyExp lhs, Set<FunctionValue> instances, Set<ObjectValue> receivers, NodeFinder finder) {
		String name = Literals.getName(lhs);
		Set<Value> prototypes = getPrototypes(instances);
		PStmt prototypeAssignment = null;
		for(AccessWithName acc : AccessFinder.getNamedAccesses(finder, name)) {
			if(acc.getNode() == lhs)
				continue;
			// if this access looks the name up on one of the receivers, that receiver's __proto__ must be fun's prototype
			for(ObjectValue base : acc.getBase(input, name)) {
				if(receivers.contains(base)) {
					Set<ObjectValue> baseproto = input.getDirectPrototypes(base);
					if(baseproto.isEmpty() || !prototypes.containsAll(baseproto))
						log.error(acc.getNode(), "Binding of access %s may be broken by refactoring.", acc.getNode());
				}
			}
			
			// if this access looks the name up on one of fun's prototypes, make sure that it goes through one of the receivers, too
			Set<ObjectValue> expreceivers = acc.getReceivers(input);
			// TODO: is this check sufficient?
			if(CollectionUtil.intersects(expreceivers, prototypes))
				if(!CollectionUtil.intersects(expreceivers, receivers))
					log.error(acc.getNode(), "Access %s may be captured by refactoring.", acc.getNode());
			
		}
		
		// check for assignments to fun.prototype
		for(IPropertyAccessNode exp : finder.getAllNodesOfType(IPropertyAccessNode.class)) {
			Access nd = Access.of(exp);
			if(input.mayHaveName(exp, "prototype") && AstUtil.isLValue((PExp)exp)) {
				if(CollectionUtil.intersects(nd.getBase(input, "prototype"), instances)) {
					if(prototypeAssignment != null)
						log.error(exp, "Function prototype is assigned more than once.");
					if(exp.parent() instanceof AAssignExp && ((AAssignExp)exp.parent()).getLeft() == exp) {
						if(exp.parent().parent() instanceof PStmt) {
							prototypeAssignment = (PStmt)exp.parent().parent();
							// check that assignment is done in statement immediately following function declaration
							if(prototypeAssignment.parent() != fun.parent()) {
								log.error("Function prototype must be assigned immediately after function declaration");
							} else {
								LinkedList<PStmt> stmts = ((ABlock)fun.parent()).getStatements();
								if(stmts.indexOf(prototypeAssignment) != stmts.indexOf(fun) + 1)
									log.error("Function prototype must be assigned immediately after function declaration");
							}
							continue;
						}
					}
				}
			}
		}
		return prototypeAssignment;
	}

	public void checkDelete(String name, Set<ObjectValue> receivers, NodeFinder finder) {
		// check that property is not deleted
		for(APrefixUnopExp exp : finder.getAllNodesOfType(APrefixUnopExp.class)) {
			if(exp.getOp().kindPPrefixUnop() != EPrefixUnop.DELETE)
				continue;
			if(exp.getExp() instanceof ANameExp) {
				ANameExp node = (ANameExp)exp.getExp();
				if(Literals.getName(node).equals(name))
					if(CollectionUtil.intersects(input.getWithScopeReceivers(node), receivers))
						log.error(exp, "Property to be moved could be deleted by expression %s.", exp);
			} else if(exp.getExp() instanceof IPropertyAccessNode) {
				Access node = Access.of((IPropertyAccessNode)exp.getExp());
				if(input.mayHaveName((IPropertyAccessNode)exp.getExp(), name))
					if(CollectionUtil.intersects(node.getReceivers(input), receivers))
						log.error(exp, "Property to be moved could be deleted by expression %s.", exp);
			}
		}
	}

	public Set<Value> getPrototypes(Set<FunctionValue> instances) {
		Set<Value> prototypes = new HashSet<Value>();
		for(FunctionValue inst : instances)
			prototypes.addAll(input.getPropertyValue(inst, "prototype"));
		return prototypes;
	}

	public void checkInitializer(PExp rhs, IFunction fun) {
		// there cannot be any calls, variable, property or "this" references outside functions
		if(!rhs.apply(new FlowInsensitivityAnalysis()))
			log.warn(rhs, "Initializer expression may yield different results after refactoring.");
		// inside nested functions, there cannot be any references to local variables of "fun"
		// (it is easier to just check that there are no references to local variables anywhere, although this somewhat overlaps with the
		// above checks)
		final Set<Function> scopes = input.getFunctions(fun);
		rhs.apply(new DepthFirstAdapter() {
			@Override
			public void inANameExp(ANameExp node) {
				for(Scope scope : input.getDeclaringScopes(node))
					if(scopes.contains(scope))
						log.error(node, "Initializer refers to local variable");
			}
		});
	}
	
	// check that no object reachable from any of the given objects ever has a property assigned
	// or is subjected to an equality comparison
	private void checkImmutable(NodeFinder finder, Set<ObjectValue> objects) {
		Set<ObjectValue> reachable = new HashSet<ObjectValue>();
		LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>(objects);
		while(!queue.isEmpty()) {
			ObjectValue obj = queue.pop();
			reachable.add(obj);
			// it seems that we only need to close under "prototype"--but why?
			if(obj instanceof FunctionValue) {
				FunctionPrototypeValue prototype = ((FunctionValue)obj).getFunctionPrototype();
				if(reachable.add(prototype))
					queue.add(prototype);
			}
		}
		
		// cannot have writes whose base is a reachable object
		for(IPropertyAccessNode access : finder.getAllNodesOfType(IPropertyAccessNode.class)) {
			if(AstUtil.isLValue((PExp)access) && 
					CollectionUtil.intersects(Access.of(access).getReceivers(input), reachable))
				log.warn(access, "Access %s writes state that may be reachable from the moved object.", access);
		}
		for(ANameExp exp : finder.getAllNodesOfType(ANameExp.class)) {
			if(AstUtil.isLValue(exp) &&
					CollectionUtil.intersects(input.getWithScopeReceivers(exp), reachable))
				log.warn(exp, "Expression %s writes state that may be reachable from the moved object.", exp);
		}
		for(ANormalObjectLiteralProperty prop : finder.getAllNodesOfType(ANormalObjectLiteralProperty.class))
			if(CollectionUtil.intersects(input.getInitializedObjects(prop), reachable))
				log.warn(prop, "Property %s will become shared.", prop);
		
		// cannot compare reachable objects
		for(ABinopExp exp : finder.getAllNodesOfType(ABinopExp.class)) {
			EBinop k = exp.getOp().kindPBinop();
			if(k == EBinop.EQUAL || k == EBinop.EQUAL_STRICT 
					|| k == EBinop.NOT_EQUAL || k == EBinop.NOT_EQUAL_STRICT) {
				if(mayPointTo(exp.getLeft(), objects) && mayPointTo(exp.getRight(), objects))
					log.warn(exp, "The outcome of comparison %s may be changed by the refactoring.", exp);
			}
		}
	}
	
	// TODO: this should be done in a more disciplined way
	private boolean mayPointTo(PExp exp, final Set<ObjectValue> objects) {
		final boolean[] result = new boolean[1];
		result[0] = true;
		exp.apply(new AnalysisAdapter() {
			@Override public void caseAArrayLiteralExp(AArrayLiteralExp exp) { result[0] = false; }
			@Override public void caseAAssignExp(AAssignExp exp) { result[0] = mayPointTo(exp.getRight(), objects); }
			@Override public void caseABinopExp(ABinopExp exp) { result[0] = false; }
			@Override public void caseACommaExp(ACommaExp exp) { result[0] = mayPointTo(exp.getSecondExp(), objects); }
			@Override public void caseAConditionalExp(AConditionalExp exp) { result[0] = mayPointTo(exp.getTrueExp(), objects)
																					  || mayPointTo(exp.getFalseExp(), objects); }
			@Override public void caseAConstExp(AConstExp exp) { result[0] = false; }
			@Override public void caseADynamicPropertyExp(ADynamicPropertyExp exp) { result[0] = true; }
			@Override public void caseAEmptyExp(AEmptyExp exp) { result[0] = false; }
			@Override public void caseAFunctionExp(AFunctionExp exp) { result[0] = false; }
			@Override public void caseAInvokeExp(AInvokeExp exp) { result[0] = true; }
			@Override public void caseANameExp(ANameExp exp) { /* TODO: fix this */ result[0] = true; }
			@Override public void caseANewExp(ANewExp exp) { result[0] = false; }
			@Override public void caseAObjectLiteralExp(AObjectLiteralExp exp) { result[0] = false; }
			@Override public void caseAParenthesisExp(AParenthesisExp exp) { result[0] = mayPointTo(exp.getExp(), objects); }
			@Override public void caseAPostfixUnopExp(APostfixUnopExp exp) { result[0] = false; }
			@Override public void caseAPrefixUnopExp(APrefixUnopExp exp) { result[0] = false; }
			@Override public void caseAPropertyExp(APropertyExp exp) {
				PropertyExpAccess nd = new PropertyExpAccess(exp);
				String name = Literals.getName(exp);
				for(ObjectValue base : nd.getBase(input, name)) {
					if(CollectionUtil.intersects(input.getPropertyValue(base, name), objects)) {
						result[0] = true;
						return;
					}
				}
				result[0] = false;
			}
			@Override public void caseARegexpExp(ARegexpExp exp) { result[0] = false; }
			@Override public void caseAThisExp(AThisExp exp) { /* TODO: fix this */ result[0] = true; }
		});
		return result[0];
	}
}
