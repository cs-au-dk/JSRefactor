package dk.brics.jspointers.analysis;

import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.lattice.contexts.Context;

/**
 * Immutable pair of a {@link FlowNode} and a {@link Context}.
 * 
 * @author Asger
 */
public final class FlowNodeContextPair extends TransferNode {
    private final FlowNode flowNode;
    private final Context context;

    public FlowNodeContextPair(FlowNode flowNode, Context context) {
        assert flowNode != null : "FlowNode was null";
        assert context != null : "Context was null";
        this.flowNode = flowNode;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    public FlowNode getFlowNode() {
        return flowNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context.hashCode();
        result = prime * result + flowNode.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FlowNodeContextPair other = (FlowNodeContextPair) obj;
        if (!context.equals(other.context)) {
            return false;
        }
        if (!flowNode.equals(other.flowNode)) {
            return false;
        }
        return true;
    }
}
