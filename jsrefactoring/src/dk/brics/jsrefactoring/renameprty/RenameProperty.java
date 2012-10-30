package dk.brics.jsrefactoring.renameprty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AStringConst;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.JSUtil;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.values.CoercedPrimitiveObjectValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.StringValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.AccessFinder;
import dk.brics.jsrefactoring.CommandLineUtil;
import dk.brics.jsrefactoring.Diagnostic;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.changes.Change;
import dk.brics.jsrefactoring.changes.RenamePropertyNameNode;
import dk.brics.jsrefactoring.family.FamilyClosure;
import dk.brics.jsrefactoring.hosts.Host;
import dk.brics.jsrefactoring.hosts.ObjectHost;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.nodes.Access;
import dk.brics.jsrefactoring.nodes.AccessVariable;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.AccessWithoutName;
import dk.brics.jsrefactoring.nodes.ConstInExpAccess;
import dk.brics.jsrefactoring.nodes.DynamicPropertyExpAccess;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsutil.CollectionUtil;
import dk.brics.jsutil.Pair;

public class RenameProperty extends Refactoring {
	private FamilyClosure<Host, Access> affected;
	private Set<ObjectValue> affectedObjects;
    private Set<ScopeHost> affectedScopes = getAffectedScopes();
	private String newName;
	
	private ForInDynamicAccesses getForInDynamicAccesses(Master input, NodeFinder finder) {
	    ForInDynamicAccesses result = new ForInDynamicAccesses();
	    for (ADynamicPropertyExp exp : finder.getAllNodesOfType(ADynamicPropertyExp.class)) {
	        Pair<Set<AForInStmt>,Boolean> p = input.getForInStatementsThatMayDefinePropertyName(exp);
	        if (p.snd) {
	            result.getSafeExps().add(exp);
	        }
	        for (AForInStmt forin : p.fst) {
	            Set<ObjectValue> base = input.getAllPrototypes(input.getForInObjects(forin), true);
	            result.getExp2forinObjs().addAll(exp, base);
	        }
	    }
	    for (AForInStmt forin : finder.getAllNodesOfType(AForInStmt.class)) {
	        Pair<Set<ADynamicPropertyExp>,Boolean> p = input.getForInNameUses(forin);
	        if (p.snd) {
	            result.getSafeForIns().add(forin);
	        }
	    }
	    return result;
	}
	
