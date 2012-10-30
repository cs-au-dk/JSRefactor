package dk.brics.jsrefactoring.nodes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.node.ABreakStmt;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.AContinueStmt;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AGetObjectLiteralProperty;
import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.ALabelledStmt;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.ASetObjectLiteralProperty;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.ILeftHandSideNode;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.TGet;
import dk.brics.jsparser.node.TId;
import dk.brics.jsparser.node.TSet;
import dk.brics.jsparser.node.Token;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

/**
 * A node that depends on property names.
 */
public abstract class Access {
	public abstract boolean equals(Object obj);
	public abstract int hashCode();
	
	public abstract <Q,A> A apply(AccessVisitor<Q, A> v, Q arg);
	
	public abstract Node getNode();
	
	public abstract boolean isPrototypeSensitive();
	public abstract boolean isRValue();
	public abstract boolean isLValue();
	public abstract boolean mayAlias(Master input, Access acc);
	
	public boolean mayAlias(Master input, Collection<? extends Access> accs) {
		for(Access acc : accs)
			if(this.mayAlias(input, acc))
				return true;
		return false;
	}
	
	/**
	 * Returns receivers, not including prototypes.
	 * For variable accesses, this includes receivers accessed through <tt>with</tt> statements, and the global
	 * object if a global variable is accessed.
	 */
	public final Set<ObjectValue> getDirectReceivers(final Master input) {
		return apply(new AccessVisitor<Void, Set<ObjectValue>>() {
			@Override
			public Set<ObjectValue> casePropertyInitializer(PropertyInitializerAccess node, Void arg) {
				return input.getInitializedObjects(node.getNode());
			}
			@Override
			public Set<ObjectValue> casePropertyExp(PropertyExpAccess node, Void arg) {
				return input.getAccessedObjects(node.getNode());
			}
			@Override
			public Set<ObjectValue> caseConstantInExp(ConstInExpAccess node, Void arg) {
				return input.getInExpObjectArgs(node.getExp());
			}
			@Override
			public Set<ObjectValue> caseDynamicProperty(DynamicPropertyExpAccess node, Void arg) {
				return input.getAccessedObjects(node.getExp());
			}
			@Override
			public Set<ObjectValue> caseNameExp(NameExpAccess node, Void arg) {
			    Set<ObjectValue> receivers = input.getWithScopeReceivers(node.getExp());
			    if (node.getSearchedScopes(input).contains(ScopeHost.GLOBAL)) {
			        return addGlobal(receivers);
			    } else {
			        return receivers;
			    }
			}
			@Override
			public Set<ObjectValue> caseVarDecl(VarDeclAccess node, Void arg) {
			    if (node.getSearchedScopes(input).contains(ScopeHost.GLOBAL)) {
                    return Collections.<ObjectValue>singleton(GlobalObjectValue.Instance);
                } else {
                    return Collections.emptySet();
                }
			}
			@Override
			public Set<ObjectValue> caseParameter(ParameterAccess node, Void arg) {
			    return Collections.emptySet();
			}
			@Override
			public Set<ObjectValue> caseCatch(CatchAccess node, Void arg) {
				return Collections.emptySet();
			}
			@Override
			public Set<ObjectValue> caseFunctionDecl(FunctionDeclAccess node, Void arg) {
				return Collections.emptySet();
			}
			@Override
			public Set<ObjectValue> caseFunctionExp(FunctionExpAccess node, Void arg) {
			  return Collections.emptySet();
			}
			private Set<ObjectValue> addGlobal(Set<? extends ObjectValue> set) {
			    Set<ObjectValue> result = new HashSet<ObjectValue>(set);
			    result.add(GlobalObjectValue.Instance);
                return result;
			}
		}, null);
	}
	
	/**
	 * Returns receivers, and prototypes if name node is prototype sensitive.
	 */
	public final Set<ObjectValue> getReceivers(final Master input) {
		Set<ObjectValue> set = getDirectReceivers(input);
		if (isPrototypeSensitive()) {
			return input.getAllPrototypes(set, true);
		} else {
			return set;
		}
	}
	
