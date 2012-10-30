package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The <tt>Object.create</tt> native function.
 */
public final class ObjectGetPrototypeOfNative extends NativeFunctionValue {
	
	public static final ObjectGetPrototypeOfNative Instance = new ObjectGetPrototypeOfNative();
	
	private ObjectGetPrototypeOfNative() {}

	@Override
	public void apply(NativeFunctionVisitor visitor) {
		visitor.caseObjectGetPrototypeOf(this);
	}

	@Override
	public String getPrettyName() {
		return "getPrototypeOf";
	}

	@Override
	public Set<String> getNativeMembers() {
		return Collections.emptySet();
	}
    @Override
    public Set<String> getNativeStaticMembers() {
    	return Collections.emptySet();
    }

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
	
	@Override
	public int hashCode() {
		return 1195453;
	}
	
}
