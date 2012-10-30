package dk.brics.jsrefactoring.nodes;

import java.util.Set;

import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsutil.CollectionUtil;

/**
 * A node that depends on property names, but the name of the property it refers
 * to is not constant.
 * <p/>
 * Note: Currently the only implementor is {@link DynamicPropertyExpAccess}, but this
 * may change.
 */
public abstract class AccessWithoutName extends Access {
	@Override
	public Set<ObjectValue> getBase(Master input) {
		return getReceivers(input);
	}
	
	@Override
	public boolean mayAlias(Master input, Access acc) {
		return CollectionUtil.intersects(this.getBase(input), acc.getBase(input));
	}
	
	public abstract <Q,A> A apply(AccessWithoutNameVisitor<Q, A> v, Q arg);
	
	public final <Q,A> A apply(AccessVisitor<Q, A> v, Q arg) {
		return apply((AccessWithoutNameVisitor<Q, A>)v, arg);
	}
}
