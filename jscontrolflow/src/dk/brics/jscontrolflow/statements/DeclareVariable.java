package dk.brics.jscontrolflow.statements;

import java.util.Collection;
import java.util.Collections;


public class DeclareVariable extends NonAssignment {
    private String varName;
    private Kind kind;

    public enum Kind {
        /**
         * Declared by a <tt>var</tt> statement somewhere in the body
         */
        VAR,

        /**
         * Variable is a parameter
         */
        PARAMETER,

        /**
         * Declared by a function declaration statement somewhere in the body
         */
        FUNCTION,

        /**
         * Implicitly declared "arguments" variable
         */
        ARGUMENTS,
        
        /**
         * Function expression referring to its own name.
         */
        SELF,
    }

    public DeclareVariable(String varName, Kind kind) {
        this.varName = varName;
        this.kind = kind;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public boolean canThrowException() {
        return false;
    }
    @Override
    public Collection<Integer> getReadVariables() {
        return Collections.<Integer>emptySet();
    }

    @Override
    public void apply(StatementVisitor v) {
        v.caseDeclareVariable(this);
    }
    @Override
    public <Q, A> A apply(StatementQuestionAnswer<Q, A> v, Q arg) {
        return v.caseDeclareVariable(this, arg);
    }
}
