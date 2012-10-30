package dk.brics.jspointers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.OutputPoint;

public class DataflowUtil {
    /**
     * Returns the set of flow nodes reachable from the given node.
     * Includes the argument node itself.
     * @param root the initial flow node
     * @return a newly created set
     */
    public static Set<FlowNode> getReachableFrom(FlowNode root) {
        Set<FlowNode> set = new HashSet<FlowNode>();
        LinkedList<FlowNode> queue = new LinkedList<FlowNode>();
        set.add(root);
        queue.add(root);
        while (!queue.isEmpty()) {
            FlowNode node = queue.removeFirst();
            for (OutputPoint op : node.getOutputPoints()) {
                for (InputPoint ip : op.getDestinations()) {
                    if (set.add(ip.getFlowNode())) {
                        queue.add(ip.getFlowNode());
                    }
                }
            }
        }
        return set;
    }

}
