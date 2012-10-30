package dk.brics.jspointers.lattice.contexts;


/**
 * Context specific to one invocation site. Used for 1-callsite context sensitivity.
 * 
 * @author Asger
 */
public class InvokeContext extends Context {
    private Object callsiteId;
    private Context parentContext;

    public InvokeContext(Object callsiteId, Context parentContext) {
        assert callsiteId != null : "Call node cannot be null";
        assert parentContext != null : "Parent context cannot be null";
        this.callsiteId = callsiteId;
        this.parentContext = parentContext;
    }

    public Object getCallsiteId() {
        return callsiteId;
    }

    @Override
    public Context getParentContext() {
        return parentContext;
    }
    @Override
    public InvokeContext replaceParentContext(Context newParentContext) {
        if (newParentContext == parentContext) {
            return this;
        } else {
            return new InvokeContext(callsiteId, newParentContext);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callsiteId == null) ? 0 : callsiteId.hashCode());
        result = prime * result
        + ((parentContext == null) ? 0 : parentContext.hashCode());
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
        InvokeContext other = (InvokeContext) obj;
        if (callsiteId == null) {
            if (other.callsiteId != null) {
                return false;
            }
        } else if (!callsiteId.equals(other.callsiteId)) {
            return false;
        }
        if (parentContext == null) {
            if (other.parentContext != null) {
                return false;
            }
        } else if (!parentContext.equals(other.parentContext)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Invoke[" + callsiteId + "]";
    }
}
