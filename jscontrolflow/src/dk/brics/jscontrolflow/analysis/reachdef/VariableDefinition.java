package dk.brics.jscontrolflow.analysis.reachdef;

public abstract class VariableDefinition {
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract int hashCode();

    public abstract void apply(VariableDefinitionVisitor v);
    public abstract <Q,A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg);
}
