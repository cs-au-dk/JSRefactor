package dk.brics.jscontrolflow.statements;

/**
 * Common superclass for {@link WriteVariable}, {@link WriteProperty} and {@link WriteProperty}.
 * <p/>
 * This class exists to strengthen the relation between AST and CFG.
 */
public abstract class WriteStatement extends NonAssignment {
	public abstract int getValueVar();
}
