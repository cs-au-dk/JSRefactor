package dk.brics.jspointers.lattice.contexts;

public class DOMInvokeContext extends Context {
    public static final DOMInvokeContext Instance = new DOMInvokeContext();

    private DOMInvokeContext() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 11021;
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
