package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.lattice.values.natives.FunctionBindResultNative;

public class BoundArgKey extends Key {
	private FunctionBindResultNative function;
	private int index;
	
	public BoundArgKey(FunctionBindResultNative function, int index) {
		this.function = function;
		this.index = index;
	}
	
	public FunctionBindResultNative getFunction() {
		return function;
	}
	public int getIndex() {
		return index;
	}

	@Override
	public void apply(KeyVisitor v) {
		v.caseBoundArg(this);
	}

	@Override
	public Key makeContextInsensitive() {
		return new BoundArgKey(function.makeContextInsensitive(), index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result + index;
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
		BoundArgKey other = (BoundArgKey) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (index != other.index)
			return false;
		return true;
	}
	
}
