package dk.brics.jscontrolflow.analysis.reachdef;

import dk.brics.jscontrolflow.display.Statement2Dot;
import dk.brics.jscontrolflow.statements.Assignment;

public class StatementVariableDefinition extends VariableDefinition {
    private Assignment statement;

    public StatementVariableDefinition(Assignment statement) {
        this.statement = statement;
    }

    public Assignment getStatement() {
        return statement;
    }
    public void setStatement(Assignment statement) {
        this.statement = statement;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
        + ((statement == null) ? 0 : statement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatementVariableDefinition other = (StatementVariableDefinition) obj;
        if (statement == null) {
            if (other.statement != null) {
                return false;
            }
        } else if (!statement.equals(other.statement)) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(VariableDefinitionVisitor v) {
        v.caseStatement(this);
    }

    @Override
    public <Q, A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg) {
        return v.caseStatement(this, arg);
    }
    
    @Override
    public String toString() {
    	return Statement2Dot.toDot(statement);
    }

}