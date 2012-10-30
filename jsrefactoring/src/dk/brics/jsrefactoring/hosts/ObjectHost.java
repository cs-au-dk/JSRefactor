package dk.brics.jsrefactoring.hosts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jspointers.lattice.values.ObjectValue;

/**
 * An object label as a host.
 */
public class ObjectHost extends Host {

    private ObjectValue object;

    public ObjectHost(ObjectValue object) {
        this.object = object;
    }
    
    public ObjectValue getObject() {
        return object;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectHost other = (ObjectHost) obj;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        return true;
    }
    
    public static Set<ObjectHost> wrap(Collection<? extends ObjectValue> collection) {
        Set<ObjectHost> set = new HashSet<ObjectHost>();
        for (ObjectValue val : collection) {
            set.add(new ObjectHost(val));
        }
        return set;
    }
    public static Set<ObjectValue> unwrap(Collection<? extends ObjectHost> collection) {
        Set<ObjectValue> set = new HashSet<ObjectValue>();
        for (ObjectHost host : collection) {
            set.add(host.getObject());
        }
        return set;
    }
    
    
}