	public RenameProperty(Master input, AccessWithName expression, String newName) {
    	super(input);
    	this.newName = newName;
    	
    	if (!AstUtil.isName(newName)) {
    		log.fatal("New name is invalid");
    		return;
    	}
    	
    	// if the expression is unreachable, don't bother
//        if (input.getAccessedObjects(expression).isEmpty())
//        	log.warn(expression.getName(), "Expression is unreachable");
//    	if (expression.getDirectReceivers(input).isEmpty()) {
//    	    log.warn(expression.getName(), "Expression is unreachable");
//    	}
        
        String oldName = expression.getName();
        
        NodeFinder finder = new NodeFinder(input, IPropertyAccessNode.class, ANameExp.class, 
        										  ANormalObjectLiteralProperty.class, ABinopExp.class);
        List<AccessWithName> nodesWithNewName = AccessFinder.getNamedAccesses(finder, newName);
        Set<ABinopExp> dynamicInExps = new HashSet<ABinopExp>();
        
        // find nodes with new name and dynamic in expressions
        for (ABinopExp exp : finder.getAllNodesOfType(ABinopExp.class)) {
        	if (exp.getOp().kindPBinop() != EBinop.IN)
        		continue;
        	PExp left = exp.getLeft();
        	if (!(left instanceof AConstExp)) {
        		dynamicInExps.add(exp);
        		continue; // non-constant node cannot be renamed
        	}
    		AConstExp leftc = (AConstExp)left;
    		if (!(leftc.getConst() instanceof AStringConst))
    			continue; // int and boolean are safe
    		AStringConst sc = (AStringConst)leftc.getConst();
    		String name = Literals.parseStringLiteral(sc.getStringLiteral().getText());
    		if(name.equals(newName))
    			nodesWithNewName.add(new ConstInExpAccess(sc, exp));
        }
        
        List<Access> propertySensitiveNodes = new ArrayList<Access>();
        propertySensitiveNodes.addAll(AccessFinder.getNamedAccesses(finder, oldName));
        ForInDynamicAccesses forinAccess = getForInDynamicAccesses(input, finder);
        for (ADynamicPropertyExp exp : forinAccess.getSafeExps()) {
            propertySensitiveNodes.add(new DynamicPropertyExpAccess(exp));
        }
        affected = FamilyClosure.compute(
                new RenamingFamily(input, oldName, forinAccess), 
                propertySensitiveNodes, 
                Collections.singleton(expression), 
                Collections.<Host>emptySet());
        affectedObjects = ObjectHost.unwrap(CollectionUtil.filter(affected.getAffectedObjects(), ObjectHost.class));
        affectedScopes = CollectionUtil.filter(affected.getAffectedObjects(), ScopeHost.class);
        
        Set<IFunction> violatingNatives = new HashSet<IFunction>();
        for (AccessWithoutName access : getAffectedDynamicAccesses()) {
        	if (input.isNativeCode(access.getNode()))
        		continue;
            boolean safe = false;
            if (access instanceof DynamicPropertyExpAccess) {
                DynamicPropertyExpAccess dyn = (DynamicPropertyExpAccess)access;
                if (input.isDefinitelyArrayLookup(dyn.getExp()) || forinAccess.getSafeExps().contains(dyn.getExp())) {
                    safe = true;
                }
            }
            if (safe)
                continue;
            log.warn(access.getNode(), "Access %s might be affected, but cannot be updated.", access.getNode());
        }
        for (ADynamicPropertyExp exp : finder.getAllNodesOfType(ADynamicPropertyExp.class)) {
            if (input.isDefinitelyArrayLookup(exp) || forinAccess.getSafeExps().contains(exp))
                continue;
            Set<ObjectValue> receivers = input.getReceivers(exp);
            if (CollectionUtil.intersects(receivers, affectedObjects))
            	if (input.isNativeCode(exp)) {
            		violatingNatives.add(exp.getAncestor(IFunction.class));
            	} else {
            		log.warn(exp, "Expression %s might be affected, but cannot be updated.", exp);
            	}
        }
        // check for for-in loop conflicts
        for (AForInStmt forin : finder.getAllNodesOfType(AForInStmt.class)) {
            if (forinAccess.getSafeForIns().contains(forin)) {
                continue;
            }
            Set<ObjectValue> receivers = input.getAllPrototypes(input.getForInObjects(forin), true);
            if (CollectionUtil.intersects(receivers, affectedObjects)) {
            	if (input.isNativeCode(forin)) {
            		violatingNatives.add(forin.getAncestor(IFunction.class));
            	} else {
            		log.warn(forin, "Property name produced by for-in loop may change");
            	}
            }
        }
        // check for dynamic 'in' expression conflicts
        for (ABinopExp exp : dynamicInExps) {
        	if (input.isNativeCode(exp))
        		continue;
        	Set<ObjectValue> objs = input.getInExpObjectArgs(exp);
        	if (CollectionUtil.intersects(objs, affectedObjects))
        		log.warn(exp, "The expression %s might be affected, but cannot be updated.", exp);
        }
        // check if new name clashes with existing properties
        for (AccessWithName node : this.getAffectedNames()) {
        	if (input.isNativeCode(node.getNode()))
        		continue;
        	Set<ObjectValue> base = node.getBase(input, newName);
        	if (!base.isEmpty()) {
        		log.error(node.getNode(), "Renaming expression %s may clash with existing property of name %s.", node.getNode(), newName);
        	}
        	if (node instanceof AccessVariable) {
        		// note: the check with getBase above took care of conflicting with-scopes and global object properties
                AccessVariable varAccess = (AccessVariable)node;
                for (ScopeHost scope : varAccess.getSearchedScopes(input, oldName)) {
                	if (scope.isGlobal()) {
                		continue;
                	}
                	if (scope.getScope().getDeclaredVariables().contains(newName)) {
                		log.error(node.getNode(), "Renaming expression %s will change resolution of variable %s.", node.getNode(), newName);
                	}
                }
        	}
        }
        // check if existing accesses with the new name are affected
        for (AccessWithName node : nodesWithNewName) {
        	if (input.isNativeCode(node.getNode()))
        		continue;
        	Set<ObjectValue> receivers = node.getReceivers(input);
        	if (CollectionUtil.intersects(receivers, affectedObjects)) {
        		log.error(node.getNode(), "Expression %s may clash with renamed property.", node.getNode());
        	}
        	else if (node instanceof AccessVariable) {
        	    AccessVariable varAccess = (AccessVariable)node;
				if (CollectionUtil.intersects(new HashSet<ScopeHost>(varAccess.getSearchedScopes(input)), affectedScopes)) {
					log.error(node.getNode(), "Expression %s may clash with renamed property.", node.getNode());
        	    }
        	}
        }
        // check for renaming native stuff in the harness files
        for (AccessWithName node : this.getAffectedNames()) {
        	if (input.isNativeCode(node.getNode()))
        		log.error("Native property %s cannot be renamed", node.getNode());
        }
        // check for renaming native stuff not in the harness files
        // XXX The difference between code in harness files and internal natives is quite analysis-specific - can it be abstracted??
        for (NativeFunctionValue nativ : JSUtil.NATIVE_FUNCTIONS) {
        	if (affectedObjects.contains(nativ.getFunctionPrototype()) && nativ.getNativeMembers().contains(oldName))
        		log.error("Native property %s.%s cannot be renamed.", nativ.getPrettyName(), oldName);
        }
        // check for renaming of toString, valueOf or constructor
        if (oldName.equals("toString") || oldName.equals("valueOf"))
        	log.warn("Renaming %s may affect implicit coercions.", oldName);
        if (newName.equals("toString") || newName.equals("valueOf"))
        	log.warn("Renaming to %s may affect implicit coersions", newName);
        if (oldName.equals("constructor") || newName.equals("constructor"))
        	log.warn("Renaming property from/to 'constructor' may affect instanceof.");
        if (CollectionUtil.containsInstanceOf(affectedObjects, FunctionValue.class) && (oldName.equals("prototype") || newName.equals("prototype"))) {
        	log.warn("Cannot rename 'prototype' property.");
        }
        boolean coercedStringsAreAffected = affectedObjects.contains(new CoercedPrimitiveObjectValue(StringValue.Instance));
        boolean isLength = oldName.equals("length") || newName.equals("length");
		if (coercedStringsAreAffected && isLength) {
        	log.error("Cannot rename String.length property");
        }
        boolean arraysAreAffected = input.getAllPrototypes(affectedObjects, false).contains(new UserFunctionValue(input.getHarnessNativeFunction("Array"), MainContext.Instance).getFunctionPrototype());
		if (arraysAreAffected && isLength) {
        	log.error("Cannot rename Array.length property");
        }
        // check for calling a native function that performs dynamic property access
        violatingNatives.addAll(findNativeDynamicAccesses(affectedObjects));
        for (IFunction func : violatingNatives) {
        	log.warn("Renamed property may be dynamically accessed by native function %s", getNativeFunctionName(func));
        }
        checkEval(input);
        
        
        // TODO more conflict checks?
    }

