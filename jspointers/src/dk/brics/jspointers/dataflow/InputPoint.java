package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class InputPoint {
    private final Set<OutputPoint> sources = new HashSet<OutputPoint>();
    private final Set<OutputPoint> postfixSources = new HashSet<OutputPoint>();
    private final Set<OutputPoint> sourcesUnm = Collections.unmodifiableSet(sources);
    private final Set<OutputPoint> postfixSourcesUnm = Collections.unmodifiableSet(postfixSources);
    private final FlowNode flow;

    public InputPoint(FlowNode flow) {
        this.flow = flow;
    }

    public FlowNode getFlowNode() {
        return flow;
    }

    /**
     * Adds a dataflow edge from the given output point to this input point.
     * {@link OutputPoint#getDestinations()} will be updated accordingly.
     * @param point an output point
     * @return true if they were not connected already
     */
    public boolean addSource(OutputPoint point) {
    	if (postfixSources.remove(point)) {
    		point.postfixDestinations.remove(this);
    	}
        sources.add(point);
        return point.destinations.add(this);
    }
    
    /**
     * Adds a <i>postfix</i> dataflow edge from the given output point to this input point.
     * Postfix dataflow edges represent information flow that is not relevant for the heap,
     * and they can therefore be ignored during the search for a fixpoint.
     * <p/>
     * If the output point is already a normal source, nothing happens.
     * @param point an output point
     * @return true if the output point was not a source or postfix source already
     */
    public boolean addPostfixSource(OutputPoint point) {
    	if (sources.contains(point))
    		return false;
    	postfixSources.add(point);
    	return point.postfixDestinations.add(this);
    }
    
    /**
     * Removes the edge between this input point and the given output point (removes
     * both normal edges and postfix edges).
     * @param point an output point
     * @return true if an edge was removed
     */
    public boolean removeSource(OutputPoint point) {
        if (sources.remove(point)) {
        	return point.destinations.remove(this);
        }
        else if (postfixSources.remove(point)) {
        	return point.postfixDestinations.remove(this);
        }
        return false;
    }
    
    /**
     * Puts a postfix dataflow edges from the given output point to this input point.
     * If there was a normal dataflow edges already, it is replaced by the postfix edge.
     * @param point an output point
     * @return true if a postfix edge was added
     */
    public boolean putPostfixSource(OutputPoint point) {
    	if (sources.remove(point)) {
    		point.destinations.remove(this);
    	}
    	if (postfixSources.add(point)) {
    		return point.postfixDestinations.add(this);
    	}
    	return false;
    }
    
    /**
     * Removes all ingoing dataflow edges (both normal and postfix).
     */
    public void clearSources() {
        for (OutputPoint op : sources) {
            op.destinations.remove(this);
        }
        sources.clear();
    }
    
    /**
     * Converts all ingoing normal dataflow edges to postfix dataflow edges.
     */
    public void reduceAllSourcesToPostfixSources() {
    	for (OutputPoint op : sources) {
    		postfixSources.add(op);
    		op.postfixDestinations.add(this);
    		op.destinations.remove(this);
    	}
    	sources.clear();
    }
    
    /**
     * Returns the set of output points from which there is a dataflow edge (which is not a <i>postfix</i> edge).
     * @return unmodifiable set
     */
    public Set<OutputPoint> getSources() {
        return sourcesUnm;
    }
    
    /**
     * Returns the set of output points from which there is a <i>postfix</i> dataflow edge.
     * @return unmodifiable set
     */
    public Set<OutputPoint> getPostfixSources() {
		return postfixSourcesUnm;
	}
    
}
