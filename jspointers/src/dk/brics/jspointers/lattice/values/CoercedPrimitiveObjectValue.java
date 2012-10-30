package dk.brics.jspointers.lattice.values;

import dk.brics.jspointers.lattice.contexts.Context;

public class CoercedPrimitiveObjectValue extends ObjectValue {
	private PrimitiveValue primitive;
	
	public CoercedPrimitiveObjectValue(PrimitiveValue primitive) {
		if (primitive == NullValue.Instance || primitive == UndefinedValue.Instance)
			throw new IllegalArgumentException("Null and undefined values do not coerce to object");
		this.primitive = primitive;
	}
	
	public PrimitiveValue getPrimitive() {
		return primitive;
	}

	@Override
	public Context getContext() {
		return null;
	}

	@Override
	public ObjectValue replaceContext(Context newContext) {
		return this;
	}

	@Override
	public ObjectValue makeContextInsensitive() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((primitive == null) ? 0 : primitive.hashCode());
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
		CoercedPrimitiveObjectValue other = (CoercedPrimitiveObjectValue) obj;
		if (primitive == null) {
			if (other.primitive != null)
				return false;
		} else if (!primitive.equals(other.primitive))
			return false;
		return true;
	}
	
}
