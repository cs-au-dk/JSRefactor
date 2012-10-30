package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dk.brics.jspointers.lattice.values.Value;

public class ConstNode extends FlowNode {
    private InputPoint functionInstance = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private Value value;

    public ConstNode(Value value) {
        this.value = value;
    }

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public OutputPoint getResult() {
        return result;
    }
    public Value getValue() {
        return value;
    }

    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseConst(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(functionInstance);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
