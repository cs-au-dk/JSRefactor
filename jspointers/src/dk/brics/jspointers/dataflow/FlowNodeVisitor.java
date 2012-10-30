package dk.brics.jspointers.dataflow;

public interface FlowNodeVisitor {
    void caseConst(ConstNode node);
    void caseInvoke(InvokeNode node);
    void caseLoad(LoadNode node);
    void casePlus(PlusNode node);
    void caseStoreIfPresent(StoreIfPresentNode node);
    void caseStore(StoreNode node);
    void caseVarReadInterscope(VarReadInterscopeNode node);
    void caseVarWriteInterscope(VarWriteInterscopeNode node);
    void caseReturn(ReturnNode node);
    void caseInvokeResult(InvokeResultNode node);
    void caseStoreDynamic(StoreDynamicNode node);
    void caseLoadDynamic(LoadDynamicNode node);
    void caseInitialize(InitializeNode node);
    void caseInitializeFunction(InitializeFunctionNode node);
    void caseFunctionInstance(FunctionInstanceNode node);
    void caseVarRead(VarReadNode node);
    void caseVarWrite(VarWriteNode node);
    void caseAlloc(AllocNode node);
    void caseVarReadGlobal(VarReadGlobalNode node);
    void caseVarWriteGlobal(VarWriteGlobalNode node);
    void caseGlobalException(GlobalExceptionNode node);
    void caseIdentity(IdentityNode node);
    void caseCoerceToPrimitive(CoerceToPrimitive node);
    void caseLoadAndInvoke(LoadAndInvokeNode node);
    void caseInterscopeIdentity(InterscopeIdentityNode node);
    void caseStub(StubNode node);
	void caseCoerceToObject(CoerceToObject node);
	void caseSetPrototype(SetPrototype node);
	void caseLoadDirect(LoadDirectNode node);
}
