package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.Function;

public class FunctionInstanceNode extends FlowNode {
    private OutputPoint result = new OutputPoint(this);
    private Function function;

    public FunctionInstanceNode(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    public OutputPoint getResult() {
        return result;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.emptyList();
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseFunctionInstance(this);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
