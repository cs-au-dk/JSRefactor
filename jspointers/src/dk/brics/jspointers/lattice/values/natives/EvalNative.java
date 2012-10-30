package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

public class EvalNative extends NativeFunctionValue {
	
	public static final EvalNative Instance = new EvalNative();
	
	private EvalNative() {}
	
	@Override
	public void apply(NativeFunctionVisitor visitor) {
		visitor.caseEval(this);
	}

	@Override
	public String getPrettyName() {
		return "eval";
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
		return 45452165;
	}
	
}
