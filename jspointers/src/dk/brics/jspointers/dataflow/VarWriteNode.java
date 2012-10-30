package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;

public class VarWriteNode extends FlowNode implements INonGlobalVariableAccessFlowNode, IVariableWriteFlowNode  {
    private String varName;
    private InputPoint value = new InputPoint(this);
    private Scope scope;

    public VarWriteNode(String varName, Scope scope) {
        assert scope.getParentScope() != null : "VarWriteNode cannot write to global variables";
        this.varName = varName;
        this.scope = scope;
    }

    @Override
    public String getVarName() {
        return varName;
    }
    
    @Override
    public InputPoint getValue() {
        return value;
    }
    public Scope getScope() {
        return scope;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(value);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarWrite(this);
    }
}
