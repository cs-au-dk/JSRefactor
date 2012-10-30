package dk.brics.jsrefactoring.nodes;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.ADynamicPropertyExp;

public class DynamicPropertyExpAccess extends AccessWithoutName {
	private ADynamicPropertyExp exp;

	public DynamicPropertyExpAccess(ADynamicPropertyExp exp) {
		this.exp = exp;
	}

	@Override
	public ADynamicPropertyExp getNode() {
	    return exp;
	}
	public ADynamicPropertyExp getExp() {
		return exp;
	}
	
	@Override
	public boolean isPrototypeSensitive() {
		return isRValue();
	}
	@Override
	public boolean isLValue() {
		return AstUtil.isLValue(getNode());
	}
	@Override
	public boolean isRValue() {
		return AstUtil.isRValue(getNode());
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
		DynamicPropertyExpAccess other = (DynamicPropertyExpAccess) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		return true;
	}

	@Override
	public <Q, A> A apply(AccessWithoutNameVisitor<Q, A> v, Q arg) {
		return v.caseDynamicProperty(this, arg);
	}
	
	
}
