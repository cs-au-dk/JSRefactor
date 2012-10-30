package dk.brics.jspointers.lattice.values;

import java.util.Set;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.keys.NativeDefaultArgsKey;
import dk.brics.jspointers.lattice.keys.NativeDynamicArgsKey;
import dk.brics.jspointers.lattice.keys.NativeExceptionalResultKey;
import dk.brics.jspointers.lattice.keys.NativeLabelArgKey;
import dk.brics.jspointers.lattice.keys.NativeResultKey;
import dk.brics.jspointers.lattice.keys.NativeThisArgKey;
import dk.brics.jspointers.lattice.values.natives.NativeFunctionVisitor;

public abstract class NativeFunctionValue extends FunctionValue {
    public abstract void apply(NativeFunctionVisitor visitor);

    public NativeThisArgKey getThisArg(Context context) {
        return new NativeThisArgKey(this, context);
    }
    public NativeResultKey getResult(Context context) {
        return new NativeResultKey(this, context);
    }
    public NativeExceptionalResultKey getExceptionalResult(Context context) {
        return new NativeExceptionalResultKey(this, context);
    }
    public NativeDefaultArgsKey getDefaultArgs(Context context) {
        return new NativeDefaultArgsKey(this, context);
    }
    public NativeDynamicArgsKey getDynamicArgs(Context context) {
        return new NativeDynamicArgsKey(this, context);
    }
    public NativeLabelArgKey getLabelArg(Context context) {
        return new NativeLabelArgKey(this, context);
    }

    public abstract String getPrettyName();

    @Override
    public NativeFunctionValue makeContextInsensitive() {
        return this;
    }
    @Override
    public Context getContext() {
        return null;
    }
    @Override
    public NativeFunctionValue replaceContext(Context newContext) {
        return this;
    }
    
    /**
     * Returns the names of the properties natively defined on this function's public prototype object.
     * Does not include functions defined in harness files.
     * @return unmodifiable set
     */
    public abstract Set<String> getNativeMembers();
    
    /**
     * Returns the names of the properties natively defined on this function.
     * Does not include functions defined in harness files.
     * @return unmodifiable set
     */
    public abstract Set<String> getNativeStaticMembers();
}
