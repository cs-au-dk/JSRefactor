package dk.brics.jsrefactoring.nodes;

import java.util.List;
import java.util.Set;

import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsutil.CollectionUtil;

public abstract class AccessVariable extends AccessWithName {
    /**
     * Returns the list of scopes searched for the variable of the given
     * name. The last scope in the list will either be the global scope,
     * or a scope that declares a variable with this name.
     */
    public abstract List<ScopeHost> getSearchedScopes(Master input, String name);
    
    public abstract Set<ScopeHost> getAffectedScopes(Master input, String name);
    
    public final List<ScopeHost> getSearchedScopes(Master input) {
        return getSearchedScopes(input, getName());
    }
    
    @Override
    public boolean mayAlias(final Master input, final Access acc) {
    	final Set<ScopeHost> scopes = this.getAffectedScopes(input, getName());
    	return acc.apply(new AbstractAccessVisitor<Void, Boolean>() {
    		@Override
    		public Boolean defaultAccessVariable(AccessVariable node, Void arg) {
    			return getName().equals(node.getName()) &&
    				   CollectionUtil.intersects(scopes, node.getAffectedScopes(input, node.getName()))
    				|| super.defaultAccessVariable(node, arg);
    		}
    		
    		@Override
    		public Boolean defaultAccess(Access node, Void arg) {
    			return AccessVariable.super.mayAlias(input, acc);
    		}
    		
		}, null);
    }
}
