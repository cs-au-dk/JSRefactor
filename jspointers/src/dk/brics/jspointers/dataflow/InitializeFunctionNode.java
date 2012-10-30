package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.Function;

/**
 * Create a new instance of a function.
 * 
 * @author Asger
 */
public class InitializeFunctionNode extends FlowNode {
    private InputPoint outerFunction = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private Function function;

    public InitializeFunctionNode(Function function) {
        this.function = function;
    }
    public OutputPoint getResult() {
        return result;
    }
    public InputPoint getOuterFunction() {
        return outerFunction;
    }
    public Function getFunction() {
        return function;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(outerFunction);
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseInitializeFunction(this);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }

}
