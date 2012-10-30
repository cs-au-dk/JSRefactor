package dk.brics.jspointers.lattice.values;

import dk.brics.jscontrolflow.Function;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;

/**
 * The <tt>arguments</tt> array allocated for a function.
 * 
 * @author Asger
 */
public class ArgumentsArrayValue extends ObjectValue {

    private Function function;
    private Context context;

    public ArgumentsArrayValue(Function function, Context context) {
        this.function = function;
        this.context = context;
    }

    /**
     * Execution context of the function whose <tt>arguments</tt>
     * array is represented.
     */
    @Override
    public Context getContext() {
        return context;
    }
    public Function getFunction() {
        return function;
    }
    @Override
    public ArgumentsArrayValue replaceContext(Context newContext) {
        return new ArgumentsArrayValue(function, newContext);
    }

    @Override
    public ArgumentsArrayValue makeContextInsensitive() {
        return new ArgumentsArrayValue(function, NullContext.Instance);
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
        ArgumentsArrayValue other = (ArgumentsArrayValue) obj;
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
