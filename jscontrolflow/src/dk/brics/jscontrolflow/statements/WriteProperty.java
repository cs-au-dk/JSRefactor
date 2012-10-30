package dk.brics.jscontrolflow.statements;

import java.util.Arrays;
import java.util.Collection;

/**
 * v<sub>base</sub>[v<sub>property</sub>] = v<sub>value</sub>
 * <p/>
 * Stores a property named ToString(v<sub>property</sub>) on the object ToObject(v<sub>base</sub>)
 * with the value v<sub>value</sub>.
 * <p/>
 * Note: If v<sub>base</sub> is not an object and not <tt>null</tt> or <tt>undefined</tt>, 
 * then the effects can be ignored, since nobody can hold a reference to the coerced object, 
 * so the side-effect cannot be observed.
 */
public class WriteProperty extends WriteStatement implements IPropertyAccessStatement {
    private int baseVar;
    private int propertyVar;
    private int valueVar;
    public WriteProperty(int baseVar, int propertyVar, int valueVar) {
        this.baseVar = baseVar;
        this.propertyVar = propertyVar;
        this.valueVar = valueVar;
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
    public int getValueVar() {
        return valueVar;
    }
    public void setValueVar(int valueVar) {
        this.valueVar = valueVar;
    }
    @Override
    public boolean canThrowException() {
        return true;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Arrays.asList(baseVar, propertyVar, valueVar);
    }
    @Override
    public void apply(StatementVisitor v) {
        v.caseWriteProperty(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseWriteProperty(this, arg);
    }
}
