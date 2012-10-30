package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.keys.DefaultPropertyKey;
import dk.brics.jspointers.lattice.keys.DynamicStorePropertyKey;
import dk.brics.jspointers.lattice.keys.IntegerPropertyKey;
import dk.brics.jspointers.lattice.keys.PrototypePropertyKey;

/**
 * Supertype of all values that are objects (as opposed to {@link PrimitiveValue}).
 * There is no distinction between "object label X" and "pointer to object with label X".
 * 
 * @author asf
 */
public abstract class ObjectValue extends Value {
    private final DefaultPropertyKey defaultProperty = new DefaultPropertyKey(this);
    private final DynamicStorePropertyKey dynamicStoreProperty = new DynamicStorePropertyKey(this);
    private final IntegerPropertyKey integerProperty = new IntegerPropertyKey(this);
    private final PrototypePropertyKey prototypeProperty = new PrototypePropertyKey(this);

    /**
     * Returns an instance of {@link DefaultPropertyKey} referring to this
     * object value. It is cached here for performance and convenience.
     */
    public DefaultPropertyKey getDefaultProperty() {
        return defaultProperty;
    }

    public DynamicStorePropertyKey getDynamicStoreProperty() {
        return dynamicStoreProperty;
    }

    public IntegerPropertyKey getIntegerProperty() {
        return integerProperty;
    }

    public PrototypePropertyKey getPrototypeProperty() {
        return prototypeProperty;
    }

    /**
     * Returns the object's variable allocation context, or <tt>null</tt> if
     * this object has no context variable.
     * @return a context or <tt>null</tt>
     */
    public abstract Context getContext();

    /**
     * Returns the object's variable allocation context, or itself if this
     * object has no context variable. 
     * @param newContext a context; not null
     * @return an object of same type as this instance; not null
     */
    public abstract ObjectValue replaceContext(Context newContext);

    @Override
    public abstract ObjectValue makeContextInsensitive();

    @Override
    public final BasicType getBasicType() {
        return BasicType.OBJECT;
    }
}
