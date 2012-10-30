package dk.brics.jspointers.dataflow;

public class DeleteNode extends StubNode implements IPropertyAccessFlowNode {
    public DeleteNode() {
        super(true);
    }
    @Override
    public InputPoint getBase() {
        return getInput();
    }
}
