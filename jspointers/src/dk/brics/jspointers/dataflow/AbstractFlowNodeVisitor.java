package dk.brics.jspointers.dataflow;

public class AbstractFlowNodeVisitor implements FlowNodeVisitor {

    @Override
    public void caseConst(ConstNode node) {
    }

    @Override
    public void caseInvoke(InvokeNode node) {
    }

    @Override
    public void caseLoad(LoadNode node) {
    }
    @Override
    public void caseLoadDirect(LoadDirectNode node) {
    }

    @Override
    public void casePlus(PlusNode node) {
    }

    @Override
    public void caseStoreIfPresent(StoreIfPresentNode node) {
    }

    @Override
    public void caseStore(StoreNode node) {
    }

    @Override
    public void caseVarReadInterscope(VarReadInterscopeNode node) {
    }

    @Override
    public void caseVarWriteInterscope(VarWriteInterscopeNode node) {
    }

    @Override
    public void caseReturn(ReturnNode node) {
    }

    @Override
    public void caseInvokeResult(InvokeResultNode node) {
    }

    @Override
    public void caseStoreDynamic(StoreDynamicNode node) {
    }

    @Override
    public void caseLoadDynamic(LoadDynamicNode node) {
    }

    @Override
    public void caseInitialize(InitializeNode node) {
    }

    @Override
    public void caseInitializeFunction(InitializeFunctionNode node) {
    }

    @Override
    public void caseFunctionInstance(FunctionInstanceNode node) {
    }

    @Override
    public void caseVarRead(VarReadNode node) {
    }

    @Override
    public void caseVarWrite(VarWriteNode node) {
    }

    @Override
    public void caseAlloc(AllocNode node) {
    }

    @Override
    public void caseVarReadGlobal(VarReadGlobalNode node) {
    }

    @Override
    public void caseVarWriteGlobal(VarWriteGlobalNode node) {
    }

    @Override
    public void caseGlobalException(GlobalExceptionNode node) {
    }

    @Override
    public void caseIdentity(IdentityNode node) {
    }

    @Override
    public void caseCoerceToPrimitive(CoerceToPrimitive node) {
    }

    @Override
    public void caseLoadAndInvoke(LoadAndInvokeNode node) {
    }

    @Override
    public void caseInterscopeIdentity(InterscopeIdentityNode node) {
    }

    @Override
    public void caseStub(StubNode node) {
    }
	@Override
	public void caseCoerceToObject(CoerceToObject node) {
	}
	@Override
	public void caseSetPrototype(SetPrototype node) {
	}
}
