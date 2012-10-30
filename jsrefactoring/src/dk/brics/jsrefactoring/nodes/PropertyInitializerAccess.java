package dk.brics.jsrefactoring.nodes;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.TId;

public class PropertyInitializerAccess extends AccessWithName {
	private ANormalObjectLiteralProperty property;
	public PropertyInitializerAccess(ANormalObjectLiteralProperty property) {
		this.property = property;
	}
	@Override
	public boolean isPrototypeSensitive() {
		return false;
	}
	@Override
	public boolean isLValue() {
		return true;
	}
	@Override
	public boolean isRValue() {
		return false;
	}
	@Override
	public ANormalObjectLiteralProperty getNode() {
		return property;
	}
	@Override
	public String getName() {
		return AstUtil.getPropertyName(property.getName());
	}
	@Override
	public void replaceName(String newName) {
		AstUtil.replaceNode(property.getName(), new AIdentifierPropertyName(new TId(newName)));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
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
		PropertyInitializerAccess other = (PropertyInitializerAccess) obj;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}
	@Override
	public <Q,A> A apply(AccessWithNameVisitor<Q,A> v, Q arg) {
		return v.casePropertyInitializer(this, arg);
	}
}