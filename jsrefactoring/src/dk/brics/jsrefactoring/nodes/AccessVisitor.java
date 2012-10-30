package dk.brics.jsrefactoring.nodes;

public interface AccessVisitor<Q,A> extends 
					AccessWithNameVisitor<Q, A>, 
					AccessWithoutNameVisitor<Q, A> {
}
