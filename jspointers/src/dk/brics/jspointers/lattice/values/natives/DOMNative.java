package dk.brics.jspointers.lattice.values.natives;

import java.util.Collections;
import java.util.Set;

import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.values.GlobalObjectValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;

/**
 * Represents all objects in the DOM (including its functions), except <tt>window</tt>
 * which is represented by {@link GlobalObjectValue}.
 *  
 * @author Asger
 */
public class DOMNative extends NativeFunctionValue {
    public static final DOMNative Instance = new DOMNative();

    private DOMNative() {}

    @Override
    public void apply(NativeFunctionVisitor visitor) {
        visitor.caseDOM(this);
    }
    @Override
    public String getPrettyName() {
        return "DOM";
    }

    @Override
    public Context getContext() {
        return null;
    }
    @Override
    public DOMNative makeContextInsensitive() {
        return this;
    }
    @Override
    public DOMNative replaceContext(Context newContext) {
        return this;
    }

    @Override
    public int hashCode() {
        return 39582;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public Set<String> getNativeMembers() {
    	return Collections.emptySet();
    }
    @Override
    public Set<String> getNativeStaticMembers() {
    	return Collections.emptySet();
    }
}
