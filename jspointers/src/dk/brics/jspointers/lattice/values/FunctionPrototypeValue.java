package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;

public class FunctionPrototypeValue extends ObjectValue {
    private FunctionValue function;

    public FunctionPrototypeValue(FunctionValue function) {
        this.function = function;
    }

    public FunctionValue getFunction() {
        return function;
    }

    @Override
    public Context getContext() {
        return function.getContext();
    }
    @Override
    public FunctionPrototypeValue replaceContext(Context newContext) {
        FunctionValue fval = function.replaceContext(newContext);
        if (fval == function) {
            return this;
        } else {
            return new FunctionPrototypeValue(fval);
        }
    }

    @Override
    public FunctionPrototypeValue makeContextInsensitive() {
        return new FunctionPrototypeValue(function.makeContextInsensitive());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        FunctionPrototypeValue other = (FunctionPrototypeValue) obj;
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
