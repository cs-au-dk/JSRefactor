package dk.brics.jsrefactoring.hosts;

/**
 * A lexical environment or an object name.
 */
public abstract class Host {
    public abstract boolean equals(Object obj);
    public abstract int hashCode();
}
