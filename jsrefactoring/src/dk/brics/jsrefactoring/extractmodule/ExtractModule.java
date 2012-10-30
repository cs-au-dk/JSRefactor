package dk.brics.jsrefactoring.extractmodule;

import static dk.brics.jsrefactoring.NodeFactory.createAssignExp;
import static dk.brics.jsrefactoring.NodeFactory.createBlock;
import static dk.brics.jsrefactoring.NodeFactory.createExpStmt;
import static dk.brics.jsrefactoring.NodeFactory.createFunctionExp;
import static dk.brics.jsrefactoring.NodeFactory.createInvokeExp;
import static dk.brics.jsrefactoring.NodeFactory.createNameExp;
import static dk.brics.jsrefactoring.NodeFactory.createNullExp;
import static dk.brics.jsrefactoring.NodeFactory.createObjectLiteral;
import static dk.brics.jsrefactoring.NodeFactory.createParenExp;
import static dk.brics.jsrefactoring.NodeFactory.createPropertyExp;
import static dk.brics.jsrefactoring.NodeFactory.createReturnStmt;
import static dk.brics.jsrefactoring.NodeFactory.createVarDeclStmt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AReturnStmt;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.AVarForInLvalue;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.AccessFinder;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFactory;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.PrettyPrinter;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.changes.InsertExpIntoArglist;
import dk.brics.jsrefactoring.changes.InsertStmtIntoBlock;
import dk.brics.jsrefactoring.changes.InsertStmtIntoScript;
import dk.brics.jsrefactoring.changes.RemoveStmtFromBlock;
import dk.brics.jsrefactoring.changes.RemoveVarDecl;
import dk.brics.jsrefactoring.changes.ReplaceExp;
import dk.brics.jsrefactoring.changes.SplitVarDeclStmt;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.NameExpAccess;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsrefactoring.nodes.VarDeclAccess;
import dk.brics.jsutil.Pair;

public class ExtractModule extends Refactoring {
	private final String moduleName;
	private Start script;
	private int start, end;
	
	// pre-code are all the statements that may be executed before or during module definition,
	// pre-callees all the functions that may be called by pre-code
	private Set<PStmt> preCode = new HashSet<PStmt>();
	private Set<IFunction> preCallees;

	public ExtractModule(Master input, String moduleName, Start script, int start, int end) {
		super(input);
		this.moduleName = moduleName;
		this.script = script;
		this.start = start;
		this.end = end;
		computePreCode();
	}
	
	private void computePreCode() {
		// collect all code in source files before our script
		for(dk.brics.jsrefactoring.InputFile f : input.getUserFiles()) {
			if(f.getAst() == script)
				break;
			for(PStmt stmt : f.getAst().getBody().getBlock().getStatements())
				preCode.add(stmt);
		}
		
		// all code within the same script
		for(int i=0;i<=end;++i)
			preCode.add(script.getBody().getBlock().getStatements().get(i));
		
		// now compute transitive callees
		preCallees = new CalleeAnalysis(input).getTransitiveCallees(preCode);
	}

