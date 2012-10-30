package dk.brics.jspointers.test.instrument;

public abstract class AllocSite {
	public abstract boolean equals(Object o);
	public abstract int hashCode();
	
	public abstract <A> A apply(AllocSiteAnswerVisitor<A> v);  
    public abstract void apply(AllocSiteVisitor v);
}
