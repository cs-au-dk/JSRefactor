package dk.brics.jsrefactoring.nodes;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.AStringConst;
import dk.brics.jsparser.node.TStringLiteral;

/**
 * An <tt>in</tt>-expression whose left operand (the property name) is a string literal.
 */
public class ConstInExpAccess extends AccessWithName {
	private ABinopExp exp;
	private AStringConst node;
	public ConstInExpAccess(AStringConst node, ABinopExp exp) {
		this.node = node;
		this.exp = exp;
	}
	public ABinopExp getExp() {
		return exp;
	}
	@Override
	public boolean isPrototypeSensitive() {
		return true; // 'in' expressions consider prototypes
	}
	@Override
	public boolean isLValue() {
		return false;
	}
	@Override
	public boolean isRValue() {
		return true;
	}
	@Override
	public AStringConst getNode() {
		return node;
	}
	@Override
	public String getName() {
		return Literals.parseStringLiteral(node.getStringLiteral().getText());
	}
	@Override
	public void replaceName(String newName) {
		char q = Literals.getQuoteSymbol(node.getStringLiteral().getText());
		String literal = Literals.unparseStringLiteral(newName, q);
		AstUtil.replaceNode(node.getStringLiteral(), new TStringLiteral(literal));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		ConstInExpAccess other = (ConstInExpAccess) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	@Override
	public <Q,A> A apply(AccessWithNameVisitor<Q,A> v, Q arg) {
		return v.caseConstantInExp(this, arg);
	}
}