	@Override
	public List<Change> getChanges() {
		List<Change> changes = new LinkedList<Change>();
		
		if(!AstUtil.isName(moduleName)) {
			log.fatal(moduleName + " is not a valid name for a module.");
			return changes;
		}

		final Set<String> names = new LinkedHashSet<String>(), haveDecl = new HashSet<String>();
		findDeclarations(names, haveDecl);
		Set<String> needDecl = new LinkedHashSet<String>(names);
		
		NodeFinder finder = new NodeFinder(input, APropertyExp.class, ADynamicPropertyExp.class, ANormalObjectLiteralProperty.class,
												  ABinopExp.class, ANameExp.class, AVarDecl.class, IFunction.class, ACatchClause.class);
		
		// find existing accesses to the module variable
		for(AccessWithName acc : AccessFinder.getNamedAccesses(finder, moduleName))
			if(acc.getBase(input, moduleName).contains(GlobalObjectValue.Instance))
				log.error(acc.getNode(), "Access %s may be conflicting with new namespace.", acc.getNode());
		checkReflectiveNameCapture(Collections.<ObjectValue>singleton(GlobalObjectValue.Instance), moduleName, finder);
		
		// TODO: check for conflicts with intrinsics
		
		// determine immutability and locality
		// a global property is immutable if it is only changed in initialization code;
		// it is local if it is never accessed outside the module
		Set<String> immutable = new HashSet<String>(names),
			        local = new HashSet<String>(names);
		for(String name : names) {
			for(AccessWithName acc : AccessFinder.getNamedAccesses(finder, name)) {
				if(!acc.getBase(input, name).contains(GlobalObjectValue.Instance))
					continue;
				if(acc instanceof VarDeclAccess && !((VarDeclAccess)acc).hasInit())
					continue;
				if(!isModuleCode(acc.getNode()))
					local.remove(name);
				if(acc.isLValue() && !isInitializationCode(acc.getNode()))
					immutable.remove(name);
			}
		}
		
		// check that accesses to the moved variables can be refactored
		Collection<AccessWithName> toQualify = new LinkedList<AccessWithName>();
		Collection<APropertyExp> toStrip = new LinkedList<APropertyExp>();
		for(String name: names) {
			checkReflectiveNameCapture(Collections.<ObjectValue>singleton(GlobalObjectValue.Instance), name, finder);
			for(AccessWithName acc : AccessFinder.getNamedAccesses(finder, name)) {
				Node nd = acc.getNode();
				// if it cannot possible refer to a global variable, we don't care
				if(!acc.getBase(input, name).contains(GlobalObjectValue.Instance))
					continue;
				
				// check that this access definitely refers to one of the moved globals
				if(!Collections.singleton(GlobalObjectValue.Instance).containsAll(acc.getBase(input, name)))
					log.error(nd, "Access %s may not refer to a global variable.", nd);
				
				if(isModuleCode(nd) && (isInitializationCode(nd) || immutable.contains(name) || local.contains(name)) && 
									   (nd instanceof APropertyExp || nd instanceof ANameExp || nd instanceof AVarDecl)) {
					if(nd instanceof APropertyExp) {
						APropertyExp exp = (APropertyExp)nd;
						if(mayHaveSideEffects(exp.getBase()))
							log.warn(exp, "Qualifier of access %s will be removed, which may suppress side effects.", exp);
						for(Scope s=input.getScope(exp);!s.isGlobal();s=s.getParentScope())
							if(s instanceof WithScope || s.getDeclaredVariables().contains(name))
								log.error(exp, "Access %s may become captured.", exp);
						toStrip.add(exp);
					}
				} else if(isClientCode(nd)) {
					if(!acc.getBase(input, moduleName).isEmpty())
						log.error(nd, "Module qualification for access %s may become captured.", nd);
					toQualify.add(acc);
				} else if(nd instanceof AVarDecl && !((VarDeclAccess)acc).hasInit()) {
					// these will simply be deleted; TODO: introduce extra collection, they don't belong in toQualify
					toQualify.add(acc);
				} else {
					log.error(nd, "Cannot classify acccess %s of type %s.", nd, acc.getClass());
				}
			}
		}
		
		checkEval(input);

		// create changes
		
		// insert module declaration
		List<Pair<String, PExp>> inits = new LinkedList<Pair<String,PExp>>();
		for(String name : names)
			if(!local.contains(name))
				inits.add(Pair.make(name, (PExp)createNameExp(name)));
		AReturnStmt ret = createReturnStmt(createObjectLiteral(inits));
		ABlock block;
		int pos;
		needDecl.removeAll(haveDecl);
		if(needDecl.isEmpty()) {
			block = createBlock(ret);
			pos = 0;
		} else {
			block = createBlock(createVarDeclStmt(needDecl.toArray(new String[0])), ret);
			pos = 1;
		}
		AFunctionExp nsfun = createFunctionExp(Collections.<String>emptyList(), block);
		AVarDeclStmt nsdecl = createVarDeclStmt(moduleName, createInvokeExp(createParenExp(nsfun), Collections.<PExp>emptyList()));
		PrettyPrinter.pp(nsdecl);
		changes.add(new InsertStmtIntoScript(script, start, nsdecl));
		
		// move statements to namespace
		for(int i=end;i>=start;--i) {
			changes.add(new RemoveStmtFromBlock(script.getBody().getBlock(), i+1));
			changes.add(new InsertStmtIntoBlock(nsfun.getBody().getBlock(), pos, script.getBody().getBlock().getStatements().get(i)));
		}
		
		// refactor references
		for(APropertyExp exp : toStrip)
			changes.add(new ReplaceExp(exp, createNameExp(Literals.getName(exp))));
		for(AccessWithName acc : toQualify) {
			APropertyExp qualifiedAcc = createPropertyExp(createNameExp(moduleName), acc.getName());
			if(acc instanceof NameExpAccess) {
				ANameExp exp = (ANameExp)acc.getNode();
				AInvokeExp inv = Refactoring.getInvocationParent(exp);
				if(inv == null || !calleeMayUseThis(inv)) {
					changes.add(new ReplaceExp(exp, qualifiedAcc));
				} else {
					if(!input.getWithScopeReceivers(exp).isEmpty())
						log.error(inv, "Cannot refactor this call.");
					changes.add(new ReplaceExp(inv.getFunctionExp(), createPropertyExp(qualifiedAcc, "call")));
					changes.add(new InsertExpIntoArglist(inv.getLparen(), inv.getArguments(), inv.getRparen(), 0, createNullExp()));
				}
			} else if(acc instanceof PropertyExpAccess) {
				APropertyExp prop = (APropertyExp)acc.getNode();
				PExp base_copy = NodeFactory.clone(prop.getBase());
				APropertyExp qualifiedProp = createPropertyExp(createPropertyExp(base_copy, moduleName), acc.getName());
				AInvokeExp inv = Refactoring.getInvocationParent(prop);
				if(inv == null || !calleeMayUseThis(inv)) {
					changes.add(new ReplaceExp(prop, qualifiedProp));
				} else {
					if(mayHaveSideEffects(prop.getBase()))
						log.error(inv, "Cannot refactor this call.");
					changes.add(new ReplaceExp(inv.getFunctionExp(), createPropertyExp(qualifiedProp, "call")));
					changes.add(new InsertExpIntoArglist(inv.getLparen(), inv.getArguments(), inv.getRparen(), 0, NodeFactory.clone(prop.getBase())));
				}
			} else if(acc instanceof VarDeclAccess) {
				AVarDecl decl = (AVarDecl)acc.getNode();
				PExp init = decl.getInit();
				if(init == null) {
					if(decl.parent() instanceof AVarForInLvalue)
						log.error(decl, "Cannot remove this declaration.");
					else
						changes.add(new RemoveVarDecl(decl));
				} else {
					if(!(decl.parent() instanceof AVarDeclStmt)) {
						log.error(decl, "Cannot remove this declaration.");
					} else {
						AVarDeclStmt stmt = (AVarDeclStmt)decl.parent();
						ABlock decl_block = (ABlock)stmt.parent();
						int stmt_idx = decl_block.getStatements().indexOf(stmt);
						changes.add(new SplitVarDeclStmt(stmt, stmt.getDecls().indexOf(decl)));
						changes.add(new RemoveVarDecl(decl));
						// TODO: precondition check to ensure that this property expression will resolve as expected
						AExpStmt assgn = createExpStmt(createAssignExp(qualifiedAcc, init));
						PrettyPrinter.pp(assgn);
						changes.add(new InsertStmtIntoBlock(decl_block, stmt_idx, assgn));
					}
				}
			} else {
				log.error(acc.getNode(), "Cannot refactor access %s.", acc.getNode());
			}
		}
		
		return changes;
	}

