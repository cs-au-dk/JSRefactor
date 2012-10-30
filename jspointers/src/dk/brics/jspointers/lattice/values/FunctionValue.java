package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;

public abstract class FunctionValue extends ObjectValue {
    private FunctionPrototypeValue prototype = new FunctionPrototypeValue(this);

    public FunctionPrototypeValue getFunctionPrototype() {
        return prototype;
    }

    @Override
    public abstract FunctionValue makeContextInsensitive();

    @Override
    public abstract FunctionValue replaceContext(Context newContext);
}
