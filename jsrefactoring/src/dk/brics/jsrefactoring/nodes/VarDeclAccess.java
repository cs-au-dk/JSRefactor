package dk.brics.jsrefactoring.nodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarForInLvalue;
import dk.brics.jsparser.node.PForInLvalue;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class VarDeclAccess extends AccessVariable {
    private AVarDecl decl;

    public VarDeclAccess(AVarDecl decl) {
        this.decl = decl;
    }
    
    @Override
    public AVarDecl getNode() {
        return decl;
    }
    public AVarDecl getDecl() {
        return decl;
    }
    
    public boolean hasInit() {
    	return decl.getInit() != null || decl.parent() instanceof PForInLvalue;
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
    	return hasInit() || getNode().parent() instanceof AVarForInLvalue;
    }
    @Override
    public boolean isRValue() {
    	return false;
    }

    @Override
    public <Q, A> A apply(AccessWithNameVisitor<Q, A> v, Q arg) {
        return v.caseVarDecl(this, arg);
    }
    @Override
    public List<ScopeHost> getSearchedScopes(Master input, String name) {
        return input.getSearchedScopes(decl, name);
    }

	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		Set<ScopeHost> result = new HashSet<ScopeHost>();
		Scope scope = input.getScope(decl).getAncestorScope(Function.class);
		result.add(ScopeHost.fromScope(scope));
		// if declaration has an initializer, include the scope whose variable is being assigned to
		if (decl.getInit() != null) {
			List<ScopeHost> searchedScopes = getSearchedScopes(input, name);
			result.add(searchedScopes.get(searchedScopes.size() - 1));
		}
		return result;
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
        VarDeclAccess other = (VarDeclAccess) obj;
        if (decl == null) {
            if (other.decl != null)
                return false;
        } else if (!decl.equals(other.decl))
            return false;
        return true;
    }
    
}
