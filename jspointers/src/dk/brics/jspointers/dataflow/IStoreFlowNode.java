package dk.brics.jspointers.dataflow;

/**
 * Superinterface for {@link StoreNode}, {@link StoreDynamicNode}, {@link StoreIfPresentNode}.
 */
public interface IStoreFlowNode extends IPropertyAccessFlowNode {
	public abstract InputPoint getValue();
}
