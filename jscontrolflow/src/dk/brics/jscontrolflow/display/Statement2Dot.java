package dk.brics.jscontrolflow.display;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.Assertion;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.BooleanConst;
import dk.brics.jscontrolflow.statements.Call;
import dk.brics.jscontrolflow.statements.CallConstructor;
import dk.brics.jscontrolflow.statements.CallProperty;
import dk.brics.jscontrolflow.statements.CallVariable;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeclareVariable;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.EnterCatch;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.ExceptionalReturn;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.IVariableAccessStatement;
import dk.brics.jscontrolflow.statements.LeaveScope;
import dk.brics.jscontrolflow.statements.NewArray;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.NewRegexp;
import dk.brics.jscontrolflow.statements.Nop;
import dk.brics.jscontrolflow.statements.NullConst;
import dk.brics.jscontrolflow.statements.NumberConst;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadThis;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.Return;
import dk.brics.jscontrolflow.statements.ReturnVoid;
import dk.brics.jscontrolflow.statements.StatementQuestionAnswer;
import dk.brics.jscontrolflow.statements.StringConst;
import dk.brics.jscontrolflow.statements.Throw;
import dk.brics.jscontrolflow.statements.UnaryOperation;
import dk.brics.jscontrolflow.statements.UndefinedConst;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteVariable;

