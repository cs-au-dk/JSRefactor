package dk.brics.jscontrolflow.analysis.reachdef;

public interface VariableDefinitionVisitor {
    void caseStatement(StatementVariableDefinition def);
    void caseParameter(ParameterVariableDefinition def);
    void caseArgumentsArray(ArgumentsArrayVariableDefinition def);
	void caseUninitialized(UninitializedVariableDefinition definition);
	void caseSelf(SelfVariableDefinition def);
}
