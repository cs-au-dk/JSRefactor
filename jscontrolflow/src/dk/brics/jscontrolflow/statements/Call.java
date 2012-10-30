package dk.brics.jscontrolflow.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * v<sub>result</sub> = v<sub>func</sub>(v<sub>arg1</sub>, ..., v<sub>argn</sub>).
 * <p/>
 * Send the global object as <tt>this</tt> argument.
 */
public class Call extends InvokeStatement {
    private int funcVar;
    private List<Integer> arguments = new ArrayList<Integer>();

    public Call(int resultVar, int funcVar) {
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
    public Collection<Integer> getReadVariables() {
        Set<Integer> set = new HashSet<Integer>(arguments);
        set.add(funcVar);
        return set;
    }

    @Override
    public boolean canThrowException() {
        return true;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseCall(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseCall(this, arg);
    }

}
