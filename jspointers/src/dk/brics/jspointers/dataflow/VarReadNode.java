package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;

public class VarReadNode extends FlowNode implements INonGlobalVariableAccessFlowNode, IVariableAccessReadFlowNode  {
    private String varName;
    private InputPoint functionInstance = new InputPoint(this);
    private OutputPoint result = new OutputPoint(this);
    private Scope scope;

    public VarReadNode(String varName, Scope scope) {
        assert scope.getParentScope() != null : "VarReadNode cannot read from global variables";
        this.varName = varName;
        this.scope = scope;
    }

    /**
     * The function instance - needed for context sensitivity.
     */
    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public String getVarName() {
        return varName;
    }
    public OutputPoint getResult() {
        return result;
    }
    public Scope getScope() {
        return scope;
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Collections.singletonList(functionInstance);
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarRead(this);
    }

    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.singletonList(result);
    }
}
