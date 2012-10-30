package dk.brics.jspointers.lattice.values;

public abstract class PrimitiveValue extends Value {
    @Override
    public PrimitiveValue makeContextInsensitive() {
        return this;
    }
}
