package dk.brics.jsrefactoring.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.TId;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.hosts.ScopeHost;

public class CatchAccess extends AccessVariable {
	private ACatchClause node;
	
	public CatchAccess(ACatchClause node) {
		this.node = node;
	}

	@Override
	public List<ScopeHost> getSearchedScopes(Master input, String name) {
		return ScopeHost.getScopeChain(name, input.getCatchScope(node));
	}

	@Override
	public Set<ScopeHost> getAffectedScopes(Master input, String name) {
		List<ScopeHost> searchedScopes = getSearchedScopes(input, name);
		return Collections.singleton(searchedScopes.get(searchedScopes.size() - 1));
	}
	
	
	
	@Override
	public ACatchClause getNode() {
		return node;
	}
	@Override
	public String getName() {
		return Literals.parseIdentifier(node.getName().getText());
	}
	
	@Override
	public void replaceName(String newName) {
		AstUtil.replaceNode(node.getName(), new TId(newName));
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
	public <Q, A> A apply(AccessWithNameVisitor<Q, A> v, Q arg) {
		return v.caseCatch(this, arg);
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
		CatchAccess other = (CatchAccess) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
}
