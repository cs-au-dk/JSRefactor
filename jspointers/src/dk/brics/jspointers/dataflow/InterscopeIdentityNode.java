package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

/**
 * Reads the value of an input point in the flow node of an enclosing function's dataflow graph.
 * This is used to make the receiver object of a <tt>with</tt> statement available to inner functions.
 */
public class InterscopeIdentityNode extends FlowNode {
    private OutputPoint result = new OutputPoint(this);
    private InputPoint functionInstance = new InputPoint(this);
    private InputPoint foreignInputPoint;
    private int depth;

    public InterscopeIdentityNode(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Interscope identity node requires depth >= 1");
        }
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public OutputPoint getResult() {
        return result;
    }
    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public InputPoint getForeignInputPoint() {
        return foreignInputPoint;
    }
    public void setForeignInputPoint(InputPoint foreignInputPoint) {
        this.foreignInputPoint = foreignInputPoint;
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }
    
    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(functionInstance);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseInterscopeIdentity(this);
    }
}
