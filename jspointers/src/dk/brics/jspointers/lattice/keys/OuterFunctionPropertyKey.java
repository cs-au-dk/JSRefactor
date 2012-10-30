package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.UserFunctionValue;

/**
 * An inner function's pointer to the enclosing function instance that instantiated it.
 * Only the global scope has no outer function.
 * 
 * @author Asger
 */
public class OuterFunctionPropertyKey extends Key {
    private UserFunctionValue function;

    public OuterFunctionPropertyKey(UserFunctionValue function) {
        this.function = function;
    }

    public UserFunctionValue getFunction() {
        return function;
    }
    @Override
    public Key makeContextInsensitive() {
        return new OuterFunctionPropertyKey(function.makeContextInsensitive());
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseOuterFunctionProperty(this);
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
        OuterFunctionPropertyKey other = (OuterFunctionPropertyKey) obj;
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
