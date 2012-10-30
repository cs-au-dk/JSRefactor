package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * Maps to the least upper bound of all arguments to the given native.
 * 
 * @author Asger
 */
public class NativeDefaultArgsKey extends Key {
    private NativeFunctionValue function;
    private Context context;

    public NativeDefaultArgsKey(NativeFunctionValue function, Context context) {
        this.function = function;
        this.context = context;
    }

    public NativeFunctionValue getFunction() {
        return function;
    }
    public Context getContext() {
        return context;
    }
    @Override
    public Key makeContextInsensitive() {
        return new NativeDefaultArgsKey(function, NullContext.Instance);
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseNativeDefaultArgs(this);
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
        NativeDefaultArgsKey other = (NativeDefaultArgsKey) obj;
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
