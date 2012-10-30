package dk.brics.jspointers.analysis;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

public class NativeTransfer extends TransferNode {
	
	private NativeFunctionValue nativeFunction;
	private Context context;

	public NativeTransfer(NativeFunctionValue nativeFunction, Context context) {
		this.nativeFunction = nativeFunction;
		this.context = context;
	}
	
	
	public NativeFunctionValue getNativeFunction() {
		return nativeFunction;
	}
	public Context getContext() {
		return context;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result
				+ ((nativeFunction == null) ? 0 : nativeFunction.hashCode());
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
		NativeTransfer other = (NativeTransfer) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (nativeFunction == null) {
			if (other.nativeFunction != null)
				return false;
		} else if (!nativeFunction.equals(other.nativeFunction))
			return false;
		return true;
	}
	
}
