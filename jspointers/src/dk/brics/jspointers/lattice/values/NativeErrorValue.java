package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;

public class NativeErrorValue extends ObjectValue {
    public static final NativeErrorValue Instance = new NativeErrorValue();

    private NativeErrorValue() {}

    @Override
    public int hashCode() {
        return 395211;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public ObjectValue makeContextInsensitive() {
        return this;
    }
    @Override
    public Context getContext() {
        return null;
    }
    @Override
    public ObjectValue replaceContext(Context newContext) {
        return this;
    }
}
