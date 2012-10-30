package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.ObjectValue;

/**
 * <tt>obj.[[DynamicStore]]</tt> contains all values assigned by a
 * dynamic store statement (except those that are known to be integers).
 * <p/>
 *  
 *
 * @author Asger
 */
public class DynamicStorePropertyKey extends Key {

    private ObjectValue object;

    public DynamicStorePropertyKey(ObjectValue object) {
        this.object = object;
    }

    public ObjectValue getObject() {
        return object;
    }

    @Override
    public Key makeContextInsensitive() {
        return new DynamicStorePropertyKey(object.makeContextInsensitive());
    }

    @Override
    public void apply(KeyVisitor v) {
        v.caseDynamicStoreProperty(this);
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
        DynamicStorePropertyKey other = (DynamicStorePropertyKey) obj;
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
