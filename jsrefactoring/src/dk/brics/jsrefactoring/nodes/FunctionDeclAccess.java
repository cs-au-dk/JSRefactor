package dk.brics.jsrefactoring.nodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class FunctionDeclAccess extends AccessVariable {
    private AFunctionDeclStmt decl;

    public FunctionDeclAccess(AFunctionDeclStmt decl) {
        if (decl.getName() == null) {
          throw new IllegalArgumentException("Function does not have a name");
        }
        this.decl = decl;
    }
    
    @Override
    public AFunctionDeclStmt getNode() {
        return decl;
    }
    public AFunctionDeclStmt getDecl() {
        return decl;
    }
    
    @Override
    public String getName() {
        return Literals.getName(decl);
    }


    @Override
    public void replaceName(String newName) {
        AstUtil.replaceNode(decl.getName(), new TId(newName));
    }

    @Override
    public boolean isPrototypeSensitive() {
        return false;
    }
    @Override
    public boolean isLValue() {
    	return true; // TODO: Not sure
    }
    @Override
    public boolean isRValue() {
    	return false;
    }

    @Override
    public <Q, A> A apply(AccessWithNameVisitor<Q, A> v, Q arg) {
        return v.caseFunctionDecl(this, arg);
    }
    @Override
    public List<ScopeHost> getSearchedScopes(Master input, String name) {
        return Collections.singletonList(ScopeHost.fromScope(input.getScope(decl.getAncestor(ABody.class))));
    }

	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		return new HashSet<ScopeHost>(getSearchedScopes(input, name));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decl == null) ? 0 : decl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionDeclAccess other = (FunctionDeclAccess) obj;
		if (decl == null) {
			if (other.decl != null)
				return false;
		} else if (!decl.equals(other.decl))
			return false;
		return true;
	}
    
}
