package dk.brics.jspointers.lattice.keys;

import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;

public class OutputPointKey extends Key {
	private OutputPoint outputPoint;
	private Context context;
	
	public OutputPointKey(OutputPoint outputPoint, Context context) {
		this.outputPoint = outputPoint;
		this.context = context;
	}
	
	public OutputPoint getOutputPoint() {
		return outputPoint;
	}
	public Context getContext() {
		return context;
	}
	@Override
	public OutputPointKey makeContextInsensitive() {
		return new OutputPointKey(outputPoint, NullContext.Instance);
	}
	
	@Override
	public void apply(KeyVisitor v) {
		v.caseOutputPoint(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result
				+ ((outputPoint == null) ? 0 : outputPoint.hashCode());
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
		OutputPointKey other = (OutputPointKey) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (outputPoint == null) {
			if (other.outputPoint != null)
				return false;
		} else if (!outputPoint.equals(other.outputPoint))
			return false;
		return true;
	}
}
