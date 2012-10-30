package dk.brics.jsrefactoring.natives;


/**
 * Denotes that the given native function will dynamically examine the set of own properties of
 * its <i>n</i>-th argument (or its <i>this</i> argument if <i>n</i>=-1).
 * 
 * <p>
 * Mostly the same as {@link DynamicAccessNative}.
 * </p>
 */
public class OwnPropertyAccessNative {
	private NativeMember member;
	private int argumentIndex;
	
	/**
	 * The special argument index -1 that represents the <i>this</i> argument.
	 */
	public static int THIS = -1;
	
	public static final OwnPropertyAccessNative LIST[] = {
		new OwnPropertyAccessNative(new NativeMember("Object",false,"getOwnPropertyDescriptor"), 0),
		new OwnPropertyAccessNative(new NativeMember("Object",false,"getOwnPropertyNames"), 0),
		new OwnPropertyAccessNative(new NativeMember("Object",true, "hasOwnProperty"), THIS),
		new OwnPropertyAccessNative(new NativeMember("Object",true, "propertyIsEnumerable"), THIS)
	};
	
	private OwnPropertyAccessNative(NativeMember member, int argumentNumber) {
		this.member = member;
		this.argumentIndex = argumentNumber;
	}
	
	public NativeMember getMember() {
		return member;
	}
	public int getArgumentIndex() {
		return argumentIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + argumentIndex;
		result = prime * result + ((member == null) ? 0 : member.hashCode());
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
		OwnPropertyAccessNative other = (OwnPropertyAccessNative) obj;
		if (argumentIndex != other.argumentIndex)
			return false;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getMember().getPrettyName() + "@" + argumentIndex;
	}
	
}
