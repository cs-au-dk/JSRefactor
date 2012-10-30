package dk.brics.jspointers.dataflow;

import dk.brics.jscontrolflow.scope.Scope;

public interface INonGlobalVariableAccessFlowNode extends IVariableAccessFlowNode {
    Scope getScope();
}
