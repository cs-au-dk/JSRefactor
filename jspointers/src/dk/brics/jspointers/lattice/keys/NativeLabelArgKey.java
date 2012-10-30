package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

public class NativeLabelArgKey extends Key {
    private NativeFunctionValue function;
    private Context context;

    public NativeLabelArgKey(NativeFunctionValue function, Context context) {
        this.function = function;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    public NativeFunctionValue getFunction() {
        return function;
    }

    @Override
    public Key makeContextInsensitive() {
        return new NativeLabelArgKey(function, NullContext.Instance);
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseNativeLabelArg(this);
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
        NativeLabelArgKey other = (NativeLabelArgKey) obj;
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