	/**
	 * Returns every receiver that might have a property with the given name.
	 * (Includes prototypes if node is prototype sensitive)
	 */
	public final Set<ObjectValue> getBase(Master input, String name) {
		Set<ObjectValue> rcv = this.getReceivers(input);
		Set<ObjectValue> result = new HashSet<ObjectValue>();
		for (ObjectValue o : rcv) {
			if (!input.isPropertyDefinitelyAbsent(o, name))
				result.add(o);
		}
		return result;
	}
	public abstract Set<ObjectValue> getBase(Master input);

	/**
	 * Constructs a {@link Access} from the given {@link IPropertyAccessNode}.
	 */
	public static Access of(ILeftHandSideNode node) {
		if(node instanceof ANameExp)
			return new NameExpAccess((ANameExp)node);
		if(node instanceof APropertyExp)
			return new PropertyExpAccess((APropertyExp)node);
		return new DynamicPropertyExpAccess((ADynamicPropertyExp)node);
	}
	
	/**
	 * Returns the named access responsible for the specified identifier;
	 * or <tt>null</tt> if it is not an identifier, or no access is responsible for it.
	 * <p/>
	 * Throws {@link IllegalArgumentException} if the token is an identifier without a parent node
	 * (ie. the identifier has been disconnected from the AST).
	 * 
	 * @param id a token
	 * @return <tt>null</tt>, or a named access
	 */
	public static AccessWithName fromToken(final Token id) {
	  if (!(id instanceof TId || id instanceof TGet || id instanceof TSet))
	    return null;
	  if (id.parent() == null)
	    throw new IllegalArgumentException("Orphaned identifier");
	  return id.parent().apply(new AnswerAdapter<AccessWithName>() {
	    @Override
	    public AccessWithName caseAVarDecl(AVarDecl node) {
	      if (node.getName() == id) {
	        return new VarDeclAccess(node);
	      } else {
	        return null;
	      }
	    }
	    @Override
	    public AccessWithName caseACatchClause(ACatchClause node) {
	      if (node.getName() == id) {
	        return new CatchAccess(node);
	      } else {
	        return null;
	      }
	    }
	    private AccessWithName examineParameters(IFunction node) {
        for (int i=0; i<node.getParameters().size(); i++) {
          if (node.getParameters().get(i) == id) {
            return new ParameterAccess(node, i);
          }
        }
        return null;
	    }
	    @Override
	    public AccessWithName caseAFunctionDeclStmt(AFunctionDeclStmt node) {
	      if (node.getName() == id) {
	        return new FunctionDeclAccess(node);
	      } else {
	        return examineParameters(node);
	      }
	    }
	    @Override
	    public AccessWithName caseAFunctionExp(AFunctionExp node) {
	      if (node.getName() == id) {
	        return new FunctionExpAccess(node);
	      } else {
          return examineParameters(node);
        }
	    }
	    @Override
	    public AccessWithName caseANameExp(ANameExp node) {
	      if (node.getName() == id) {
	        return new NameExpAccess(node);
	      } else {
	        return null;
	      }
	    }
	    @Override
	    public AccessWithName caseAPropertyExp(APropertyExp node) {
	      if (node.getName() == id) {
	        return new PropertyExpAccess(node);
	      } else {
	        return null;
	      }
	    }
	    @Override
	    public AccessWithName caseAIdentifierPropertyName(AIdentifierPropertyName node) {
	      if (node.getName() == id) {
	        ANormalObjectLiteralProperty prtyInit = (ANormalObjectLiteralProperty)node.parent();
	        return new PropertyInitializerAccess(prtyInit);
	      } else {
	        return null;
	      }
	    }
	    @Override
	    public AccessWithName caseABreakStmt(ABreakStmt node) {
	      return null; // labels are not accesses
	    }
	    @Override
	    public AccessWithName caseAContinueStmt(AContinueStmt node) {
	      return null; // labels are not accesses
	    }
      @Override
	    public AccessWithName caseALabelledStmt(ALabelledStmt node) {
	      return null; // labels are not accesses
	    }
      @Override
      public AccessWithName caseAGetObjectLiteralProperty(AGetObjectLiteralProperty node) {
        return null; // get used as keyword
      }
      @Override
      public AccessWithName caseASetObjectLiteralProperty(ASetObjectLiteralProperty node) {
        return null; // set used as keyword
      }
      @Override
      public AccessWithName defaultNode(Node node) {
        throw new IllegalArgumentException("Unexpected parent node of identifier: " + node.getClass().getName());
      }
	  });
	}
	
	@Override
	public String toString() {
		return getNode().toString();
	}
}
