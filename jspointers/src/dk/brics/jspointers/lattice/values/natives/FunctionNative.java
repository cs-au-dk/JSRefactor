package dk.brics.jspointers.lattice.values.natives;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The native <tt>Function</tt> object (which is a function).
 */
public class FunctionNative extends NativeFunctionValue {
    public static final FunctionNative Instance = new FunctionNative();
    public static final Set<String> NATIVE_MEMBERS = new HashSet<String>(Arrays.asList("call","apply","bind"));

    private FunctionNative() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 3059;
    }

    @Override
    public void apply(NativeFunctionVisitor visitor) {
        visitor.caseFunction(this);
    }

    @Override
    public String getPrettyName() {
        return "Function";
    }

    @Override
    public Set<String> getNativeMembers() {
    	return NATIVE_MEMBERS;
    }
    @Override
    public Set<String> getNativeStaticMembers() {
    	return Collections.emptySet();
    }
}