public class Statement2Dot {
    /**
     * Returns the dot representation of a statement. Note that the returned string is not escaped, and should be
     * post-processed when used in a label.
     */
    public static String toDot(Statement stm) {
        return stm.getSerial() + " " + stm.apply(VISITOR, null);
    }
    public static final StatementQuestionAnswer<Void, String> VISITOR = new StatementQuestionAnswer<Void, String>() {
    	private String scopeToString(Scope scope) {
    		if (scope instanceof Function) {
				return "outer function";
			} else if (scope instanceof WithScope) {
				return "with";
			} else {
				return "catch";
			}
    	}
    	private String scopeInfo(IVariableAccessStatement stm) {
    		if (stm.getScope() == stm.getBlock().getFunction())
    			return "";
    		else
    			return " (" + scopeToString(stm.getScope()) + ")";
    	}
        @Override
        public String caseReadVariable(ReadVariable stm, Void arg) {
            return "read-variable[v" + stm.getResultVar() + "," + stm.getVarName() + "]" + scopeInfo(stm);
        }
        @Override
        public String caseUndefinedConst(UndefinedConst stm, Void arg) {
            return "undefined-const[v"+stm.getResultVar()+"]";
        }
        @Override
        public String caseUnaryOperation(UnaryOperation stm, Void arg) {
            return "unary-op[v"+stm.getResultVar()+","+stm.getOperator().toString().toLowerCase()+",v"+stm.getArgVar()+"]";
        }
        @Override
        public String caseStringConst(StringConst stm, Void arg) {
            return "string-const[v"+stm.getResultVar()+","+stm.getString()+"]";
        }
        @Override
        public String caseReadProperty(ReadProperty stm, Void arg) {
            return "read-property[v"+stm.getResultVar()+",v"+stm.getBaseVar()+",v"+stm.getPropertyVar()+"]";
        }
        @Override
        public String casePhi(Phi stm, Void arg) {
            return "phi[v"+stm.getResultVar()+",v"+stm.getArg1Var()+",v"+stm.getArg2Var()+"]";
        }
        @Override
        public String caseNumberConst(NumberConst stm, Void arg) {
            return "number-const[v"+stm.getResultVar()+","+stm.getNumber()+"]";
        }
        @Override
        public String caseNullConst(NullConst stm, Void arg) {
            return "null-const[v"+stm.getResultVar()+"]";
        }
        @Override
        public String caseNewRegexp(NewRegexp stm, Void arg) {
            return "new-regexp[v"+stm.getResultVar()+","+stm.getRegexp()+"]";
        }
        @Override
        public String caseNewObject(NewObject stm, Void arg) {
            return "new-object[v"+stm.getResultVar()+"]";
        }
        @Override
        public String caseNewArray(NewArray stm, Void arg) {
            return "new-array[v"+stm.getResultVar()+","+stm.getLength()+"]";
        }
        @Override
        public String caseGetNextProperty(GetNextProperty stm, Void arg) {
            return "get-next-property[v"+stm.getResultVar()+",v"+stm.getObjectVar()+"]";
        }
        @Override
        public String caseDeleteProperty(DeleteProperty stm, Void arg) {
            return "delete-property[v"+stm.getResultVar()+",v"+stm.getBaseVar()+",v"+stm.getPropertyVar()+"]";
        }
        @Override
        public String caseCreateFunction(CreateFunction stm, Void arg) {
            return "create-function[v"+stm.getResultVar()+",f"+stm.getFunction().getSerial()+"]";
        }
        @Override
        public String caseCall(Call stm, Void arg) {
            StringBuilder b = new StringBuilder("call[v"+stm.getResultVar()+",v"+stm.getFuncVar());
            for (int argument : stm.getArguments()) {
                b.append(",v").append(argument);
            }
            b.append("]");
            return b.toString();
        }
        @Override
        public String caseCallVariable(CallVariable stm, Void arg) {
            StringBuilder b = new StringBuilder("call-var[v"+stm.getResultVar()+","+stm.getVarName());
            for (int argument : stm.getArguments()) {
                b.append(",v").append(argument);
            }
            b.append("]");
            b.append(scopeInfo(stm));
            return b.toString();
        }
        @Override
        public String caseCallProperty(CallProperty stm, Void arg) {
            StringBuilder b = new StringBuilder("call-property[v"+stm.getResultVar()+",v"+stm.getBaseVar()+",v"+stm.getPropertyVar());
            for (int argument : stm.getArguments()) {
                b.append(",v").append(argument);
            }
            b.append("]");
            return b.toString();
        }
        @Override
        public String caseConstructorCall(CallConstructor stm, Void arg) {
            StringBuilder b = new StringBuilder("constructor-call[v"+stm.getResultVar()+",v"+stm.getFuncVar());
            for (int argument : stm.getArguments()) {
                b.append(",v").append(argument);
            }
            b.append("]");
            return b.toString();
        }
        @Override
        public String caseCatch(Catch stm, Void arg) {
            return "catch[v"+stm.getResultVar()+"]";
        }
        @Override
        public String caseBooleanConst(BooleanConst stm, Void arg) {
            return "boolean-const[v"+stm.getResultVar()+","+stm.getValue()+"]";
        }
        @Override
        public String caseBinaryOperation(BinaryOperation stm, Void arg) {
            return "binary-op[v"+stm.getResultVar()+","+stm.getOperator().toString().toLowerCase()+",v"+stm.getArg1Var()+",v"+stm.getArg2Var()+"]";
        }
        @Override
        public String caseWriteVariable(WriteVariable stm, Void arg) {
            return "write-variable["+stm.getVarName()+",v"+stm.getValueVar()+"]" + scopeInfo(stm);
        }
        @Override
        public String caseWriteProperty(WriteProperty stm, Void arg) {
            return "write-property[v"+stm.getBaseVar()+",v"+stm.getPropertyVar()+",v"+stm.getValueVar()+"]";
        }
        @Override
        public String caseThrow(Throw stm, Void arg) {
            return "throw[v"+stm.getArgVar()+"]";
        }
        @Override
        public String caseReturnVoid(ReturnVoid stm, Void arg) {
            if (stm.isImplicit()) {
                return "return-void (implicit)";
            } else {
                return "return-void";
            }
        }
        @Override
        public String caseReturn(Return stm, Void arg) {
            return "return[v"+stm.getArgVar()+"]";
        }
        @Override
        public String caseNop(Nop stm, Void arg) {
            return "nop";
        }
        @Override
        public String caseLeaveScope(LeaveScope stm, Void arg) {
            return "leave-scope";
        }
        @Override
        public String caseExceptionalReturn(ExceptionalReturn stm, Void arg) {
            return "exceptional-return";
        }
        @Override
        public String caseEnterWith(EnterWith stm, Void arg) {
            return "enter-with-scope[v"+stm.getObjectVar()+"]";
        }
        @Override
        public String caseEnterCatch(EnterCatch stm, Void arg) {
            return "enter-catch-scope["+stm.getVarName()+"]";
        }
        @Override
        public String caseDeclareVariable(DeclareVariable stm, Void arg) {
            return "declare-var["+stm.getVarName()+","+stm.getKind().toString().toLowerCase()+"]";
        }
        @Override
        public String caseAssertion(Assertion stm, Void arg) {
            return "assert[v"+stm.getArgVar()+","+stm.getValue()+"]";
        }
        @Override
        public String caseReadThis(ReadThis stm, Void arg) {
            return "read-this[v"+stm.getResultVar()+"]";
        }
    };
}
