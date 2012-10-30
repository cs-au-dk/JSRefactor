package dk.brics.jspointers.lattice.values;

/**
 * Value representing a number.
 */
public class NumberValue extends PrimitiveValue {
    /**
     * The only instance of NumberValue.
     */
    public static final NumberValue Instance = new NumberValue();

    private NumberValue() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 12356;
    }
    @Override
    public BasicType getBasicType() {
        return BasicType.NUMBER;
    }
}
