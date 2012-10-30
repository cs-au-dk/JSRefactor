package dk.brics.jspointers.lattice.values;

public abstract class Value {
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public abstract Value makeContextInsensitive();

    public abstract BasicType getBasicType();
}
