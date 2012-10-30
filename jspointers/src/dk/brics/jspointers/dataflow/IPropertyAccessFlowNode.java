package dk.brics.jspointers.dataflow;

/**
 * Superinterface for the flow nodes that read or write properties:
 * {@link LoadNode}, {@link LoadDynamicNode}, {@link LoadAndInvokeNode} , {@link StoreNode}, {@link StoreIfPresentNode}, {@link StoreDynamicNode}
 */
public interface IPropertyAccessFlowNode extends IFlowNode {
    InputPoint getBase();
}
