package dk.brics.jsrefactoring.nodes;

public interface AccessWithoutNameVisitor<Q,A> {
	A caseDynamicProperty(DynamicPropertyExpAccess node, Q arg);
}
