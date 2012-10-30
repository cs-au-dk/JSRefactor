package dk.brics.jsrefactoring.nodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class FunctionExpAccess extends AccessVariable {
    private AFunctionExp exp;

    public FunctionExpAccess(AFunctionExp exp) {
        if (exp.getName() == null) {
          throw new IllegalArgumentException("Function does not have a name");
        }
        this.exp = exp;
    }
    
    @Override
    public AFunctionExp getNode() {
        return exp;
    }
    public AFunctionExp getDecl() {
        return exp;
    }
    
    @Override
    public String getName() {
        return Literals.getName(exp);
    }


    @Override
    public void replaceName(String newName) {
        AstUtil.replaceNode(exp.getName(), new TId(newName));
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
        return v.caseFunctionExp(this, arg);
    }
    @Override
    public List<ScopeHost> getSearchedScopes(Master input, String name) {
      return Collections.singletonList(ScopeHost.fromScope(input.getScope(exp.getBody())));
    }

	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		return new HashSet<ScopeHost>(getSearchedScopes(input, name));
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 1;
		result = prime * result + ((exp == null) ? 0 : exp.hashCode());
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
		FunctionExpAccess other = (FunctionExpAccess) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		return true;
	}
    
}
