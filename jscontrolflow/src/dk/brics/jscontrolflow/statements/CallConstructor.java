package dk.brics.jscontrolflow.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * v<sub>result</sub> = <tt>new</tt> v<sub>func</sub>(v<sub>arg1</sub>, ..., v<sub>argn</sub>).
 * <p/>
 * Semantics:
 * <ol>
 * <li>Let <i>obj</i> be a newly created object.
 * <li>Set the internal prototype of <i>obj</i> to the
 * property named "<tt>prototype</tt>" on v<sub>func</sub>.
 * <li>Invoke v<sub>func</sub> as a function with v<sub>arg1</sub>, ..., v<sub>argn</sub> as arguments,
 * and <i>obj</i> as the <tt>this</tt> argument.
 * <li>If the function returns an object, store the returned object in v<sub>result</sub>, otherwise
 * store <i>obj</i> in v<sub>result</sub>.
 * </ol>
 */
public class CallConstructor extends InvokeStatement {
    private int funcVar;
    private List<Integer> arguments = new ArrayList<Integer>();

    public CallConstructor(int resultVar, int funcVar) {
        super(resultVar);
        this.funcVar = funcVar;
    }

    public int getFuncVar() {
        return funcVar;
    }
    public void setFuncVar(int funcVar) {
        this.funcVar = funcVar;
    }

    @Override
    public List<Integer> getArguments() {
        return arguments;
    }
    @Override
    public boolean canThrowException() {
        return true;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        Set<Integer> set = new HashSet<Integer>(arguments);
        set.add(funcVar);
        return set;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseConstructorCall(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseConstructorCall(this, arg);
    }
}
