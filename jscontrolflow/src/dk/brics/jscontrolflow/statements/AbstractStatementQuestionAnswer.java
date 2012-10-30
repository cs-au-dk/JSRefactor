package dk.brics.jscontrolflow.statements;

public class AbstractStatementQuestionAnswer<Q,A> implements StatementQuestionAnswer<Q,A> {

    @Override
    public A caseBinaryOperation(BinaryOperation stm, Q arg) {
        return null;
    }

    @Override
    public A caseBooleanConst(BooleanConst stm, Q arg) {
        return null;
    }

    @Override
    public A caseCall(Call stm, Q arg) {
        return null;
    }

    @Override
    public A caseCatch(Catch stm, Q arg) {
        return null;
    }

    @Override
    public A caseConstructorCall(CallConstructor stm, Q arg) {
        return null;
    }

    @Override
    public A caseCreateFunction(CreateFunction stm, Q arg) {
        return null;
    }

    @Override
    public A caseDeleteProperty(DeleteProperty stm, Q arg) {
        return null;
    }

    @Override
    public A caseGetNextProperty(GetNextProperty stm, Q arg) {
        return null;
    }

    @Override
    public A caseNewArray(NewArray stm, Q arg) {
        return null;
    }

    @Override
    public A caseNewObject(NewObject stm, Q arg) {
        return null;
    }

    @Override
    public A caseNewRegexp(NewRegexp stm, Q arg) {
        return null;
    }

    @Override
    public A caseNullConst(NullConst stm, Q arg) {
        return null;
    }

    @Override
    public A caseNumberConst(NumberConst stm, Q arg) {
        return null;
    }

    @Override
    public A casePhi(Phi stm, Q arg) {
        return null;
    }

    @Override
    public A caseReadProperty(ReadProperty stm, Q arg) {
        return null;
    }

    @Override
    public A caseStringConst(StringConst stm, Q arg) {
        return null;
    }

    @Override
    public A caseUndefinedConst(UndefinedConst stm, Q arg) {
        return null;
    }

    @Override
    public A caseUnaryOperation(UnaryOperation stm, Q arg) {
        return null;
    }

    @Override
    public A caseReadThis(ReadThis stm, Q arg) {
        return null;
    }

    @Override
    public A caseReadVariable(ReadVariable stm, Q arg) {
        return null;
    }

    @Override
    public A caseDeclareVariable(DeclareVariable stm, Q arg) {
        return null;
    }

    @Override
    public A caseEnterWith(EnterWith stm, Q arg) {
        return null;
    }

    @Override
    public A caseExceptionalReturn(ExceptionalReturn stm, Q arg) {
        return null;
    }

    @Override
    public A caseLeaveScope(LeaveScope stm, Q arg) {
        return null;
    }

    @Override
    public A caseNop(Nop stm, Q arg) {
        return null;
    }

    @Override
    public A caseReturn(Return stm, Q arg) {
        return null;
    }

    @Override
    public A caseReturnVoid(ReturnVoid stm, Q arg) {
        return null;
    }

    @Override
    public A caseThrow(Throw stm, Q arg) {
        return null;
    }

    @Override
    public A caseWriteProperty(WriteProperty stm, Q arg) {
        return null;
    }

    @Override
    public A caseWriteVariable(WriteVariable stm, Q arg) {
        return null;
    }

    @Override
    public A caseAssertion(Assertion stm, Q arg) {
        return null;
    }

    @Override
    public A caseEnterCatch(EnterCatch stm, Q arg) {
        return null;
    }

    @Override
    public A caseCallVariable(CallVariable stm, Q arg) {
        return null;
    }

    @Override
    public A caseCallProperty(CallProperty stm, Q arg) {
        return null;
    }

}
