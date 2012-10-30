package dk.brics.jspointers.lattice.values;

import dk.brics.jscontrolflow.Function;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.MainContext;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.OuterFunctionPropertyKey;

public final class UserFunctionValue extends FunctionValue {
    private Function function;
    private Context context;

    private OuterFunctionPropertyKey outerFunction = new OuterFunctionPropertyKey(this);

    public UserFunctionValue(Function function, Context context) {
        assert context != null : "Context was null";
        this.function = function;
        this.context = context;
    }

    /**
     * Execution context of the outer function when it allocated this function instance.
     * For the main function, {@link MainContext} is used.
     */
    @Override
    public Context getContext() {
        return context;
    }
    public Function getFunction() {
        return function;
    }

    @Override
    public UserFunctionValue replaceContext(Context newContext) {
        return new UserFunctionValue(function, newContext);
    }

    public OuterFunctionPropertyKey getOuterFunction() {
        return outerFunction;
    }

    @Override
    public UserFunctionValue makeContextInsensitive() {
        return new UserFunctionValue(function, NullContext.Instance);
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
        UserFunctionValue other = (UserFunctionValue) obj;
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

    @Override
    public String toString() {
        String func;
        //		if (function.getName() == null) {
        //			func = new File(function.getSourceLocation().getFileName()).getName() + ":" + function.getSourceLocation().getLineNumber();
        //		} else {
        //			func = function.getName();
        //		}
        func = function.toString();
        return "UserFunctionValue["+func+"]";
    }
}
