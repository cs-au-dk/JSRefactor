package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

/**
 * <tt>value.toString()</tt> or <tt>value.valueOf()</tt>.
 * 
 * @author Asger
 */
public class CoerceToPrimitive extends FlowNode {
    private InputPoint value = new InputPoint(this);
    private OutputPoint exceptionalResult = new OutputPoint(this);

    public InputPoint getValue() {
        return value;
    }
    public OutputPoint getExceptionalResult() {
        return exceptionalResult;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(value);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(exceptionalResult);
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseCoerceToPrimitive(this);
    }
}
