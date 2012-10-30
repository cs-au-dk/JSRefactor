package dk.brics.jscontrolflow.statements;

import dk.brics.jscontrolflow.IStatement;
import dk.brics.jscontrolflow.scope.Scope;

/**
 * Common superinterface for {@link ReadVariable}, {@link WriteVariable}, {@link CallVariable}.
 */
public interface IVariableAccessStatement extends IStatement {
    Scope getScope();
    String getVarName();
}
