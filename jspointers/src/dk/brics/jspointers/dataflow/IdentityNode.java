package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

/**
 * Node that copies information from its input point to its output point.
 * This node can be removed by a transitive closure, but is useful during
 * creation.
 * 
 * @author Asger
 */
public class IdentityNode extends FlowNode {
    private InputPoint value = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);

    public InputPoint getValue() {
        return value;
    }
    public OutputPoint getResult() {
        return result;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseIdentity(this);
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }
    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(value);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
