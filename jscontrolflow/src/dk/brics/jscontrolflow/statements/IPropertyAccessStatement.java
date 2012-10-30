package dk.brics.jscontrolflow.statements;

import dk.brics.jscontrolflow.IStatement;

/**
 * Superinterface for {@link WriteProperty}, {@link ReadProperty}, {@link DeleteProperty}, {@link CallProperty}.
 */
public interface IPropertyAccessStatement extends IStatement {
    int getBaseVar();
    int getPropertyVar();
}
