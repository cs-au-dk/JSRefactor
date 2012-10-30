package dk.brics.jscontrolflow.statements;

/**
 * Common superclass for {@link ReadVariable}, {@link ReadProperty}.
 * <p/>
 * This class exists to strengthen the relation between AST and CFG.
 */
public abstract class ReadStatement extends Assignment {
    public ReadStatement(int resultVar) {
        super(resultVar);
    }
}
