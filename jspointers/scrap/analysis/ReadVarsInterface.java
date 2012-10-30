package dk.brics.jspointers.flowgraph.analysis;

import dk.brics.tajs.flowgraph.Node;

/**
 * Determines for a node what relevant variables it may use.
 */
public interface ReadVarsInterface {
	/**
	 * Returns an array with the variables read by the specified node.
	 * @param node a statement
	 * @return not null. The result may not be modified.
	 */
	int[] getReadVars(Node node);
}
