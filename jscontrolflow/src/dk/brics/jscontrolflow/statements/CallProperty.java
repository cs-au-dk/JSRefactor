package dk.brics.jscontrolflow.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * v<sub>result</sub> = v<sub>base</sub>[v<sub>property</sub>] (v<sub>arg1</sub>, ..., v<sub>argn</sub>).
 * <p/>
 * Semantics:
 * <ol>
 * <li>Let <i>base</i> = ToObject(v<sub>base</sub>).
 * <li>Let <i>func</i> be the value of the property named ToString(v<sub>property</sub>) on <i>base</i>.
 * <li>Invoke <i>func</i> as a function with v<sub>arg1</sub>, ..., v<sub>argn</sub> as arguments
 *     and <i>base</i> as the <tt>this</tt> argument.
 * <li>Store the returned value in v<sub>result</sub>.
 * </ol>
 */
public class CallProperty extends InvokeStatement implements IPropertyAccessStatement {
    private int baseVar;
    private int propertyVar;
    private List<Integer> arguments = new ArrayList<Integer>();

    public CallProperty(int resultVar, int baseVar, int propertyVar) {
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
    public List<Integer> getArguments() {
        return arguments;
    }
    @Override
    public Collection<Integer> getReadVariables() {
    	List<Integer> vars = new ArrayList<Integer>();
    	vars.add(baseVar);
    	vars.add(propertyVar);
    	vars.addAll(arguments);
        return vars;
    }

    @Override
    public boolean canThrowException() {
        return true;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseCallProperty(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseCallProperty(this, arg);
    }

}
