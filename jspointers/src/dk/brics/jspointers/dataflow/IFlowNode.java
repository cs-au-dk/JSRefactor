package dk.brics.jspointers.dataflow;

import java.util.List;

public interface IFlowNode {

    public abstract void apply(FlowNodeVisitor visitor);

    /**
     * Returns the list of input points to this flow node.
     * Used mostly for debugging and visualization tools.
     * @return unmodifiable list
     */
    public abstract List<InputPoint> getInputPoints();

    public abstract List<OutputPoint> getOutputPoints();
    
    /**
     * Returns true if the flow node's only purpose is to modify its own output points.
     * @return true if definitely local, false otherwise
     */
    public abstract boolean isPurelyLocal();

}