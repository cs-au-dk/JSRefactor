package dk.brics.jspointers.analysis;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.WidenedContext;

public class ContextWidening {
    /**
     * Reduces a context to the specified depth (or shorter) by replacing
     * some context objects by {@link WidenedContext}.
     * <p/>
     * The <i>depth</i> of a context is the length of the longest chain of 
     * context objects reachable from the given context.
     * @param context a context
     * @param depth non-negative integer
     * @return a context; possibly the same instance that was given
     */
    public static Context widenContext(Context context, int depth) {
        if (depth <= 0) {
            return WidenedContext.Instance;
        } else if (context.getParentContext() == null) {
            return context;
        } else {
            return context.replaceParentContext(widenContext(context.getParentContext(), depth-1));
        }
    }
}
