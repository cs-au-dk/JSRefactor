package dk.brics.jsutil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A bijective map.
 * The map from A to B is called the <em>forward</em> map,
 * and the map from B to A is called the <em>backward</em> map.
 * <p/>
 * A and B must have proper {@link #hashCode()} and {@link #equals(Object)} methods.
 * 
 * @author Asger
 *
 * @param <A> first key type
 * @param <B> second key type
 */
public class BijectiveMap<A,B> {

    private Map<A,B> forward = new HashMap<A,B>();
    private Map<B,A> backward = new HashMap<B,A>();

    public B getForward(A a) {
        return forward.get(a);
    }
    public A getBackward(B b) {
        return backward.get(b);
    }

    /**
     * Sets a mapping in the bijection. 
     * Mappings that already exist for <tt>a</tt> and/or <tt>b</tt> are removed
     * to maintain the bijective property.
     */
    public void put(A a, B b) {
        B oldB = forward.put(a, b);
        if (oldB == b) {
            return;
        }
        if (oldB != null) {
            backward.remove(oldB);
        }
        A oldA = backward.put(b, a);
        if (oldA != null) {
            forward.remove(oldA);
        }
    }

    /**
     * A restricted version of {@link #put(Object, Object)} that throws an exception
     * if <tt>a</tt> and/or <tt>b</tt> already were part of a mapping (even if they
     * were mapped to each other).
     */
    public void add(A a, B b) {
        if (forward.containsKey(a)) {
            throw new IllegalArgumentException("First key already has a mapping");
        }
        if (backward.containsKey(b)) {
            throw new IllegalArgumentException("Second key already has a mapping");
        }
        forward.put(a, b);
        backward.put(b, a);
    }

    public B removeA(A a) {
        B b = forward.remove(a);
        if (b != null) {
            backward.remove(b);
        }
        return b;
    }
    public A removeB(B b) {
        A a = backward.remove(b);
        if (a != null) {
            forward.remove(a);
        }
        return a;
    }

    public Set<A> keySetA() {
        return Collections.unmodifiableSet(forward.keySet()); // prevent remove() operations on the keyset
    }
    public Set<B> keySetB() {
        return Collections.unmodifiableSet(backward.keySet());
    }

    /**
     * Returns the number of mappings. This equals the size of {@link #keySetA()}
     * and {@link #keySetB()}.
     * @return non-negative integer
     */
    public int size() {
        return forward.size();
    }

    public void clear() {
        forward.clear();
        backward.clear();
    }
}
