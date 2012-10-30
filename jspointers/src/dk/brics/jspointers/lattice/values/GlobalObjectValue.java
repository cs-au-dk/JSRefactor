package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;

public final class GlobalObjectValue extends ObjectValue {
    public static final GlobalObjectValue Instance = new GlobalObjectValue();

    private GlobalObjectValue() {}

    @Override
    public GlobalObjectValue makeContextInsensitive() {
        return this;
    }
    @Override
    public Context getContext() {
        return null;
    }
    @Override
    public GlobalObjectValue replaceContext(Context newContext) {
        return this;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 43095;
    }
}
