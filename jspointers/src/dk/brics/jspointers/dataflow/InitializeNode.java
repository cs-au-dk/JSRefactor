package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

public class InitializeNode extends FlowNode {

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseInitialize(this);
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.emptyList();
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }

}
