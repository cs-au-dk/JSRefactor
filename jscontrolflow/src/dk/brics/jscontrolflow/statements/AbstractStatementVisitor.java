package dk.brics.jscontrolflow.statements;

import dk.brics.jscontrolflow.Statement;

public class AbstractStatementVisitor implements StatementVisitor {

    @Override
    public void caseBinaryOperation(BinaryOperation stm) {
        defaultCase(stm);
    }

    @Override
    public void caseBooleanConst(BooleanConst stm) {
        defaultCase(stm);
    }

    @Override
    public void caseCall(Call stm) {
        defaultCase(stm);
    }

    @Override
    public void caseCatch(Catch stm) {
        defaultCase(stm);
    }

    @Override
    public void caseConstructorCall(CallConstructor stm) {
        defaultCase(stm);
    }

    @Override
    public void caseCreateFunction(CreateFunction stm) {
        defaultCase(stm);
    }

    @Override
    public void caseDeleteProperty(DeleteProperty stm) {
        defaultCase(stm);
    }

    @Override
    public void caseGetNextProperty(GetNextProperty stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNewArray(NewArray stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNewObject(NewObject stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNewRegexp(NewRegexp stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNullConst(NullConst stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNumberConst(NumberConst stm) {
        defaultCase(stm);
    }

    @Override
    public void casePhi(Phi stm) {
        defaultCase(stm);
    }

    @Override
    public void caseReadProperty(ReadProperty stm) {
        defaultCase(stm);
    }

    @Override
    public void caseStringConst(StringConst stm) {
        defaultCase(stm);
    }

    @Override
    public void caseUndefinedConst(UndefinedConst stm) {
        defaultCase(stm);
    }

    @Override
    public void caseUnaryOperation(UnaryOperation stm) {
        defaultCase(stm);
    }

    @Override
    public void caseReadThis(ReadThis stm) {
        defaultCase(stm);
    }

    @Override
    public void caseReadVariable(ReadVariable stm) {
        defaultCase(stm);
    }

    @Override
    public void caseDeclareVariable(DeclareVariable stm) {
        defaultCase(stm);
    }

    @Override
    public void caseEnterWith(EnterWith stm) {
        defaultCase(stm);
    }

    @Override
    public void caseExceptionalReturn(ExceptionalReturn stm) {
        defaultCase(stm);
    }

    @Override
    public void caseLeaveScope(LeaveScope stm) {
        defaultCase(stm);
    }

    @Override
    public void caseNop(Nop stm) {
        defaultCase(stm);
    }

    @Override
    public void caseReturn(Return stm) {
        defaultCase(stm);
    }

    @Override
    public void caseReturnVoid(ReturnVoid stm) {
        defaultCase(stm);
    }

    @Override
    public void caseThrow(Throw stm) {
        defaultCase(stm);
    }

    @Override
    public void caseWriteProperty(WriteProperty stm) {
        defaultCase(stm);
    }

    @Override
    public void caseWriteVariable(WriteVariable stm) {
        defaultCase(stm);
    }

    @Override
    public void caseAssertion(Assertion stm) {
        defaultCase(stm);
    }

    @Override
    public void caseEnterCatch(EnterCatch stm) {
        defaultCase(stm);
    }

    @Override
    public void caseCallProperty(CallProperty stm) {
        defaultCase(stm);
    }

    @Override
    public void caseCallVariable(CallVariable stm) {
        defaultCase(stm);
    }
    
    public void defaultCase(Statement stm) {
    }
}
