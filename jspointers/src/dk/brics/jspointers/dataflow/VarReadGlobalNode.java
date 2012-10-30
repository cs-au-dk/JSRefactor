package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.statements.IVariableAccessStatement;

public class VarReadGlobalNode extends FlowNode implements IVariableAccessReadFlowNode {
    private String varName;
    private InputPoint functionInstance = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);

    public VarReadGlobalNode(String varName) {
        this.varName = varName;
    }

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public String getVarName() {
        return varName;
    }
    public OutputPoint getResult() {
        return result;
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarReadGlobal(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(functionInstance);
    }

    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
