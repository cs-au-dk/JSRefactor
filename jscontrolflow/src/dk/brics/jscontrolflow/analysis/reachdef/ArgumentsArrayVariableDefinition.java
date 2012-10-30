package dk.brics.jscontrolflow.analysis.reachdef;

public class ArgumentsArrayVariableDefinition extends VariableDefinition {

    public static final ArgumentsArrayVariableDefinition Instance = new ArgumentsArrayVariableDefinition();

    private ArgumentsArrayVariableDefinition() {}

    @Override
    public int hashCode() {
        return ArgumentsArrayVariableDefinition.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public void apply(VariableDefinitionVisitor v) {
        v.caseArgumentsArray(this);
    }

    @Override
    public <Q, A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg) {
        return v.caseArgumentsArray(this, arg);
    }
    
    @Override
    public String toString() {
    	return "arguments array";
    }

}
