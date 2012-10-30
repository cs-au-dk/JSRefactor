package dk.brics.jscontrolflow.analysis.reachdef;

import dk.brics.jscontrolflow.scope.Scope;

public class ProgramVariable extends Variable {
    private String name;
    private Scope scope;

    public ProgramVariable(String name, Scope scope) {
        assert scope != null : "Scope was null";
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }
    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProgramVariable other = (ProgramVariable) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
    	return name;
    }

}
