package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.ObjectValue;

/**
 * Key for a named property on an object.
 */
public final class NamedPropertyKey extends Key {
    private final ObjectValue object;
    private final String property;

    public NamedPropertyKey(ObjectValue object, String property) {
        this.object = object;
        this.property = property;
    }

    public ObjectValue getObject() {
        return object;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public Key makeContextInsensitive() {
        return new NamedPropertyKey(object.makeContextInsensitive(), property);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result
        + ((property == null) ? 0 : property.hashCode());
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
        NamedPropertyKey other = (NamedPropertyKey) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        if (property == null) {
            if (other.property != null) {
                return false;
            }
        } else if (!property.equals(other.property)) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(KeyVisitor key) {
        key.caseNamedProperty(this);
    }


}
