package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * result = base[property];
 * 
 * @author Asger
 *
 */
public class LoadDynamicNode extends FlowNode implements ILoadFlowNode, IDynamicPropertyAccessFlowNode {
    private OutputPoint result = new OutputPoint(this);
    private InputPoint base = new InputPoint(this);
    private InputPoint property = new InputPoint(this);

    public InputPoint getBase() {
        return base;
    }
    public InputPoint getProperty() {
        return property;
    }
    public OutputPoint getResult() {
        return result;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseLoadDynamic(this);
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(base, property);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
