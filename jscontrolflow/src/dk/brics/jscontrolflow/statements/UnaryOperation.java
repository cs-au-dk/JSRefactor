package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;

import dk.brics.jsparser.node.EPostfixUnop;
import dk.brics.jsparser.node.EPrefixUnop;

/**
 * Application of a {@link UnaryOperation.Operator unary operator}. The unary operation has no side-effects
 * on its argument. For this reason, <tt>delete</tt> is not a unary operator, even though it is at the AST level.
 * Increment and decrement operators (eg. <tt>++x</tt>, <tt>x--</tt>) have intermediate unary operations that have no side-effects;
 * their side-effects must be encoded using a combination of statements.
 * 
 * @author asf
 */
public class UnaryOperation extends Assignment {
    private Operator operator;
    private int argVar;

    public enum Operator {
        /**
         * Intermediate operator for ++X and X++. Should not have side-effects
         * on its argument, just return ToNumber(X)+1.
         */ 
        INCREMENT,

        /**
         * Intermediate operator for --X and X--. Should not have side-effects
         * on its argument, just return ToNumber(X)-1.
         */
        DECREMENT,

        /**
         * ~X. Bitwise complement.
         */
        COMPLEMENT,

        /**
         * !X. Logical negation.
         */
        NOT,

        /**
         * +X. Simply converts argument to a number.
         */
        PLUS,

        /**
         * -X. Arithmetic negation.
         */
        MINUS,

        /**
         * <tt>typeof X</tt>. Returns a string with the type of X.
         */
        TYPEOF,

        /**
         * <tt>void X</tt>. Returns undefined.
         */
        VOID, // TODO: Should we have VOID operator in the control flow graph?
    }

    /**
     * Returns the intermediate operator corresponding to the given ++ or -- operator.
     * @param unop a postfix unary operator
     * @return {@link Operator#INCREMENT} or {@link Operator#DECREMENT}.
     */
    public static Operator fromPostfixUnop(EPostfixUnop unop) {
        switch (unop) {
        case DECREMENT: return Operator.DECREMENT;
        case INCREMENT: return Operator.INCREMENT;
        default:
            throw new RuntimeException("Unknown postfix unop: " + unop);
        }
    }

    /**
     * Returns the operator corresponding to a given side-effect free prefix unary operator.
     * The <tt>delete</tt> operator has no unary operation in the flow graph, use {@link DeleteProperty} instead.
     * The {@link Operator#INCREMENT increment} and {@link Operator#DECREMENT decrement} in this class do not have
     * side-effects on its argument, but are returned by convention for their corresponding effective AST operator.
     * @param unop a prefix unary operator other than <tt>delete</tt>
     * @return an {@link Operator}
     */
    public static Operator fromPrefixUnop(EPrefixUnop unop) {
        switch (unop) {
        case COMPLEMENT: return Operator.COMPLEMENT;
        case DECREMENT: return Operator.DECREMENT;
        case DELETE: throw new RuntimeException("Delete is not a unary operation in the flow graph");
        case INCREMENT: return Operator.INCREMENT;
        case MINUS: return Operator.MINUS;
        case NOT: return Operator.NOT;
        case PLUS: return Operator.PLUS;
        case TYPEOF: return Operator.TYPEOF;
        case VOID: return Operator.VOID;
        default:
            throw new RuntimeException("Unknown prefix unop: " + unop);
        }
    }

    public UnaryOperation(int resultVar, Operator operator, int argVar) {
        super(resultVar);
        this.operator = operator;
        this.argVar = argVar;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public int getArgVar() {
        return argVar;
    }

    public void setArgVar(int argVar) {
        this.argVar = argVar;
    }
    @Override
    public boolean canThrowException() {
        return true;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.singleton(argVar);
    }

    @Override
    public void apply(AssignmentVisitor v) {
        v.caseUnaryOperation(this);
    }
    @Override
    public <Q, A> A apply(AssignmentQuestionAnswer<Q, A> v, Q arg) {
        return v.caseUnaryOperation(this, arg);
    }
}