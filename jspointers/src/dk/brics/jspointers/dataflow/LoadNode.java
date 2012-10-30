package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

/**
 * x = base.prty
 */
public class LoadNode extends FlowNode implements ILoadFlowNode {
    private InputPoint base = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private String property;

    public LoadNode(String property) {
        this.property = property;
    }

    public OutputPoint getResult() {
        return result;
    }
    public InputPoint getBase() {
        return base;
    }
    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseLoad(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(base);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
