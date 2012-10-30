package dk.brics.jsrefactoring;

import java.util.HashSet;
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
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.renameprty.ConstantInExpNameNode;
import dk.brics.jsrefactoring.renameprty.PropertyExpNameNode;
import dk.brics.jsrefactoring.renameprty.PropertyInitializerNameNode;
import dk.brics.jsrefactoring.renameprty.PropertyNameNode;
import dk.brics.jsutil.MultiMap;
import dk.brics.jsutil.Pair;

/**
 * <p>
 * Utility class for computing alias-closed sets of objects and property accesses.
 * </p> 
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class AffectedNodeFinder {
	protected final Master input;
	protected final NodeFinder finder;
	protected final PropertyNameNode node;
	
	// caches the relation Base(-, node.getName())
    protected final MultiMap<PropertyNameNode,ObjectValue> prtyname2base = new MultiMap<PropertyNameNode, ObjectValue>();
    
    // caches the inverse relation of prtyname2base
    protected final MultiMap<ObjectValue,PropertyNameNode> base2prtyname = new MultiMap<ObjectValue,PropertyNameNode>();
    
    private MultiMap<AForInStmt,ObjectValue> forin2base = new MultiMap<AForInStmt, ObjectValue>();
    private MultiMap<ObjectValue,AForInStmt> base2forin = new MultiMap<ObjectValue, AForInStmt>();
    private MultiMap<ADynamicPropertyExp,ObjectValue> dynprty2base = new MultiMap<ADynamicPropertyExp, ObjectValue>();
    private MultiMap<ObjectValue,ADynamicPropertyExp> base2dynprty = new MultiMap<ObjectValue, ADynamicPropertyExp>();
    private MultiMap<ADynamicPropertyExp,AForInStmt> dynprty2forin = new MultiMap<ADynamicPropertyExp, AForInStmt>();
    private MultiMap<AForInStmt,ADynamicPropertyExp> forin2dynprty = new MultiMap<AForInStmt, ADynamicPropertyExp>();
    private Set<ADynamicPropertyExp> unsafeDynPrty = new HashSet<ADynamicPropertyExp>();
    private Set<AForInStmt> unsafeForin = new HashSet<AForInStmt>();
    
    protected Set<ObjectValue> affectedObjects = null;
    protected Set<PropertyNameNode> affectedNames = null;
    private Set<ADynamicPropertyExp> affectedDynPrtyExps;
    private Set<AForInStmt> affectedForInStmts;
    
    public AffectedNodeFinder(Master input, NodeFinder finder, PropertyNameNode node) {
    	this.input = input;
    	this.finder = finder;
    	this.node = node;
    }
    
    public AffectedNodeFinder(Master input, PropertyNameNode node) {
    	// use standard node finder that looks for all nodes that could be accesses
    	this(input, new NodeFinder(input, IPropertyAccessNode.class, ANameExp.class, ANormalObjectLiteralProperty.class, ABinopExp.class, AForInStmt.class), node);
    }
    
	protected void computeMaps() {
	    String propertyName = node.getName();
	    
		prtyname2base.clear();
		base2prtyname.clear();
    	// first, cache Base mapping and its inverse
        for (APropertyExp exp : finder.getAllNodesOfType(APropertyExp.class)) {
            String name = Literals.parseIdentifier(exp.getName().getText());
            if (name.equals(node.getName())) {
            	PropertyNameNode node = new PropertyExpNameNode(exp);
            	for (ObjectValue base : node.getBase(input, name)) {
                    prtyname2base.add(node, base);
                    base2prtyname.add(base, node);
            	}
            }
        }
        for (ANormalObjectLiteralProperty prty : finder.getAllNodesOfType(ANormalObjectLiteralProperty.class)) {
        	// property names in object literals
            String name = AstUtil.getPropertyName(prty.getName());
        	PropertyNameNode node = new PropertyInitializerNameNode(prty);
            if (name.equals(node.getName())) {
            	// note: getReceivers = getBase for this type of node
                for (ObjectValue obj : input.getInitializedObjects(prty)) {
                    prtyname2base.add(node, obj);
                    base2prtyname.add(obj, node);
                }
            }
        }
        for (ABinopExp exp : finder.getAllNodesOfType(ABinopExp.class)) {
        	if (exp.getOp().kindPBinop() != EBinop.IN)
        		continue;
        	PExp left = exp.getLeft();
        	if (!(left instanceof AConstExp))
        		continue; // non-constant node cannot be renamed
    		AConstExp leftc = (AConstExp)left;
    		if (!(leftc.getConst() instanceof AStringConst))
    			continue; // int and boolean are safe
    		AStringConst sc = (AStringConst)leftc.getConst();
    		String name = Literals.parseStringLiteral(sc.getStringLiteral().getText());
    		PropertyNameNode node = new ConstantInExpNameNode(sc, exp);
    		if (name.equals(node.getName())) {
	    		for (ObjectValue base : node.getBase(input, name)) {
	    			prtyname2base.add(node, base);
	    			base2prtyname.add(base, node);
	    		}
    		}
        }
        // prepare dynamic property expressions
        for (ADynamicPropertyExp exp : finder.getAllNodesOfType(ADynamicPropertyExp.class)) {
            Set<ObjectValue> receivers = input.getReceivers(exp);
            Set<ObjectValue> bases;
            if (AstUtil.isReadFrom(exp)) {
                bases = input.getAllPrototypes(receivers, true);
            } else {
                bases = receivers;
            }
            for (ObjectValue base : bases) {
                if (!input.isPropertyDefinitelyAbsent(base, propertyName)) {
                    dynprty2base.add(exp, base);
                    base2dynprty.add(base, exp);
                }
            }
        }
        // prepare for-in statements
        for (AForInStmt forin : finder.getAllNodesOfType(AForInStmt.class)) {
            Set<ObjectValue> bases = input.getAllPrototypes(input.getForInObjects(forin), true);
            for (ObjectValue base : bases) {
                if (!input.isPropertyDefinitelyAbsent(base, propertyName)) {
                    forin2base.add(forin, base);
                    base2forin.add(base, forin);
                }
            }
        }
        for (ANameExp name : finder.getAllNodesOfType(ANameExp.class)) {
            Pair<Set<AForInStmt>,Boolean> p = input.getForInHosts(name);
            Set<AForInStmt> forins = p.fst;
            boolean onlyForIn = p.snd;
            Node parent = name.parent();
            if (parent instanceof ADynamicPropertyExp && ((ADynamicPropertyExp)parent).getPropertyExp() == name) {
                ADynamicPropertyExp dynprty = (ADynamicPropertyExp)parent;
                // relate property exprs and for-in statements
                if (onlyForIn) {
                    for (AForInStmt forin : forins) {
                        dynprty2forin.add(dynprty, forin);
                        forin2dynprty.add(forin, dynprty);
                    }
                } else {
                    unsafeDynPrty.add(dynprty);
                }
            } else {
                // mark for-in statements unsafe
                unsafeForin.addAll(forins);
            }
        }
	}
	
	protected void computeAffected() {
		computeMaps();
        affectedNames = new HashSet<PropertyNameNode>();
        affectedObjects = new HashSet<ObjectValue>();
        affectedDynPrtyExps = new HashSet<ADynamicPropertyExp>();
        affectedForInStmts = new HashSet<AForInStmt>();
        
        Set<PropertyNameNode> namequeue = new HashSet<PropertyNameNode>();
        Set<ObjectValue> objqueue = new HashSet<ObjectValue>();
        Set<ADynamicPropertyExp> dynprtyqueue = new HashSet<ADynamicPropertyExp>();
        Set<AForInStmt> forinqueue = new HashSet<AForInStmt>();
        affectedNames.add(node);
        namequeue.add(node);
        
        while (!namequeue.isEmpty() || !objqueue.isEmpty() || !dynprtyqueue.isEmpty() || !forinqueue.isEmpty()) {
            for (PropertyNameNode exp : namequeue) {
                for (ObjectValue obj : prtyname2base.getView(exp)) {
                    if (affectedObjects.add(obj)) {
                        objqueue.add(obj);
                    }
                }
            }
            namequeue.clear();
            for (ADynamicPropertyExp exp : dynprtyqueue) {
                for (ObjectValue obj : dynprty2base.getView(exp)) {
                    if (affectedObjects.add(obj)) {
                        objqueue.add(obj);
                    }
                }
                for (AForInStmt forin : dynprty2forin.getView(exp)) {
                    if (affectedForInStmts.add(forin)) {
                        forinqueue.add(forin);
                    }
                }
            }
            dynprtyqueue.clear();
            for (AForInStmt forin : forinqueue) {
                for (ObjectValue obj : forin2base.getView(forin)) {
                    if (affectedObjects.add(obj)) {
                        objqueue.add(obj);
                    }
                }
                for (ADynamicPropertyExp exp : forin2dynprty.getView(forin)) {
                    if (affectedDynPrtyExps.add(exp)) {
                        dynprtyqueue.add(exp);
                    }
                }
            }
            forinqueue.clear();
            for (ObjectValue obj : objqueue) {
                for (PropertyNameNode exp : base2prtyname.getView(obj)) {
                    if (affectedNames.add(exp)) {
                        namequeue.add(exp);
                    }
                }
                for (ADynamicPropertyExp exp : base2dynprty.getView(obj)) {
                    if (affectedDynPrtyExps.add(exp)) {
                        dynprtyqueue.add(exp);
                    }
                }
                for (AForInStmt forin : base2forin.getView(obj)) {
                    if (affectedForInStmts.add(forin)) {
                        forinqueue.add(forin);
                    }
                }
            }
            objqueue.clear();
        }
	}
	
	public Set<ObjectValue> getAffectedObjects() {
		if(affectedObjects == null)
			computeAffected();
		return affectedObjects;
	}
	
	public Set<PropertyNameNode> getAffectedNames() {
		if(affectedNames == null)
			computeAffected();
		return affectedNames;
	}
	
	public Set<ADynamicPropertyExp> getAffectedDynPrtyExps() {
	    if (affectedDynPrtyExps == null)
	        computeAffected();
        return affectedDynPrtyExps;
    }
	public Set<AForInStmt> getAffectedForInStmts() {
	    if (affectedForInStmts == null)
	        computeAffected();
        return affectedForInStmts;
    }
}
