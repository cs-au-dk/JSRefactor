package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * base.prty = y; [only if base.prty already exists]
 */
public class StoreIfPresentNode extends FlowNode implements IStoreFlowNode {
    private InputPoint base = new InputPoint(this);
    private InputPoint value = new InputPoint(this);
    private String property;

    public StoreIfPresentNode(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
    @Override
    public InputPoint getBase() {
        return base;
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
        visitor.caseStoreIfPresent(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(base, value);
    }

    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
