package dk.brics.jspointers.lattice.contexts;

import dk.brics.jspointers.lattice.values.ObjectValue;

public class ObjectContext extends Context {
    private ObjectValue object;

    public ObjectContext(ObjectValue object) {
        assert object != null;
        this.object = object;
    }

    public ObjectValue getObject() {
        return object;
    }

    @Override
    public Context getParentContext() {
        return object.getContext();
    }

    @Override
    public Context replaceParentContext(Context newParentContext) {
        ObjectValue val = object.replaceContext(newParentContext);
        if (val == object) {
            return this;
        } else {
            return new ObjectContext(val);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
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
        ObjectContext other = (ObjectContext) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }


}
