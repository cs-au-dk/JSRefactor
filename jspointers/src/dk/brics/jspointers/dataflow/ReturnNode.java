package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.Function;

/**
 * A return node; either exceptional or normal. Each function has a unique
 * normal return and a unique exceptional return.
 * 
 * @author Asger
 */
public class ReturnNode extends FlowNode {
    private InputPoint value = new InputPoint(this);
    private Function func;
    private boolean isExceptional;

    public ReturnNode(Function func, boolean isExceptional) {
        this.func = func;
        this.isExceptional = isExceptional;
    }

    /**
     * True if this is an exceptional return, false if it is a normal
     * return. 
     */
    public boolean isExceptional() {
        return isExceptional;
    }
    public InputPoint getValue() {
        return value;
    }
    public Function getFunction() {
        return func;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseReturn(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(value);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
