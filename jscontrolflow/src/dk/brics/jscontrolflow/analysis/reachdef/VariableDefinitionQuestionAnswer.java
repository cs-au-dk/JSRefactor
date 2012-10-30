package dk.brics.jscontrolflow.analysis.reachdef;

public interface VariableDefinitionQuestionAnswer<Q,A> {
    A caseStatement(StatementVariableDefinition def, Q arg);
    A caseParameter(ParameterVariableDefinition def, Q arg);
    A caseArgumentsArray(ArgumentsArrayVariableDefinition def, Q arg);
    A caseUninitialized(UninitializedVariableDefinition def, Q arg);
    A caseSelf(SelfVariableDefinition def, Q arg);
}
