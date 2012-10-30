package dk.brics.jspointers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindNative;
import dk.brics.jspointers.lattice.values.natives.FunctionBindResultNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jspointers.lattice.values.natives.DOMNative;
import dk.brics.jspointers.lattice.values.natives.FunctionNative;
import dk.brics.jspointers.lattice.values.natives.ObjectCreateNative;
import dk.brics.jspointers.lattice.values.natives.ObjectNative;

public class JSUtil {
	/**
	 * All the native functions that are not part of the harness files. These are all the subtypes of {@link NativeFunctionValue}.
	 * No instance of {@link FunctionBindResultNative} is part of this set.
	 */
	public static final Set<NativeFunctionValue> NATIVE_FUNCTIONS = new HashSet<NativeFunctionValue>(Arrays.asList(
			ObjectNative.Instance,
			ObjectCreateNative.Instance,
			FunctionNative.Instance,
			FunctionApplyNative.Instance,
			FunctionCallNative.Instance,
			FunctionBindNative.Instance,
			DOMNative.Instance
		));
}
