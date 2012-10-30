package dk.brics.jspointers.lattice.keys;

import dk.brics.jscontrolflow.Function;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;

public class FunctionInstanceKey extends Key {
    private Function function;
    private Context context;

    public FunctionInstanceKey(Function function, Context context) {
        this.function = function;
        this.context = context;
    }

    public Function getFunction() {
        return function;
    }
    public Context getContext() {
        return context;
    }

    @Override
    public Key makeContextInsensitive() {
        return new FunctionInstanceKey(function, NullContext.Instance);
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseFunctionInstance(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result
        + ((function == null) ? 0 : function.hashCode());
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
        FunctionInstanceKey other = (FunctionInstanceKey) obj;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (function == null) {
            if (other.function != null) {
                return false;
            }
        } else if (!function.equals(other.function)) {
            return false;
        }
        return true;
    }


}
