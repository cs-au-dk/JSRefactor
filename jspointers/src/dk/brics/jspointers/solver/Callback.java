package dk.brics.jspointers.solver;

/**
 * Callback provided to transfer functions in the {@link Analysis} interface.
 * These methods provide the means to read and write to specific lattice points.
 * <p/>
 * See {@link Solver} for a description of the algorithm.
 * 
 * @author Asger
 *
 * @param <Key> the lattice's key type
 * @param <Value> the lattice's value type
 */
public interface Callback<Key, Value> {
    /**
     * Signals that the value associated with the specified key has been modified
     * during this transfer call. This method may be called multiple times with the
     * same key; it has the same effect as calling it just once.
     * @param k a key; not null
     */
    void markChanged(Key k);

    /**
     * Returns the value of the given key that may be read from.
     * <p/>
     * The reference returned by this method may not be used for modification.
     * One must acquire a reference using {@link #modifiableValueAt} to
     * modify that value.
     * @param k a key; not null
     * @return a value; not null
     */
    Value readableValueAt(Key k);

    /**
     * Returns the value of the given key. The value object may be modified
     * by the transfer function.
     * If the value changes as a result of modifications, {@link #markChanged}
     * <i>must</i> be called on the key before the transfer function completes
     * (and <i>only</i> if the value actually changed!).
     * <p/>
     * Information read from the value may not be used in a way that has significance
     * for how any value is modified by the transfer function. One must acquire
     * a reference using {@link #readableValueAt} to do that.
     * @param k a key; not null
     * @return a value; not null
     */
    Value modifiableValueAt(Key k);
}
