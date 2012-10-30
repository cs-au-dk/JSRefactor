package dk.brics.jsrefactoring.hosts;

import java.util.ArrayList;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;

/**
 * A lexical environment created by a function or catch clause (not by with statements).
 */
public class ScopeHost extends Host {
    private Scope scope;
    
    public static final ScopeHost GLOBAL = new ScopeHost(null);

    public ScopeHost(Scope scope) {
        this.scope = scope;
    }
    
    public Scope getScope() {
        return scope;
    }

    public boolean isGlobal() {
        return scope == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
        ScopeHost other = (ScopeHost) obj;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }
    
    public static ScopeHost fromScope(Scope scope) {
    	if (scope == null || scope.getParentScope() == null) {
    		return GLOBAL;
    	} else {
    		return new ScopeHost(scope);
    	}
    }

	public static List<ScopeHost> getScopeChain(String name, Scope scope) {
		List<ScopeHost> list = new ArrayList<ScopeHost>();
	    while (scope != null) {
	        if (!(scope instanceof WithScope)) {
	            list.add(new ScopeHost(scope));
	        }
	        if (scope.getDeclaredVariables().contains(name))
	            break;
	        scope = scope.getParentScope();
	    }
	    if (scope == null) {
	        list.add(GLOBAL);
	    }
	    return list;
	}

}
