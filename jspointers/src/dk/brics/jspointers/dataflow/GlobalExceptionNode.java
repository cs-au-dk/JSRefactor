package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

public class GlobalExceptionNode extends FlowNode { // TODO: Remove this FlowNode??
    private InputPoint functionInstance = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public OutputPoint getResult() {
        return result;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseGlobalException(this);
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }
    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(functionInstance);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
