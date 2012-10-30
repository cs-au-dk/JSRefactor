package dk.brics.jspointers.lattice.keys;

public abstract class Key {
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract int hashCode();
    public abstract void apply(KeyVisitor v);

    public abstract Key makeContextInsensitive();
}
