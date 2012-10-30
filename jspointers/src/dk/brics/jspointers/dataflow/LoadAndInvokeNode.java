package dk.brics.jspointers.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadAndInvokeNode extends FlowNode implements IInvocationFlowNode, ILoadFlowNode {
    private InputPoint base = new InputPoint(this);
    private OutputPoint invokedFunction = new OutputPoint(this);
    private List<InputPoint> arguments;
    private InvokeResultNode resultNode; // TODO: Remove this reference??
    private String property;
    private Object callsiteId;

    public LoadAndInvokeNode(String property, int numArguments, Object callsiteId) {
        this.callsiteId = callsiteId;
        this.property = property;
        this.arguments = new ArrayList<InputPoint>();
        for (int i=0; i<numArguments; i++) {
            arguments.add(new InputPoint(this));
        }
    }

    public OutputPoint getInvokedFunction() {
        return invokedFunction;
    }
    public Object getCallsiteId() {
        return callsiteId;
    }

    public String getProperty() {
        return property;
    }
    public InputPoint getBase() {
        return base;
    }
    public List<InputPoint> getArguments() {
        return arguments;
    }

    public InvokeResultNode getResultNode() {
        return resultNode;
    }
    public void setResultNode(InvokeResultNode resultNode) {
        this.resultNode = resultNode;
    }

    @Override
    public boolean isConstructor() {
        return false;
    }
    @Override
    public boolean isThisArgOmitted() {
        return false;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        List<InputPoint> ips = new ArrayList<InputPoint>();
        ips.add(base);
        ips.addAll(arguments);
        return ips;
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseLoadAndInvoke(this);
    }
    
    @Override
    public OutputPoint getResult() {
        return resultNode.getResult();
    }

}
