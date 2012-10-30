package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jspointers.parametric.AllocSite;

public class AllocNode extends FlowNode {
    private AllocSite allocationSite;
    private InputPoint functionInstance = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private PrototypeKind prototypeKind;
    
    /**
     * What type of prototype to set on the newly allocated object.
     */
    public enum PrototypeKind {
    	NONE,
    	OBJECT,
    	REGEXP,
    	ARRAY,
    }
    
    public AllocNode(AllocSite allocationSite, PrototypeKind proto) {
        this.allocationSite = allocationSite;
        this.prototypeKind = proto;
    }
    
    public PrototypeKind getPrototypeKind() {
		return prototypeKind;
	}

    /**
     * The {@link CallNode} or {@link NewObjectNode}.
     */
    public AllocSite getAllocationSite() {
        return allocationSite;
    }

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }

    public OutputPoint getResult() {
        return result;
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
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseAlloc(this);
    }
}
