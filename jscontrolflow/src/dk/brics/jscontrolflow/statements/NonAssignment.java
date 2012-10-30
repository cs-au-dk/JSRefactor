package dk.brics.jscontrolflow.statements;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.Statement;

/**
 * Superclass for statements that do not modify temporary variables.
 */
public abstract class NonAssignment extends Statement {

    @Override
    public final List<Integer> getAssignedVariables() {
        return Collections.emptyList();
    }

}
