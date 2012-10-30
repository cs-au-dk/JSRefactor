package dk.brics.jspointers.lattice.values.natives;


public interface NativeFunctionVisitor {
    void caseObject(ObjectNative value);
	void caseObjectCreate(ObjectCreateNative value);
	void caseObjectGetPrototypeOf(ObjectGetPrototypeOfNative value);
	
	void caseFunction(FunctionNative value);
    void caseFunctionCall(FunctionCallNative value);
    void caseFunctionApply(FunctionApplyNative value);
	void caseFunctionBind(FunctionBindNative value);
	void caseFunctionBindResult(FunctionBindResultNative value);
	
    void caseDOM(DOMNative value);
	void caseEval(EvalNative value);
	
}
