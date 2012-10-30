package dk.brics.jscontrolflow.scope;

import java.util.Collections;
import java.util.Set;

import dk.brics.jscontrolflow.statements.EnterWith;


public class WithScope extends Scope {

    private EnterWith statement;

    public WithScope(Scope parentScope, EnterWith statement) {
        super(parentScope);
        this.statement = statement;
    }

    public EnterWith getStatement() {
        return statement;
    }

    @Override
    public Set<String> getDeclaredVariables() {
        return Collections.emptySet();
    }
    
    @Override
    public boolean isGlobal() {
    	return false;
    }
}
