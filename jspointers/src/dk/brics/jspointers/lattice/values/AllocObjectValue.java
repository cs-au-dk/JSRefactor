package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;

public class AllocObjectValue extends ObjectValue {
    private Object allocsite;
    private Context context;

    public AllocObjectValue(Object allocsite, Context context) {
        this.allocsite = allocsite;
        this.context = context;
    }

    /**
     * Execution context of the function whose body performed the allocation.
     */
    @Override
    public Context getContext() {
        return context;
    }
    public Object getAllocsite() {
        return allocsite;
    }

    @Override
    public AllocObjectValue replaceContext(Context newContext) {
        return new AllocObjectValue(this.allocsite, newContext);
    }

    @Override
    public AllocObjectValue makeContextInsensitive() {
        return new AllocObjectValue(this.allocsite, NullContext.Instance);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
        + ((allocsite == null) ? 0 : allocsite.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
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
        AllocObjectValue other = (AllocObjectValue) obj;
        if (allocsite == null) {
            if (other.allocsite != null) {
                return false;
            }
        } else if (!allocsite.equals(other.allocsite)) {
            return false;
        }
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        return true;
    }
}
