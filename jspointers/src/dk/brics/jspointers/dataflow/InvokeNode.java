package dk.brics.jspointers.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * x = thisarg.func(arg0,arg1,...);
 */
public class InvokeNode extends FlowNode implements IInvocationFlowNode {
    private InputPoint base = new InputPoint(this);
    private InputPoint func = new InputPoint(this);
    private List<InputPoint> arguments;
    private boolean isConstructor;
    private boolean isThisArgOmitted;
    private Object callsiteId;

    public InvokeNode(int numargs, boolean isConstructor, boolean isThisArgOmitted, Object callsiteId) {
        this.callsiteId = callsiteId;
        this.isConstructor = isConstructor;
        this.isThisArgOmitted = isConstructor || isThisArgOmitted;
        arguments = new ArrayList<InputPoint>();
        for (int i=0; i<numargs; i++) {
            arguments.add(new InputPoint(this));
        }
        arguments = Collections.unmodifiableList(arguments);
    }

    public Object getCallsiteId() {
        return callsiteId;
    }

    /**
     * True if the <tt>this</tt> argument was not supplied explicitly. 
     * For constructor invocations, this is <tt>true</tt> by convention.
     */
    public boolean isThisArgOmitted() {
        return isThisArgOmitted;
    }
    public boolean isConstructor() {
        return isConstructor;
    }

    /**
     * For constructor calls, this is the object that was allocated by the call.<br/>
     * For non-constructor calls with an explicit <i>this</i> argument, this is the <i>this</i> argument.<br/>
     * For non-constructor calls with omitted <i>this</i> argument, this is ignored.
     */
    public InputPoint getBase() {
        return base;
    }
    public InputPoint getFunc() {
        return func;
    }
    public List<InputPoint> getArguments() {
        return arguments;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseInvoke(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        List<InputPoint> inputPoints = new LinkedList<InputPoint>();
        inputPoints.add(base);
        inputPoints.add(func);
        inputPoints.addAll(arguments);
        return inputPoints;
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
