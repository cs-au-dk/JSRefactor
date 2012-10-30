package dk.brics.jscontrolflow.scope;

import java.util.Collections;
import java.util.Set;


public class CatchScope extends Scope {
    private String varName;

    public CatchScope(Scope parentScope, String varName) {
        super(parentScope);
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public Set<String> getDeclaredVariables() {
        return Collections.singleton(varName);
    }

    @Override
    public boolean isGlobal() {
    	return false;
    }
}
