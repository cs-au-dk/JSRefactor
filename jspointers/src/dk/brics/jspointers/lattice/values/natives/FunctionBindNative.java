package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The <tt>Function.prototype.bind</tt> function.
 */
public final class FunctionBindNative extends NativeFunctionValue {
	
	public static final FunctionBindNative Instance = new FunctionBindNative();
	
	private FunctionBindNative() {}

	@Override
	public void apply(NativeFunctionVisitor visitor) {
		visitor.caseFunctionBind(this);
	}

	@Override
	public String getPrettyName() {
		return "bind";
	}

	@Override
	public Set<String> getNativeMembers() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
	
	@Override
	public int hashCode() {
		return 9389672;
	}
    @Override
    public Set<String> getNativeStaticMembers() {
    	return Collections.emptySet();
    }
	
}
