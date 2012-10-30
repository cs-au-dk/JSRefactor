package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.BoundArgKey;
import dk.brics.jspointers.lattice.keys.BoundDefaultArgKey;
import dk.brics.jspointers.lattice.keys.BoundDynamicArgKey;
import dk.brics.jspointers.lattice.keys.BoundTargetKey;
import dk.brics.jspointers.lattice.keys.BoundThisArgKey;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The result of a <tt>Function.prototype.bind</tt> call.
 */
public class FunctionBindResultNative extends NativeFunctionValue {
	
	private Context context;
	
	public FunctionBindResultNative(Context context) {
		this.context = context;
	}
	
	public BoundTargetKey getBoundTarget() {
		return new BoundTargetKey(this);
	}
	public BoundThisArgKey getBoundThisArg() {
		return new BoundThisArgKey(this);
	}
	public BoundArgKey getBoundArg(int index) {
		return new BoundArgKey(this, index);
	}
	public BoundDefaultArgKey getBoundDefaultArg() {
		return new BoundDefaultArgKey(this);
	}
	public BoundDynamicArgKey getBoundDynamicArg() {
		return new BoundDynamicArgKey(this);
	}
	
	/**
	 * The context in which the <tt>bind</tt> function was called when this
	 * function was instantiated.
	 */
	@Override
	public Context getContext() {
		return context;
	}
	
	@Override
	public FunctionBindResultNative makeContextInsensitive() {
		return new FunctionBindResultNative(NullContext.Instance);
	}

	@Override
	public String getPrettyName() {
		return "bound-function";
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
	public void apply(NativeFunctionVisitor visitor) {
		visitor.caseFunctionBindResult(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionBindResultNative other = (FunctionBindResultNative) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

	
}
