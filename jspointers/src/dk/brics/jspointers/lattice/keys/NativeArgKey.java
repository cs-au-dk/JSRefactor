package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * Key to the <i>n</i>th argument to a native function.
 * 
 * @author Asger
 */
public class NativeArgKey extends Key {
    private NativeFunctionValue function;
    private Context context;
    private int index;

    /**
     * Key to the <i>n</i>th argument to a native function.
     * @param function a native function
     * @param index index of the argument, starting at zero
     * @param calling context
     */
    public NativeArgKey(NativeFunctionValue function, Context context, int index) {
        this.function = function;
        this.context = context;
        this.index = index;
    }

    @Override
    public Key makeContextInsensitive() {
        return new NativeArgKey(function, NullContext.Instance, index);
    }

    public Context getContext() {
        return context;
    }
    public NativeFunctionValue getFunction() {
        return function;
    }
    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result
        + ((function == null) ? 0 : function.hashCode());
        result = prime * result + index;
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
        NativeArgKey other = (NativeArgKey) obj;
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
        if (index != other.index) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseNativeArg(this);
    }
}
