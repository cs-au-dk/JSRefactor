package dk.brics.jspointers.dataflow;

public interface IDynamicPropertyAccessFlowNode extends IPropertyAccessFlowNode {
    InputPoint getProperty();
}
