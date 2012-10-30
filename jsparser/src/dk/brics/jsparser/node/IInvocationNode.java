package dk.brics.jsparser.node;

import java.util.LinkedList;

/**
 * An {@link AInvokeExp} or {@link ANewExp}.
 */
public interface IInvocationNode extends NodeInterface {
    PExp getFunctionExp();
    LinkedList<PExp> getArguments();
}