	public Set<AccessWithName> getAffectedNames() {
		return CollectionUtil.filter(affected.getAffectedFamilies(), AccessWithName.class);
	}
    public Set<AccessWithoutName> getAffectedDynamicAccesses() {
        return CollectionUtil.filter(affected.getAffectedFamilies(), AccessWithoutName.class);
    }
	public Set<ObjectValue> getAffectedObjects() {
		return affectedObjects;
	}
	public Set<ScopeHost> getAffectedScopes() {
	    return affectedScopes;
	}
	
	@Override
	public List<Change> getChanges() {
		LinkedList<Change> changes = new LinkedList<Change>();
		if(affected == null)
			return changes;
		for (AccessWithName node : getAffectedNames())
			changes.add(new RenamePropertyNameNode(node, newName));
		return changes;
	}
	
    public static void main(String[] args) {
        String filename;
        if (args.length == 0 || !args[0].endsWith(".js")) {
            filename = "../jspointers/test/nano/prototype1.js";
        } else {
            filename = args[0];
        }
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println(filename + " does not exist");
            return;
        }
        boolean onlyWarnings = false;
        for (int i=1; i<args.length; i++) {
        	if (args[i].equals("-onlywarn")) {
        		onlyWarnings = true;
        	}
        }
        
        Master master = new Master(file);
        System.out.printf("Using file %s as test case\n", filename);
        int lineNr = CommandLineUtil.promptInt("Enter line number of a property expression to rename");
        List<APropertyExp> candidates = new ArrayList<APropertyExp>();
        for (APropertyExp exp : master.getAllNodesOfType(APropertyExp.class)) {
            if (exp.getName().getLine() == lineNr && exp.getRoot() == master.getUserFiles().get(0).getAst()) {
                candidates.add(exp);
            }
        }
        APropertyExp expToRename;
        if (candidates.size() == 0) {
            System.err.println("I can't find any property expressions on that line");
            return;
        } else if (candidates.size() == 1) {
            expToRename = candidates.get(0);
            System.out.printf("Renaming expression %s\n", AstUtil.toSourceString(expToRename));
        } else {
            for (int i=0; i<candidates.size(); i++) {
                System.out.printf("%d. %s\n", i+1, AstUtil.toSourceString(candidates.get(i)));
            }
            int num = CommandLineUtil.promptInt("Which of the above expressions would you like to rename?");
            expToRename = candidates.get(num-1);
        }
        String newName = CommandLineUtil.promptString("Enter a new name for the property");
        Refactoring rename = new RenameProperty(master, new PropertyExpAccess(expToRename), newName);
        rename.execute();
        for(Diagnostic warning : rename.getDiagnostics())
        	System.err.println("Warning at line " + warning.getStartLine() + ": " + warning.getMessage());
        System.out.println();
        if (!onlyWarnings) {
	        System.out.println("----------------------------------");
	        System.out.print(AstUtil.toSourceString(master.getUserFiles().get(0).getAst()));
        }
    }

}
