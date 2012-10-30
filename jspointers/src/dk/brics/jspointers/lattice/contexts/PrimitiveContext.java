package dk.brics.jspointers.lattice.contexts;

public class PrimitiveContext extends Context {
    public static final PrimitiveContext Instance = new PrimitiveContext();

    private PrimitiveContext() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 3949221;
    }

    @Override
    public Context getParentContext() {
        return null;
    }
    @Override
    public Context replaceParentContext(Context newParentContext) {
        return this;
    }
}
