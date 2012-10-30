package dk.brics.jspointers.lattice.values.natives;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * The native <tt>Object</tt> function.
 * 
 * @author asf
 */
public class ObjectNative extends NativeFunctionValue {
    public static final ObjectNative Instance = new ObjectNative();

    private ObjectNative() {}

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return 8764;
    }

    @Override
    public void apply(NativeFunctionVisitor visitor) {
        visitor.caseObject(this);
    }

    @Override
    public String getPrettyName() {
        return "Object";
    }
    
    private static final Set<String> natives = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
    		"create","getPrototypeOf"
    	)));
    @Override
    public Set<String> getNativeMembers() {
    	return Collections.emptySet();
    }
    @Override
    public Set<String> getNativeStaticMembers() {
    	return natives;
    }
}
