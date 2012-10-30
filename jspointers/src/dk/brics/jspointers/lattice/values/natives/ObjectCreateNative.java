package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The <tt>Object.create</tt> native function.
 */
public final class ObjectCreateNative extends NativeFunctionValue {
	
	public static final ObjectCreateNative Instance = new ObjectCreateNative();
	
	private ObjectCreateNative() {}

	@Override
	public void apply(NativeFunctionVisitor visitor) {
		visitor.caseObjectCreate(this);
	}

	@Override
	public String getPrettyName() {
		return "create";
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
		return 2395;
	}
	
}
