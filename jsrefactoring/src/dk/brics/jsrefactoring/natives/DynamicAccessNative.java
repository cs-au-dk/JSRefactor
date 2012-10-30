package dk.brics.jsrefactoring.natives;

import java.util.Arrays;
import java.util.List;

/**
 * Denotes that the given native function will dynamically access the named properties of
 * its <i>n</i>-th argument (or its <i>this</i> argument if <i>n</i>=-1).
 * 
 * (A named property is a property whose name is a valid identifier).
 */
public class DynamicAccessNative {
	private NativeMember member;
	private int argumentIndex;
	
	/**
	 * The special argument index -1 that represents the <i>this</i> argument.
	 */
	public static int THIS = -1;
	
	public static final List<DynamicAccessNative> LIST = Arrays.asList(
		new DynamicAccessNative(new NativeMember("Object",false,"getOwnPropertyDescriptor"), 0),
		new DynamicAccessNative(new NativeMember("Object",false,"getOwnPropertyNames"), 0),
		new DynamicAccessNative(new NativeMember("Object",false,"defineProperty"), 0),
		new DynamicAccessNative(new NativeMember("Object",false,"defineProperties"), 0),
		new DynamicAccessNative(new NativeMember("Object",false,"defineProperties"), 1), // both arguments
		new DynamicAccessNative(new NativeMember("Object",false,"keys"), 0),
		new DynamicAccessNative(new NativeMember("Object",true, "hasOwnProperty"), THIS),
		new DynamicAccessNative(new NativeMember("Object",true, "propertyIsEnumerable"), THIS)
	);
	
	public DynamicAccessNative(NativeMember member, int argumentNumber) {
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
		DynamicAccessNative other = (DynamicAccessNative) obj;
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
