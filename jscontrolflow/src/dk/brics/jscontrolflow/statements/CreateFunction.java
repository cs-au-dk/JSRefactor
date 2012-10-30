package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.scope.Scope;

/**
 * v<sub>result</sub> = a new instance of the given function. The current scope becomes the enclosing scope (or closure)
 * of the new function instance.
 * <p/>
 * Note that function <i>declarations</i> have their function instance created at the top of a function
 * before its actual body begins (and therefore in the function's scope), while function <i>expressions</i>
 * create a new function every time they get evaluated. The difference between these is encoded in the
 * control-flow graph, and is transparent to the semantics of {@link CreateFunction}. 
 */
public class CreateFunction extends Assignment {
    private Function function;
    private Scope scope;

    public CreateFunction(int resultVar, Function function, Scope scope) {
        super(resultVar);
        this.function = function;
        this.scope = scope;
    }
    
    public Scope getScope() {
      return scope;
    }
    public void setScope(Scope scope) {
      this.scope = scope;
    }
    
    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public boolean canThrowException() {
        return false; // yes, this can NOT throw exceptions
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseCreateFunction(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseCreateFunction(this, arg);
    }
}
