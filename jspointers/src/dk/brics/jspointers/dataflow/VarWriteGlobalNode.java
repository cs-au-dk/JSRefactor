package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

public class VarWriteGlobalNode extends FlowNode implements IVariableWriteFlowNode {
    private String varName;
    private InputPoint value = new InputPoint(this);

    public VarWriteGlobalNode(String varName) {
        this.varName = varName;
    }

    @Override
    public InputPoint getValue() {
        return value;
    }
    @Override
    public String getVarName() {
        return varName;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarWriteGlobal(this);
    }
    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(value);
    }

    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
