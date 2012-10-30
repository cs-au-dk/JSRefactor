package dk.brics.jspointers.analysis;

public class InvokeDOMFunctionTransfer extends TransferNode {

    public static final InvokeDOMFunctionTransfer Instance = new InvokeDOMFunctionTransfer();

    private InvokeDOMFunctionTransfer() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return 929284;
    }

}
