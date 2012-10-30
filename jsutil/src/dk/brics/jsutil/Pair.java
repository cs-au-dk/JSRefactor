package dk.brics.jsutil;

/**
 * Generic type of pairs.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 * @param <S> type of first component
 * @param <T> type of second component
 */
public class Pair<S, T> {
	public S fst;
	public T snd;
	
	public Pair(S fst, T snd) {
		super();
		this.fst = fst;
		this.snd = snd;
	}
	
	/**
	 * Alternative to using the constructor. Useful when Java can infer the type parameters.
	 */
	public static <S,T> Pair<S,T> make(S first, T second) {
	    return new Pair<S,T>(first,second);
	}
	
	@Override
	public String toString() {
		return "<" + fst + ", " + snd + ">";
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fst == null) ? 0 : fst.hashCode());
        result = prime * result + ((snd == null) ? 0 : snd.hashCode());
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
        Pair<?,?> other = (Pair<?,?>) obj;
        if (fst == null) {
            if (other.fst != null)
                return false;
        } else if (!fst.equals(other.fst))
            return false;
        if (snd == null) {
            if (other.snd != null)
                return false;
        } else if (!snd.equals(other.snd))
            return false;
        return true;
    }
}
