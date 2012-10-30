package dk.brics.jspointers.solver;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import dk.brics.jsutil.MultiMap;


/**
 * Implements a worklist algorithm for finding the least-fixed point of a set of constraints
 * on a map lattice. An instanceof {@link Analysis} embodies the lattice definition and
 * constraints of the system to solve.
 * <p/>
 * The lattice is <tt>Key -&gt; Value</tt>. The constraints are of type <tt>TransferNode</tt>;
 * they are called as such because they are implemented as transfer functions. We shall
 * use the term <i>transfer function</i> instead of constraint from here on.
 * <p/>
 * <tt>Value</tt>s are mutable objects that are modified by the transfer functions.
 * <tt>Key</tt>s and <tt>TransferNode</tt>s on the other hand, should remain unmodified
 * (as seen by {@link #equals(Object)} and {@link #hashCode()}).
 * <p/>
 * Each transfer function <i>depends</i> on a set of keys; namely the keys whose value is relevant for the constraint.
 * These dependencies may change throughout the search for a fixed point, and the design ensures 
 * that these dependencies are automatically discovered based on the behaviour of the transfer functions.
 * The solver maintains these dependencies explicitly with a dependency graph.
 * <p/>
 * Conceptually, each transfer node has a set of keys which are its <i>initial dependencies</i>.
 * If all keys in its initial dependencies are the <i>bottom</i> element, then the transfer function has
 * no effect.
 * The {@link Analysis} instance must for any key provide the set of transfer nodes that has it as an initial dependency. 
 * This is how transfer nodes get added to the worklist for the first time.
 * <p/>
 * Transfer functions access the lattice by means of the {@link Callback} interface.
 * When it reads the value from a key using {@link Callback#readableValueAt}, it adds a dependency
 * edge between the transfer node and the key it read from. When it calls {@link Callback#markChanged}
 * to signal that a value has changed, all transfer nodes that depend on the modified key gets added
 * to the worklist. This maintains the following invariant:
 * <blockquote>
 * For all transfer nodes N:
 * <ul>
 * <li>N in the worklist; or
 * <li>N has a correct set of dependencies in the dependency graph
 * </ul>
 * </blockquote>
 * The invariant is crucial to the correctness of the algorithm. When something changes on
 * the lattice, we have no <i>a priori</i> guarantee that the dependency graph remains correct.
 * But because a transfer function is assumed to be consistent, it can only find new dependencies
 * if something it reads is different from last time it was executed. If something was different
 * from last time it executed, it <i>will</i> be on the worklist because everything it read from
 * last time is reflected by the dependency graph. Since it is on the worklist, it will eventually
 * get executed, and the dependency graph updated based on its new behaviour.
 * 
 * @author Asger
 *
 * @param <TransferNode>
 * @param <Key>
 */
public class Solver<TransferNode, Key> {

    private final class PriorityComparator implements Comparator<TransferNode> {
        public int compare(TransferNode t1, TransferNode t2) {
            return transfer2priority.get(t1).compareTo(transfer2priority.get(t2));
        }
    }

    private Map<TransferNode,Integer> transfer2priority = new HashMap<TransferNode, Integer>();
    private PriorityQueue<TransferNode> queue = new PriorityQueue<TransferNode>(128, new PriorityComparator());
    private Set<TransferNode> inqueue = new HashSet<TransferNode>();
    private int nextPriority = 1;

    private <Value> AnalysisResult<Key,Value> solvex(final Analysis<TransferNode, Key, Value> analysis) {
        final AnalysisResult<Key,Value> result = new AnalysisResult<Key, Value>(analysis.emptyValue());
        final MultiMap<Key, TransferNode> dependencies = new MultiMap<Key, TransferNode>();
        for (TransferNode t : analysis.initialNodes()) {
            enqueue(t);
        }
        class CallbackClass implements Callback<Key,Value> {
            TransferNode current;
            Set<Key> modified = new HashSet<Key>(); // TODO: Benchmark this and possibly find a more efficient approach to cloning
            Set<Key> read = new HashSet<Key>();
            @Override
            public void markChanged(Key k) {
                assert result.containsKey(k) : "cannot mark updated if value was never accessed";
                for (TransferNode t : dependencies.getView(k)) {
                    enqueue(t);
                }
            }
            private Value getValue(Key k) {
                Value value = result.get(k);
                if (value == null) {
                    value = analysis.bottom(k);
                    assert value != null : "null may not be returned by bottom()";
                    result.put(k, value);
                    for (TransferNode t : analysis.getInitialDependencies(k)) {
                        dependencies.add(k, t);
                    }
                }
                return value;
            }
            @Override
            public Value readableValueAt(Key k) {
                dependencies.add(k, current);
                if (!read.add(k) || !modified.contains(k)) {
                    return getValue(k);
                } else {
                    // value was previously returned for modification
                    // clone value and return that
                    // result.get(k) cannot return null because it would have
                    // been put there by the preceeding call to modifiableValueAt
                    return analysis.clone(result.get(k));
                }
            }
            @Override
            public Value modifiableValueAt(Key k) {
                if (!modified.add(k) || !read.contains(k)) {
                    return getValue(k);
                } else {
                    // value was previously returned for reading
                    // clone value and substitute that in the map
                    Value clone = analysis.clone(result.get(k));
                    result.put(k, clone);
                    return clone;
                }
            }
        }
        CallbackClass callback = new CallbackClass();
        while (!queue.isEmpty()) {
            TransferNode t = queue.remove();
            inqueue.remove(t);
            callback.current = t;
            callback.modified.clear();
            callback.read.clear();
            analysis.transfer(t, callback);
        }
        return result;
    }

    private void enqueue(TransferNode t) {
        if (!inqueue.add(t)) {
            return;
        }
        if (!transfer2priority.containsKey(t)) {
            transfer2priority.put(t, nextPriority++);
        }
        queue.add(t);
    }

    public static <TransferNode, Key, Value> AnalysisResult<Key,Value> solve(Analysis<TransferNode, Key, Value> analysis) {
        return new Solver<TransferNode,Key>().solvex(analysis);
    }

}
