package dk.brics.jspointers.lattice.contexts;

/**
 * Singleton object representing the context in which the global scope
 * is running.
 * 
 * @author Asger
 */
public class MainContext extends Context {
    public static final MainContext Instance = new MainContext();

    private MainContext() {}

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
        return 994941;
    }
}
