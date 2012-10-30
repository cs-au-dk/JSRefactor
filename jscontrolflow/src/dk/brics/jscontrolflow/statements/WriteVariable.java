package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * <i>var</i> = v<sub>value</sub>
 */
public class WriteVariable extends WriteStatement implements IVariableAccessStatement {
    private String varName;
    private int valueVar;
    private Scope scope;

    public WriteVariable(String varName, int valueVar, Scope scope) {
        this.varName = varName;
        this.valueVar = valueVar;
        this.scope = scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
    public Scope getScope() {
        return scope;
    }

    public String getVarName() {
        return varName;
    }
    public void setVarName(String varName) {
        this.varName = varName;
    }
    @Override
    public int getValueVar() {
        return valueVar;
    }
    public void setValueVar(int valueVar) {
        this.valueVar = valueVar;
    }
    @Override
    public boolean canThrowException() {
        return true; // XXX Can WriteVariable throw an exception??
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(valueVar);
    }
    @Override
    public void apply(StatementVisitor v) {
        v.caseWriteVariable(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseWriteVariable(this, arg);
    }
}
