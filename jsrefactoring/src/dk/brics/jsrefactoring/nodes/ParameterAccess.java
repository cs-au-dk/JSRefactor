package dk.brics.jsrefactoring.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class ParameterAccess extends AccessVariable {
    private IFunction function;
    private int index;
    
    public ParameterAccess(IFunction function, int index) {
        this.function = function;
        this.index = index;
    }
    
    @Override
    public List<ScopeHost> getSearchedScopes(Master input, String name) {
        return ScopeHost.getScopeChain(name, input.getFunctionScope(function));
    }

	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		List<ScopeHost> searchedScopes = getSearchedScopes(input, name);
		return Collections.singleton(searchedScopes.get(searchedScopes.size() - 1));
	}

	@Override
    public Node getNode() {
        return function.getParameters().get(index);
    }

    @Override
    public String getName() {
        return Literals.parseIdentifier(function.getParameters().get(index).getText());
    }

    @Override
    public void replaceName(String newName) {
        AstUtil.replaceNode(function.getParameters().get(index), new TId(newName));
    }
    
    @Override
    public boolean isPrototypeSensitive() {
        return false;
    }
    @Override
    public boolean isLValue() {
    	return true;
    }
    @Override
    public boolean isRValue() {
    	return false;
    }

    @Override
    public <Q, A> A apply(AccessWithNameVisitor<Q, A> v, Q arg) {
        return v.caseParameter(this, arg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((function == null) ? 0 : function.hashCode());
        result = prime * result + index;
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
        ParameterAccess other = (ParameterAccess) obj;
        if (function == null) {
            if (other.function != null)
                return false;
        } else if (!function.equals(other.function))
            return false;
        if (index != other.index)
            return false;
        return true;
    }
    
}
