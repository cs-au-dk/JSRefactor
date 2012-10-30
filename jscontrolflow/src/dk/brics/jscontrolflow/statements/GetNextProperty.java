package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

/**
 * v<sub>result</sub> = name of some enumerable property of v<sub>object</sub>.
 * 
 * @author Asger
 */
public class GetNextProperty extends Assignment {
    private int objectVar;

    public GetNextProperty(int resultVar, int objectVar) {
        super(resultVar);
        this.objectVar = objectVar;
    }
    public int getObjectVar() {
        return objectVar;
    }
    public void setObjectVar(int objectVar) {
        this.objectVar = objectVar;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(objectVar);
    }

    @Override
    public boolean canThrowException() {
        return true; // XXX if objectVar is null, though it can't really be null can it?
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseGetNextProperty(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseGetNextProperty(this, arg);
    }
}
