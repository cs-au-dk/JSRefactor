package dk.brics.jspointers.dataflow;

import java.util.List;

public interface IInvocationFlowNode extends IFlowNode {
    Object getCallsiteId();
    List<InputPoint> getArguments();
    InputPoint getBase();
    boolean isConstructor();
    boolean isThisArgOmitted();
}
