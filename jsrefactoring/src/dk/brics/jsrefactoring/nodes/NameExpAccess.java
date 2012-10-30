package dk.brics.jsrefactoring.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class NameExpAccess extends AccessVariable {
    
    private ANameExp exp;

    public NameExpAccess(ANameExp exp) {
        this.exp = exp;
    }
    
    public ANameExp getExp() {
        return exp;
    }
    
    @Override
    public ANameExp getNode() {
        return exp;
    }

    @Override
    public String getName() {
        return Literals.parseIdentifier(exp.getName().getText());
    }

    @Override
    public void replaceName(String newName) {
        AstUtil.replaceNode(exp.getName(), new TId(newName));
    }

    @Override
    public boolean isPrototypeSensitive() {
        return true;
    }
    
    @Override
    public boolean isLValue() {
    	return AstUtil.isLValue(getNode());
    }
    
    @Override
    public boolean isRValue() {
    	return AstUtil.isRValue(getNode());
    }

    @Override
    public <Q, A> A apply(AccessWithNameVisitor<Q, A> v, Q arg) {
        return v.caseNameExp(this, arg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
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
        NameExpAccess other = (NameExpAccess) obj;
        if (exp == null) {
            if (other.exp != null)
                return false;
        } else if (!exp.equals(other.exp))
            return false;
        return true;
    }
    
    @Override
    public List<ScopeHost> getSearchedScopes(Master input, String name) {
        return input.getSearchedScopes(exp, name);
    }


	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		List<ScopeHost> searchedScopes = getSearchedScopes(input, name);
		return Collections.singleton(searchedScopes.get(searchedScopes.size() - 1));
	}
    
    
}
