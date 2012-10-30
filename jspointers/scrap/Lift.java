package dk.brics.jsrefactoring;

/**
 * Lift is the algebraic datatype:
 * <pre>
 * Lift a = Bottom
 *        | Top
 *        | Actual a
 * </pre>
 * It can also be seen as the lattice:
 * <pre>
 *      TOP          (the top value)
 *    /  |  \ 
 * ..v1  v2  v3...   (<i>actual</i> values)
 *    \  |  /
 *     BOTTOM        (the bottom value)
 * </pre>
 * where v<sub>i</sub> are all the values of the type parameter <tt>T</tt>.
 * <p/>
 * When comparing for instance equality, keep in mind that there exists exactly
 * one instance of TOP and one instance of BOTTOM.
 * <p/>
 * For the <i>actual</i> values, <tt>hashCode</tt> and <tt>equals</tt> are propagated to the
 * underlying instances of <tt>T</tt>. 
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class Lift<T> {
    private Lift() {}
    private static class Actual<T> extends Lift<T> {
        private T value;
        public Actual(T value) {
            this.value = value;
        }
        @Override
        public T getValue() {
            return value;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Actual other = (Actual) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }
    
    /**
     * Returns the unique BOTTOM instance.
     */
    public static <T> Lift<T> bottom() {
        return BOTTOM;
    }
    /**
     * Returns the unique TOP instance.
     */
    public static <T> Lift<T> top() {
        return TOP;
    }
    /**
     * Returns a new Lift instance containing the given value.
     * @param <T> type of the lift instance
     * @param value value contained in the lift instance
     * @return a newly created instance
     */
    public static <T> Lift<T> actual(T value) {
        return new Actual<T>(value);
    }
    
    public static <T> Lift<T> leastUpperBound(Lift<T> a, Lift<T> b) {
        if (a == BOTTOM)
            return b;
        if (b == BOTTOM)
            return a;
        if (a == TOP || b == TOP)
            return TOP;
        if (a.getValue() == b.getValue())
            return a;
        if (a.getValue() != null && a.getValue().equals(b.getValue()))
            return a;
        return TOP;
    }
    
    public Lift<T> leastUpperBoundWith(Lift<T> b) {
        return leastUpperBound(this, b);
    }
    
    public boolean isTop() {
        return this == TOP;
    }
    public boolean isBottom() {
        return this == BOTTOM;
    }
    public boolean isActual() {
        return this instanceof Actual<?>;
    }
    /**
     * Returns <tt>null</tt> if this is top or bottom,
     * and the value passed to {@link #actual(Object) actual} otherwise.
     * @return <tt>null</tt> or an object
     */
    public T getValue() {
        return null;
    }
    
    private static final Lift BOTTOM = new Lift<Object>(){};
    private static final Lift TOP = new Lift<Object>(){};
}
