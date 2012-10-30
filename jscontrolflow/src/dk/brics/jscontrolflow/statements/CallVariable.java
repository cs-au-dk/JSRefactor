package dk.brics.jscontrolflow.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * v<sub>result</sub> = <i>var</i> (v<sub>arg1</sub>, ..., v<sub>argn</sub>).
 * <p/>
 * Read the contents of <i>var</i> and invoke it as a function.
 * If <i>var</i> was resolved by a <tt>with</tt> statement, send the <tt>with</tt>
 * statement's object argument as <tt>this</tt> argument, and otherwise use the global object.
 * <p/>
 * Example: 
 * <pre>
 * function Foo() {...}
 * var x = {foo:Foo};
 * with (x) {
 *   foo(); // x is sent as 'this' argument
 * }
 * </pre>
 */
public class CallVariable extends InvokeStatement implements IVariableAccessStatement {
    private String varName;
    private Scope scope;
    private List<Integer> arguments = new ArrayList<Integer>();

    public CallVariable(int resultVar, String varName, Scope scope) {
        super(resultVar);
        this.varName = varName;
        this.scope = scope;
    }

    public String getVarName() {
        return varName;
    }
    public void setVarName(String varName) {
        this.varName = varName;
    }
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public List<Integer> getArguments() {
        return arguments;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return arguments;
    }

    @Override
    public boolean canThrowException() {
        return true;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseCallVariable(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseCallVariable(this, arg);
    }

}
