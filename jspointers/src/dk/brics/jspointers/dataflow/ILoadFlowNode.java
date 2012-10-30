package dk.brics.jspointers.dataflow;

/**
 * Common superinterface for {@link LoadNode}, {@link LoadDynamicNode}, and {@link LoadAndInvokeNode}.
 */
public interface ILoadFlowNode extends IPropertyAccessFlowNode {
    OutputPoint getResult();
}
