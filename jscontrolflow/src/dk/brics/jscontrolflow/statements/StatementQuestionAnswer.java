package dk.brics.jscontrolflow.statements;

public interface StatementQuestionAnswer<Q,A> extends AssignmentQuestionAnswer<Q,A> {
    A caseDeclareVariable(DeclareVariable stm, Q arg);
    A caseEnterWith(EnterWith stm, Q arg);
    A caseExceptionalReturn(ExceptionalReturn stm, Q arg);
    A caseLeaveScope(LeaveScope stm, Q arg);
    A caseNop(Nop stm, Q arg);
    A caseReturn(Return stm, Q arg);
    A caseReturnVoid(ReturnVoid stm, Q arg);
    A caseThrow(Throw stm, Q arg);
    A caseWriteProperty(WriteProperty stm, Q arg);
    A caseWriteVariable(WriteVariable stm, Q arg);
    A caseAssertion(Assertion stm, Q arg);
    A caseEnterCatch(EnterCatch stm, Q arg);
}
