package dk.brics.jsrefactoring.natives;

/**
 * Denotes a member of one of the top-level native functions, such as <tt>Object.create</tt> or <tt>Object.prototype.hasOwnProperty</tt>.
 */
public class NativeMember {
	private String hostName;
	private boolean isPrototype;
	private String memberName;
	public NativeMember(String hostName, boolean isPrototype, String memberName) {
		super();
		this.hostName = hostName;
		this.isPrototype = isPrototype;
		this.memberName = memberName;
	}
	public String getHostName() {
		return hostName;
	}
	public boolean isPrototype() {
		return isPrototype;
	}
	public String getMemberName() {
		return memberName;
	}
	/**
	 * Returns the name denoted by the function expression for this function in the harness file.
	 * Eg.
	 * <pre>
	 * Object.foo = function Object_foo(..) {...}
	 * Object.prototype.bar = function Object_prototype_bar(..) {...}
	 * </pre>
	 */
	public String getCodeName() {
		if (isPrototype) {
			return hostName + "_prototype_" + memberName;
		} else {
			return hostName + "_" + memberName;
		}
	}
	/**
	 * Returns a pretty name for the member, such as "<tt>Object.create</tt>" or "<tt>Object.prototype.hasOwnProperty</tt>"
	 */
	public String getPrettyName() {
		if (isPrototype) {
			return hostName + ".prototype." + memberName;
		} else {
			return hostName + "." + memberName;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + (isPrototype ? 1231 : 1237);
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
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
		NativeMember other = (NativeMember) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (isPrototype != other.isPrototype)
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		return true;
	}
}
