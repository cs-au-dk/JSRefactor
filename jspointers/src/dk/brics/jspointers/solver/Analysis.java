package dk.brics.jspointers.solver;

public interface Analysis<TransferNode, Key, Value> {
    Value emptyValue();
    Value bottom(Key key);

    Iterable<? extends TransferNode> initialNodes();

    void transfer(TransferNode t, Callback<Key,Value> callback);

    /**
     * Returns the transfer nodes that initially depend on the specified key.
     * <p/>
     * The resulting transfer nodes should be iterated in prioritized order,
     * with the highest-priority nodes first.
     * @param key a key
     * @return transfer node, may contain duplicates, order is irrelevant
     */
    Iterable<? extends TransferNode> getInitialDependencies(Key key);

    Value clone(Value value);
}
