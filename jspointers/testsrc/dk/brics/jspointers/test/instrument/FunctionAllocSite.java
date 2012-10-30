package dk.brics.jspointers.test.instrument;

import dk.brics.jsparser.node.IFunction;

public class FunctionAllocSite extends AllocSite {
	private IFunction exp;

	public FunctionAllocSite(IFunction exp) {
		this.exp = exp;
	}

	public IFunction getExp() {
		return exp;
	}

	public void setExp(IFunction exp) {
		this.exp = exp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exp == null) ? 0 : exp.hashCode());
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
		FunctionAllocSite other = (FunctionAllocSite) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		return true;
	}
	
	@Override
	public <A> A apply(AllocSiteAnswerVisitor<A> v) {
		return v.caseFunction(this);
	}
	@Override
	public void apply(AllocSiteVisitor v) {
	    v.caseFunction(this);
	}
}
