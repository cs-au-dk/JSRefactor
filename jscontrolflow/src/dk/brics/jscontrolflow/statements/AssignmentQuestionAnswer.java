package dk.brics.jscontrolflow.statements;

public interface AssignmentQuestionAnswer<Q,A> {
    A caseBinaryOperation(BinaryOperation stm, Q arg);
    A caseBooleanConst(BooleanConst stm, Q arg);
    A caseCall(Call stm, Q arg);
    A caseCatch(Catch stm, Q arg);
    A caseConstructorCall(CallConstructor stm, Q arg);
    A caseCreateFunction(CreateFunction stm, Q arg);
    A caseDeleteProperty(DeleteProperty stm, Q arg);
    A caseGetNextProperty(GetNextProperty stm, Q arg);
    A caseNewArray(NewArray stm, Q arg);
    A caseNewObject(NewObject stm, Q arg);
    A caseNewRegexp(NewRegexp stm, Q arg);
    A caseNullConst(NullConst stm, Q arg);
    A caseNumberConst(NumberConst stm, Q arg);
    A casePhi(Phi stm, Q arg);
    A caseReadProperty(ReadProperty stm, Q arg);
    A caseStringConst(StringConst stm, Q arg);
    A caseUndefinedConst(UndefinedConst stm, Q arg);
    A caseUnaryOperation(UnaryOperation stm, Q arg);
    A caseReadThis(ReadThis stm, Q arg);
    A caseReadVariable(ReadVariable stm, Q arg);
    A caseCallVariable(CallVariable stm, Q arg);
    A caseCallProperty(CallProperty stm, Q arg);
}
