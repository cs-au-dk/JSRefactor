package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.natives.FunctionBindResultNative;

public class BoundDefaultArgKey extends Key {
	private FunctionBindResultNative function;
	
	public BoundDefaultArgKey(FunctionBindResultNative function) {
		this.function = function;
	}
	
	public FunctionBindResultNative getFunction() {
		return function;
	}

	@Override
	public void apply(KeyVisitor v) {
		v.caseBoundDefaultArg(this);
	}
	
	@Override
	public Key makeContextInsensitive() {
		return new BoundDefaultArgKey(function.makeContextInsensitive());
	}
	
	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
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
		BoundDefaultArgKey other = (BoundDefaultArgKey) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		return true;
	}
	
	
}
