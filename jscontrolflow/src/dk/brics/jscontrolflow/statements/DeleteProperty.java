package dk.brics.jscontrolflow.statements;

import java.util.Arrays;
import java.util.Collection;

/**
 * <tt>delete</tt> v<sub>object</sub>[v<sub>property</sub>] 
 */
public class DeleteProperty extends Assignment implements IPropertyAccessStatement {
    private int baseVar;
    private int propertyVar;

    public DeleteProperty(int resultVar, int baseVar, int propertyVar) {
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
    public Collection<Integer> getReadVariables() {
        return Arrays.asList(baseVar, propertyVar);
    }

    @Override
    public boolean canThrowException() {
        return true;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseDeleteProperty(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseDeleteProperty(this, arg);
    }
}
