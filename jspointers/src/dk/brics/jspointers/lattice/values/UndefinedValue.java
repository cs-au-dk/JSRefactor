package dk.brics.jspointers.lattice.values;

/**
 * Value representing the primitive undefined value.
 */
public class UndefinedValue extends PrimitiveValue {
    /**
     * The only instance of {@link UndefinedValue}.
     */
    public static final UndefinedValue Instance = new UndefinedValue();

    private UndefinedValue() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 50;
    }
    @Override
    public BasicType getBasicType() {
        return BasicType.UNDEFINED;
    }
}
