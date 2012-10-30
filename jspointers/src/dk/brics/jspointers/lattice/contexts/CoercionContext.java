package dk.brics.jspointers.lattice.contexts;

/**
 * The context in which the coercion functions <tt>toString</tt> and
 * <tt>valueOf</tt> are executed.
 * 
 * @author Asger
 */
public final class CoercionContext extends Context {
    public static final CoercionContext Instance = new CoercionContext();

    private CoercionContext() {}

    @Override
    public Context getParentContext() {
        return null;
    }
    @Override
    public CoercionContext replaceParentContext(Context newParentContext) {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return -39592;
    }
}
