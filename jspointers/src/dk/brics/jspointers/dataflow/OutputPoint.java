package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.OutputPointKey;

public final class OutputPoint {
    final Set<InputPoint> destinations = new HashSet<InputPoint>();
    final Set<InputPoint> postfixDestinations = new HashSet<InputPoint>();
    private final Set<InputPoint> destinationsUnm = Collections.unmodifiableSet(destinations);
    private final Set<InputPoint> postfixDestinationsUnm = Collections.unmodifiableSet(postfixDestinations);
    private final FlowNode flow;

    public OutputPoint(FlowNode flow) {
        this.flow = flow;
    }

    public FlowNode getFlowNode() {
        return flow;
    }

    public Set<InputPoint> getDestinations() {
        return destinationsUnm;
    }
    public Set<InputPoint> getPostfixDestinations() {
    	return postfixDestinationsUnm;
    }
    
    public void clearDestinations() {
        while (!destinations.isEmpty()) {
            destinations.iterator().next().removeSource(this);
        }
        while (!postfixDestinations.isEmpty()) {
        	postfixDestinations.iterator().next().removeSource(this);
        }
    }

	public OutputPointKey getKey(Context context) {
		return new OutputPointKey(this, context);
	}
}
