package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

public class FunctionCallNative extends NativeFunctionValue {

    public static final FunctionCallNative Instance = new FunctionCallNative();

    @Override
    public void apply(NativeFunctionVisitor visitor) {
        visitor.caseFunctionCall(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return 339945;
    }

    @Override
    public String getPrettyName() {
        return "call";
    }
    
    @Override
    public Set<String> getNativeMembers() {
    	return Collections.emptySet();
    }
    @Override
    public Set<String> getNativeStaticMembers() {
    	return Collections.emptySet();
    }

}
