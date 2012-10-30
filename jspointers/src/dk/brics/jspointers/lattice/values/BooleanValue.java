package dk.brics.jspointers.lattice.values;

/**
 * Value representing a primitive boolean.
 */
public class BooleanValue extends PrimitiveValue {
    /**
     * The only instance of {@link BooleanValue}.
     */
    public static final BooleanValue Instance = new BooleanValue();

    private BooleanValue() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 325346;
    }
    @Override
    public BasicType getBasicType() {
        return BasicType.BOOLEAN;
    }
}
