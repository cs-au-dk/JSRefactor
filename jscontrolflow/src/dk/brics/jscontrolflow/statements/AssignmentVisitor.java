package dk.brics.jscontrolflow.statements;

public interface AssignmentVisitor {
    void caseBinaryOperation(BinaryOperation stm);
    void caseBooleanConst(BooleanConst stm);
    void caseCall(Call stm);
    void caseCatch(Catch stm);
    void caseConstructorCall(CallConstructor stm);
    void caseCreateFunction(CreateFunction stm);
    void caseDeleteProperty(DeleteProperty stm);
    void caseGetNextProperty(GetNextProperty stm);
    void caseNewArray(NewArray stm);
    void caseNewObject(NewObject stm);
    void caseNewRegexp(NewRegexp stm);
    void caseNullConst(NullConst stm);
    void caseNumberConst(NumberConst stm);
    void casePhi(Phi stm);
    void caseReadProperty(ReadProperty stm);
    void caseStringConst(StringConst stm);
    void caseUndefinedConst(UndefinedConst stm);
    void caseUnaryOperation(UnaryOperation stm);
    void caseReadThis(ReadThis stm);
    void caseReadVariable(ReadVariable stm);
    void caseCallVariable(CallVariable stm);
    void caseCallProperty(CallProperty stm);
}
