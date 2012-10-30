package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.ObjectValue;

public class PrototypePropertyKey extends Key {
    private ObjectValue object;

    public PrototypePropertyKey(ObjectValue object) {
        this.object = object;
    }

    public ObjectValue getObject() {
        return object;
    }

    @Override
    public Key makeContextInsensitive() {
        return new PrototypePropertyKey(object.makeContextInsensitive());
    }

    @Override
    public void apply(KeyVisitor v) {
        v.casePrototypePropertyKey(this);
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
        PrototypePropertyKey other = (PrototypePropertyKey) obj;
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