	// determine the set of global variables declared or defined in the module code
	private void findDeclarations(final Set<String> names, final Set<String> haveDecl) {
		for(int i=start;i<=end;++i) {
			PStmt stmt = script.getBody().getBlock().getStatements().get(i);
			stmt.apply(new DepthFirstAdapter() {
				@Override public void caseAFunctionDeclStmt(AFunctionDeclStmt fun) {
					String name = Literals.getName(fun);
					names.add(name);
					haveDecl.add(name);
				}
				@Override public void caseAFunctionExp(AFunctionExp node) {}
				@Override public void inANameExp(ANameExp exp) {
					String name = Literals.getName(exp);
					AccessWithName acc = new NameExpAccess(exp);
					if(acc.isLValue() && acc.getBase(input, name).contains(GlobalObjectValue.Instance))
						names.add(name);
				}
				@Override public void inAVarDecl(AVarDecl decl) {
					String name = Literals.getName(decl);
					names.add(name);
					haveDecl.add(name);
				}
			});
		}
	}
	
	// determines whether the given node belongs to the module to be extracted
	private boolean isModuleCode(Node nd) {
		for(int i=start;i<=end;++i) {
			PStmt stmt = script.getBody().getBlock().getStatements().get(i);
			if(AstUtil.isAncestor(stmt, nd))
				return true;
		}
		return false;
	}
	
	// determines whether the given node can only be executed during module initialisation
	private boolean isInitializationCode(Node nd) {
		return isModuleCode(nd) && nd.parent().getAncestor(IFunction.class) == null;
	}
	
	// determines whether the given node can only be executed after module initialisation
	private boolean isClientCode(Node nd) {
		IFunction fun = nd.parent().getAncestor(IFunction.class);
		if(fun == null) {
			for(PStmt pre : preCode)
				if(AstUtil.isAncestor(pre, nd))
					return false;
			return true;
		} else {
			return !preCallees.contains(fun);
		}
	}
}
