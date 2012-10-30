package dk.brics.jscontrolflow.statements;

public interface StatementVisitor extends AssignmentVisitor {
    void caseDeclareVariable(DeclareVariable stm);
    void caseEnterWith(EnterWith stm);
    void caseExceptionalReturn(ExceptionalReturn stm);
    void caseLeaveScope(LeaveScope stm);
    void caseNop(Nop stm);
    void caseReturn(Return stm);
    void caseReturnVoid(ReturnVoid stm);
    void caseThrow(Throw stm);
    void caseWriteProperty(WriteProperty stm);
    void caseWriteVariable(WriteVariable stm);
    void caseAssertion(Assertion stm);
    void caseEnterCatch(EnterCatch stm);
}
