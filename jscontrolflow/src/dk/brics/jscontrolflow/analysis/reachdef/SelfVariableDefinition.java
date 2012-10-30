package dk.brics.jscontrolflow.analysis.reachdef;

/**
 * Self-reference in a named function expression.
 */
public class SelfVariableDefinition extends VariableDefinition {
	
	private SelfVariableDefinition() {}
	
	public static final SelfVariableDefinition Instance = new SelfVariableDefinition();

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public int hashCode() {
		return 175563;
	}

	@Override
	public void apply(VariableDefinitionVisitor v) {
		v.caseSelf(this);
	}

	@Override
	public <Q, A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg) {
		return v.caseSelf(this, arg);
	}
	
}
