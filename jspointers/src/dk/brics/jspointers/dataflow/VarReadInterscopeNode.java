package dk.brics.jspointers.dataflow;

import java.util.Collections;
import java.util.List;

import dk.brics.jscontrolflow.scope.Scope;

/**
 * Read a variable from another function's scope.
 * <p/>
 * x = var;
 */
public class VarReadInterscopeNode extends FlowNode implements INonGlobalVariableAccessFlowNode, IVariableAccessReadFlowNode {
    private OutputPoint result = new OutputPoint(this);
    private String varname;
    private Scope scope;
    private int depth;
    private InputPoint functionInstance = new InputPoint(this);

    public VarReadInterscopeNode(String varname, Scope targetScope, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("depth<1. Use VarReadNode instead");
        }
        this.varname = varname;
        this.scope = targetScope;
        this.depth = depth;
    }

    public InputPoint getFunctionInstance() {
        return functionInstance;
    }
    public OutputPoint getResult() {
        return result;
    }

    public Scope getScope() {
        return scope;
    }
    public String getVarName() {
        return varname;
    }
    public int getDepth() {
        return depth;
    }
    @Override
    public boolean isPurelyLocal() {
        return true;
    }

    @Override
    public void apply(FlowNodeVisitor visitor) {
        visitor.caseVarReadInterscope(this);
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
