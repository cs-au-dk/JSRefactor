package dk.brics.jspointers.lattice.keys;

import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;

public final class VariableKey extends Key {
    private final String varname;
    private final Scope scope;
    private final Context context;

    public VariableKey(String varname, Scope scope, Context context) {
        assert varname != null : "varname cannot be null";
        assert scope.getParentScope() != null : "VariableKey cannot represent global variables";
        assert context != null : "Context was null";
        this.varname = varname;
        this.scope = scope;
        this.context = context;
    }

    @Override
    public Key makeContextInsensitive() {
        return new VariableKey(varname, scope, NullContext.Instance);
    }

    public Context getContext() {
        return context;
    }
    public String getVarname() {
        return varname;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((varname == null) ? 0 : varname.hashCode());
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
        VariableKey other = (VariableKey) obj;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        if (varname == null) {
            if (other.varname != null) {
                return false;
            }
        } else if (!varname.equals(other.varname)) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(KeyVisitor key) {
        key.caseVariableKey(this);
    }
}
