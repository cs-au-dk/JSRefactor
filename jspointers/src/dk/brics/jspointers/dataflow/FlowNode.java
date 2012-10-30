package dk.brics.jspointers.dataflow;

import java.util.List;

/**
 * A node in the dataflow graph. A FlowNode has a set of <i>input points</i> and a set of <i>output point</i>.
 * FlowNodes can be connected by adding dataflow edges between their input points and output points. 
 * Only FlowNodes belonging to the same function may be connected by dataflow edges.
 * <p/>
 * We use the term <i>foreign input point</i> when a flow node refers to the input point of another
 * flow node without necessarily being from the same function or execution context. (See {@link InterscopeIdentityNode})
 * <p/>
 * In constraint terminology, input points and output points represent variables, and flow nodes represent
 * complex constraints. Dataflow edges represent subset constraints. A flow node's constraint may depend on its
 * input and output points, and on zero or more <i>heap</i> variables. It must the case that the constraint is
 * satisfied if all of its input points contain empty sets. There are some exceptions to this, such as {@link FunctionInstanceNode}
 * which depends on interprocedural information (it has no input points). In a context-sensitive analysis, each
 * flow node and dataflow edge represents one such constraint for every context, and input points and
 * output points have a variable for every context.
 * <p/>
 * Some dataflow edges are labelled as <i>postfix edges</i>, indicating that they have no effect on the heap
 * or any interprocedural dataflow.
 * They can be ignored until after a fixpoint has been found which describes the abstract heap and call-graph.
 * (This is sometimes called a <i>stratification</i> - dividing the analysis steps that don't require reiteration).
 * <p/>
 */
public abstract class FlowNode implements IFlowNode {
    @Override
    public abstract void apply(FlowNodeVisitor visitor);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the list of input points to this flow node.
     * Used mostly for debugging and visualization tools.
     * @return unmodifiable list
     */
    @Override
    public abstract List<InputPoint> getInputPoints();

    @Override
    public abstract List<OutputPoint> getOutputPoints();

    @Override
    public abstract boolean isPurelyLocal();
}
