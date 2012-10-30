package dk.brics.jspointers.flowgraph.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.brics.tajs.flowgraph.BasicBlock;
import dk.brics.tajs.flowgraph.Function;
import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.flowgraph.nodes.AssignmentNode;

/**
 * For each assignment node, determines which nodes may read the variable it assigns.
 * Requires the results from the reaching definitions analysis.
 */
public class ReachesTo {
    private Map<Assignment,Set<Node>> reachesTo = new HashMap<Assignment,Set<Node>>();
    
    private Set<Node> fetch(Assignment an) {
        Set<Node> set = reachesTo.get(an);
        if (set == null) {
            set = new HashSet<Node>();
            reachesTo.put(an, set);
        }
        return set;
    }
    
    public ReachesTo(Function function, ReachingDefs reachingDefs) {
    	for (BasicBlock block : function.getBlocks()) {
    		for (Node node : block.getNodes()) {
    			int[] vars = new ReadVarsVisitor().getReadVars(node);
                for (int var : vars) {
                    Set<Assignment> assigns = reachingDefs.getReachingDefsForTempVar(node, var);
                    if (assigns == null)
                        continue;
                    for (Assignment an : assigns) {
                        fetch(an).add(node);
                    }
                }
    		}
    	}
    }
    
    /**
     * Returns the set of nodes that may read the value assigned by the given assignment node.
     * @param an an assignment node
     * @return unmodifiable set. Not null; possibly empty.
     */
    public Set<Node> getReachesTo(AssignmentNode an) {
        Set<Node> set = reachesTo.get(new TempVarAssignment(an));
        if (set == null)
            return Collections.<Node>emptySet();
        else
            return Collections.unmodifiableSet(set);
    }
}
