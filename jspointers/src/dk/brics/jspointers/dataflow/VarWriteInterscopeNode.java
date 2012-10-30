package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * Assign to a variably in another function's scope.
 * <p/>
 * var = x;
 * 
 * @author Asger
 *
 */
public class VarWriteInterscopeNode extends FlowNode implements INonGlobalVariableAccessFlowNode, IVariableWriteFlowNode  {
    private String varName;
    private Scope scope;
    private InputPoint value = new InputPoint(this);
    private InputPoint functionInstance = new InputPoint(this);
    private int depth;

    public VarWriteInterscopeNode(String varName, Scope targetScope, int depth) {
        this.varName = varName;
        this.scope = targetScope;
        this.depth = depth;
    }

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    @Override
    public InputPoint getValue() {
        return value;
    }
    @Override
    public String getVarName() {
        return varName;
    }
    public Scope getScope() {
        return scope;
    }
    public int getDepth() {
        return depth;
    }
    @Override
    public boolean isPurelyLocal() {
        return false;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarWriteInterscope(this);
    }

    @Override
    public List<InputPoint> getInputPoints() {
        return Arrays.asList(value, functionInstance);
    }
    @Override
    public List<OutputPoint> getOutputPoints() {
        return Collections.emptyList();
    }
}
