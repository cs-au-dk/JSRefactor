package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

public class FunctionApplyNative extends NativeFunctionValue {

    public static final FunctionApplyNative Instance = new FunctionApplyNative();

    @Override
    public void apply(NativeFunctionVisitor visitor) {
        visitor.caseFunctionApply(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return 46482;
    }

    @Override
    public String getPrettyName() {
        return "apply";
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
