package dk.brics.jscontrolflow.analysis.reachdef;

public class ParameterVariableDefinition extends VariableDefinition {
    private int index;

    public ParameterVariableDefinition(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
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
        ParameterVariableDefinition other = (ParameterVariableDefinition) obj;
        if (index != other.index) {
            return false;
        }
        return true;
    }

    @Override
    public void apply(VariableDefinitionVisitor v) {
        v.caseParameter(this);
    }

    @Override
    public <Q, A> A apply(VariableDefinitionQuestionAnswer<Q, A> v, Q arg) {
        return v.caseParameter(this, arg);
    }
    
    @Override
    public String toString() {
    	return "param " + index;
    }

}
