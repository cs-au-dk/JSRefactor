package dk.brics.jscontrolflow.analysis.reachdef;

/**
 * The <tt>undefined</tt> value assigned to every variable
 * upon entering a function.
 */
public class UninitializedVariableDefinition extends VariableDefinition {
	
	public static final UninitializedVariableDefinition Instance = new UninitializedVariableDefinition();
	
	private UninitializedVariableDefinition() {}

	@Override
	public int hashCode() {
		return 20201943;
	}

	@Override
	public void apply(VariableDefinitionVisitor v) {
		v.caseUninitialized(this);
	}

	@Override
	public <Q, A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg) {
		return v.caseUninitialized(this, arg);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}	
}
