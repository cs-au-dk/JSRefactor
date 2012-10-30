package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.ObjectValue;

/**
 * Key for an integer-index property on an object.
 * 
 * @author Asger
 */
public class IntegerPropertyKey extends Key {
    private final ObjectValue object;

    public IntegerPropertyKey(ObjectValue object) {
        this.object = object;
    }

    public ObjectValue getObject() {
        return object;
    }

    @Override
    public Key makeContextInsensitive() {
        return new IntegerPropertyKey(object.makeContextInsensitive());
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
        IntegerPropertyKey other = (IntegerPropertyKey) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(KeyVisitor key) {
        key.caseIntegerProperty(this);
    }

}
