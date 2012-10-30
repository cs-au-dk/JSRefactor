package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * v<sub>result</sub> = <i>var</i><br/>
 */
public class ReadVariable extends ReadStatement implements IVariableAccessStatement {
    private String varName;
    private Scope scope;

    public ReadVariable(int resultVar, String varName, Scope scope) {
        super(resultVar);
        this.varName = varName;
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public boolean canThrowException() {
        return true; // XXX Can ReadVariable throw an exception??
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseReadVariable(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseReadVariable(this, arg);
    }

}
