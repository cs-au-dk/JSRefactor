package dk.brics.jspointers.lattice.contexts;

public abstract class Context {
    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Returns the context's parent context, or <tt>null</tt>
     * if it has no parent context.
     */
    public abstract Context getParentContext();

    /**
     * Returns a context identical to this one, except the parent
     * context has been substituted with the given context.
     * If this context has no parent context, it should return itself.
     * @param newParentContext a context; not null
     * @return a context; not null
     */
    public abstract Context replaceParentContext(Context newParentContext);
}
