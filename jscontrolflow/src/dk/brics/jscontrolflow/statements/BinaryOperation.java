package dk.brics.jscontrolflow.statements;

import java.util.Arrays;
import java.util.Collection;

import dk.brics.jsparser.node.EAssignOp;
import dk.brics.jsparser.node.EBinop;

/**
 * v<sub>result</sub> = v<sub>arg1</sub> <i>op</i> v<sub>arg2</sub>
 * <p/>
 * The logical AND and logical OR operators in JavaScript are not binary operators
 * in the control-flow graph due to their lazy evaluation semantics. All other binary
 * operators have a corresponding operator in {@link Operator}.
 */
public class BinaryOperation extends Assignment {
    private Operator operator;
    private int arg1Var;
    private int arg2Var;

    public enum Operator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDE,
        MODULO,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        USHIFT_RIGHT,
        BITWISE_AND,
        BITWISE_OR,
        BITWISE_XOR,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        EQUAL,
        STRICT_EQUAL,
        NOT_EQUAL,
        STRICT_NOT_EQUAL,
        INSTANCEOF,
        IN,
    }

    public static Operator fromBinop(EBinop binop) {
        switch (binop) {
        case PLUS: return Operator.PLUS;
        case MINUS: return Operator.MINUS;
        case TIMES: return Operator.TIMES;
        case DIVIDE: return Operator.DIVIDE;
        case MODULO: return Operator.MODULO;
        case SHIFT_LEFT: return Operator.SHIFT_LEFT;
        case SHIFT_RIGHT: return Operator.SHIFT_RIGHT;
        case SHIFT_RIGHT_UNSIGNED: return Operator.USHIFT_RIGHT;
        case BITWISE_AND: return Operator.BITWISE_AND;
        case BITWISE_OR: return Operator.BITWISE_OR;
        case BITWISE_XOR: return Operator.BITWISE_XOR;
        case LESS: return Operator.LESS;
        case LESS_EQUAL: return Operator.LESS_EQUAL;
        case GREATER: return Operator.GREATER;
        case GREATER_EQUAL: return Operator.GREATER_EQUAL;
        case EQUAL: return Operator.EQUAL;
        case EQUAL_STRICT: return Operator.STRICT_EQUAL;
        case NOT_EQUAL: return Operator.NOT_EQUAL;
        case NOT_EQUAL_STRICT: return Operator.STRICT_NOT_EQUAL;
        case INSTANCEOF: return Operator.INSTANCEOF;
        case IN: return Operator.IN;
        case LOGICAL_AND:
        case LOGICAL_OR: throw new IllegalArgumentException("Logical operators are not binops in the control flow graph");
        default: throw new IllegalArgumentException("Unknown binop: " + binop);
        }
    }

    public static Operator fromAssignOp(EAssignOp op) {
        switch (op) {
        case NORMAL: throw new IllegalArgumentException("Assignment operator = has no binary operation");
        case PLUS: return Operator.PLUS;
        case MINUS: return Operator.MINUS;
        case TIMES: return Operator.TIMES;
        case DIVIDE: return Operator.DIVIDE;
        case BITWISE_AND: return Operator.BITWISE_AND;
        case BITWISE_OR: return Operator.BITWISE_OR;
        case BITWISE_XOR: return Operator.BITWISE_XOR;
        case MODULO: return Operator.MODULO;
        case SHIFT_LEFT: return Operator.SHIFT_LEFT;
        case SHIFT_RIGHT: return Operator.SHIFT_RIGHT;
        case SHIFT_RIGHT_UNSIGNED: return Operator.USHIFT_RIGHT;
        default: throw new IllegalArgumentException("Unexpected assign op: " + op);
        }
    }

    public BinaryOperation(int resultVar, Operator operator, int arg1Var,
            int arg2Var) {
        super(resultVar);
        this.operator = operator;
        this.arg1Var = arg1Var;
        this.arg2Var = arg2Var;
    }

    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    public int getArg1Var() {
        return arg1Var;
    }
    public void setArg1Var(int arg1Var) {
        this.arg1Var = arg1Var;
    }
    public int getArg2Var() {
        return arg2Var;
    }
    public void setArg2Var(int arg2Var) {
        this.arg2Var = arg2Var;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Arrays.asList(arg1Var, arg2Var);
    }

    @Override
    public boolean canThrowException() {
        return true;
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseBinaryOperation(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseBinaryOperation(this, arg);
    }
}
