package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * base[property] = value;
 * 
 * @author Asger
 *
 */
public class StoreDynamicNode extends FlowNode implements IStoreFlowNode, IDynamicPropertyAccessFlowNode {
    private InputPoint base = new InputPoint(this);
    private InputPoint property = new InputPoint(this);
    private InputPoint value = new InputPoint(this);

    public InputPoint getBase() {
        return base;
    }
    public InputPoint getProperty() {
        return property;
    }
    @Override
	public InputPoint getValue() {
        return value;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseStoreDynamic(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(base, property, value);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }

}
