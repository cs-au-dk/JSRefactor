package dk.brics.jscontrolflow.statements;

import java.util.Arrays;
import java.util.Collection;

/**
 * v<sub>result</sub> = v<sub>base</sub>[v<sub>property</sub>]
 */
public class ReadProperty extends ReadStatement implements IPropertyAccessStatement {
    private int baseVar;
    private int propertyVar;

    public ReadProperty(int resultVar, int baseVar, int propertyVar) {
        super(resultVar);
        this.baseVar = baseVar;
        this.propertyVar = propertyVar;
    }
    public int getBaseVar() {
        return baseVar;
    }
    public void setBaseVar(int baseVar) {
        this.baseVar = baseVar;
    }
    public int getPropertyVar() {
        return propertyVar;
    }
    public void setPropertyVar(int propertyVar) {
        this.propertyVar = propertyVar;
    }
    @Override
    public boolean canThrowException() {
        return true; // if base is null/undefined
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Arrays.asList(baseVar, propertyVar);
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseReadProperty(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseReadProperty(this, arg);
    }
}
