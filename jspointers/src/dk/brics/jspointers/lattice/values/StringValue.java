package dk.brics.jspointers.lattice.values;

/**
 * Value representing a primitive string.
 */
public class StringValue extends PrimitiveValue {
    /**
     * The only instance of {@link StringValue}.
     */
    public static final StringValue Instance = new StringValue();

    private StringValue() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 34578;
    }
    @Override
    public BasicType getBasicType() {
        return BasicType.STRING;
    }
}
