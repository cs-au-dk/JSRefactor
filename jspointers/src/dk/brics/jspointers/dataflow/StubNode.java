package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

/**
 * A node that does nothing with its input. It does not directly
 * represent a constraint, but exists for technical reasons since an
 * {@link InputPoint} cannot exist without a {@link FlowNode}.
 * <p/>
 * It is typically the target of an {@link InterscopeIdentityNode}.
 */
public class StubNode extends FlowNode {
    private InputPoint input = new InputPoint(this);
    private boolean isLocal;
    
    /**
     * Creates a new stub node. The argument determines if it should be considered
     * purely local.
     * @param isLocal false if this node's input point can be accessed as a foreign input point
     */
	public StubNode(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public InputPoint getInput() {
        return input;
    }

    @Override
    public boolean isPurelyLocal() {
        return isLocal;
    }
    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseStub(this);
    }
    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(input);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
