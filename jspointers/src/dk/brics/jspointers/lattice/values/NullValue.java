package dk.brics.jspointers.lattice.values;

/**
 * Value representing the primitive null value.
 */
public class NullValue extends PrimitiveValue {
    /**
     * The only instance of {@link NullValue}.
     */
    public static final NullValue Instance = new NullValue();

    private NullValue() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 600;
    }
    @Override
    public BasicType getBasicType() {
        return BasicType.NULL;
    }
}
