package dk.brics.jsrefactoring.nodes;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.TId;

public class PropertyExpAccess extends AccessWithName {
	private final APropertyExp exp;
	private final String name;
	public PropertyExpAccess(APropertyExp exp) {
		this.exp = exp;
		this.name = Literals.getName(exp);
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
	public APropertyExp getNode() {
		return exp;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void replaceName(String newName) {
		AstUtil.replaceNode(exp.getName(), new TId(newName));
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
		PropertyExpAccess other = (PropertyExpAccess) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		return true;
	}
	@Override
	public <Q,A> A apply(AccessWithNameVisitor<Q,A> v, Q arg) {
		return v.casePropertyExp(this, arg);
	}
}