package dk.brics.jsrefactoring.nodes;

import java.util.Set;

import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsutil.CollectionUtil;


/**
 * A node that accesses a property with a known name.
 */
public abstract class AccessWithName extends Access {
	public abstract String getName();
	public abstract void replaceName(String newName);
	public abstract boolean isPrototypeSensitive();
	
	@Override
	public Set<ObjectValue> getBase(Master input) {
		return getBase(input, getName());
	}
	
	@Override
	public boolean mayAlias(final Master input, Access acc) {
		final Set<ObjectValue> bases = this.getBase(input);
		return acc.apply(new AbstractAccessVisitor<Void, Boolean>() {
			@Override
			public Boolean defaultAccessWithName(AccessWithName node, Void arg) {
				return getName().equals(node.getName()) &&
					   CollectionUtil.intersects(bases, node.getBase(input));
			}
			
			@Override
			public Boolean defaultAccessWithoutName(AccessWithoutName node,	Void arg) {
				return CollectionUtil.intersects(bases, node.getReceivers(input));
			}
		}, null);
	}
	
	public abstract <Q,A> A apply(AccessWithNameVisitor<Q,A> v, Q arg);
	
	public final <Q,A> A apply(AccessVisitor<Q, A> v, Q arg) {
		return apply((AccessWithNameVisitor<Q, A>)v, arg);
	}
}