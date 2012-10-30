package dk.brics.jscontrolflow.analysis.reachdef;

public class TemporaryVariable extends Variable {
    private int index;

    public TemporaryVariable(int index) {
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
        TemporaryVariable other = (TemporaryVariable) obj;
        if (index != other.index) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
    	return "~" + index;
    }
}
