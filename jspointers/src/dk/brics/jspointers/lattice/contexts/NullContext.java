package dk.brics.jspointers.lattice.contexts;

/**
 * Dummy context used to remove context-sensitive information from
 * a lattice point. Should <i>not</i> occur during the fixpoint search!
 * 
 * @author Asger
 */
public final class NullContext extends Context {
    public static final Context Instance = new NullContext();

    private NullContext() {}

    @Override
    public Context getParentContext() {
        return null;
    }
    @Override
    public Context replaceParentContext(Context newParentContext) {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 3936;
    }
}
