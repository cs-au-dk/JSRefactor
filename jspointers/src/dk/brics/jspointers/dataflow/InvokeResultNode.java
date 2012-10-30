package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.List;

public class InvokeResultNode extends FlowNode {
    private InputPoint func = new InputPoint(this);
    private InputPoint allocatedObject = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private OutputPoint exceptionalResult = new OutputPoint(this);
    private boolean isConstructor;
    private IInvocationFlowNode invocation;

    public InvokeResultNode(boolean isConstructor, IInvocationFlowNode invocation) {
        this.isConstructor = isConstructor;
        this.invocation = invocation;
    }

    public IInvocationFlowNode getInvocation() {
        return invocation;
    }

    /**
     * Shorthand for <tt>getInvocation().getCallsiteId()</tt>.
     */
    public Object getCallsiteId() {
        return invocation.getCallsiteId();
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    /**
     * For constructor calls, this is the object was allocated by the call.
     * Unused for non-constructor calls (but is not null).
     */
    public InputPoint getAllocatedObject() {
        return allocatedObject;
    }
    public InputPoint getFunc() {
        return func;
    }
    public OutputPoint getExceptionalResult() {
        return exceptionalResult;
    }
    public OutputPoint getResult() {
        return result;
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseInvokeResult(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(func, allocatedObject);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Arrays.asList(result, exceptionalResult);
    }
}
