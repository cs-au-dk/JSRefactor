package dk.brics.jspointers.lattice.contexts;

/**
 * The context inserted by the widening function to cut off
 * long chains of contexts.
 * 
 * @author Asger
 */
public class WidenedContext extends Context {
    public static final WidenedContext Instance = new WidenedContext();

    private WidenedContext() {}

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
        return 69592;
    }
}
