package dk.brics.jspointers.analysis;

import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.lattice.contexts.Context;

/**
 * Defines a strategy for transferring invocations. In particular, different types of context sensitivity
 * may be implemented with this interface.
 * <p/>
 * The interface allows for wide range of context sensitivities, but in return, their
 * implementations may suffer from a bit of code duplication, due to this level of generality.
 * It is attempted to minimize the code duplication as much as possible by putting commonly used
 * operations in the {@link LatticeUtil} class.
 * 
 * @author Asger
 */
public interface InvocationStrategy {
    //	void transferInvoke(InvokeNode node, Context context, FunctionValue target, LatticeUtil lattice);
    //	void transferInvokeResult(InvokeResultNode node, Context context, FunctionValue target, LatticeUtil lattice);

    //	void transferInvoke(Object callsite, Set<Value> thisArgs, List<Set<Value>> arguments, Context context, FunctionValue target, LatticeUtil lattice);
    //	void transferInvokeResult(Object callsite, Context context, FunctionValue target, LatticeUtil lattice, OutputPoint result, OutputPoint exceptionalResult);
    void transferInvoke(InvokeNode node, Context context, LatticeUtil lattice);
    void transferLoadAndInvoke(LoadAndInvokeNode node, Context context, LatticeUtil lattice);
    void transferInvokeResult(InvokeResultNode node, Context context, LatticeUtil lattice);
}
