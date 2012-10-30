package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

public class NewRegexp extends Assignment {
    private String regexp;

    public NewRegexp(int resultVar, String regexp) {
        super(resultVar);
        this.regexp = regexp;
    }
    public String getRegexp() {
        return regexp;
    }
    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public boolean canThrowException() {
        return true; // in case of syntax error?
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseNewRegexp(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseNewRegexp(this, arg);
    }
}
