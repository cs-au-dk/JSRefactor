package dk.brics.jspointers.lattice.keys;

public interface KeyVisitor {
    void caseDefaultProperty(DefaultPropertyKey key);
    void caseIntegerProperty(IntegerPropertyKey key);
    void caseNamedProperty(NamedPropertyKey key);
    void caseVariableKey(VariableKey key);
    void caseDynamicStoreProperty(DynamicStorePropertyKey key);
    void casePrototypePropertyKey(PrototypePropertyKey key);
    void caseNativeThisArg(NativeThisArgKey key);
    void caseNativeArg(NativeArgKey key);
    void caseNativeResult(NativeResultKey key);
    void caseNativeDefaultArgs(NativeDefaultArgsKey key);
    void caseNativeDynamicArgs(NativeDynamicArgsKey key);
    void caseNativeLabelArg(NativeLabelArgKey key);
    void caseOuterFunctionProperty(OuterFunctionPropertyKey key);
    void caseFunctionInstance(FunctionInstanceKey key);
    void caseNativeExceptionalResult(NativeExceptionalResultKey key);
    void caseBoundTarget(BoundTargetKey key);
	void caseBoundArg(BoundArgKey key);
	void caseBoundThisArg(BoundThisArgKey key);
	void caseBoundDynamicArg(BoundDynamicArgKey key);
	void caseBoundDefaultArg(BoundDefaultArgKey key);
	void caseOutputPoint(OutputPointKey key);
}
