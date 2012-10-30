package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

public class PlusNode extends FlowNode {
    private InputPoint argument = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);

    public InputPoint getArgument() {
        return argument;
    }
    public OutputPoint getResult() {
        return result;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.casePlus(this);
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(argument);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
