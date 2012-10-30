package dk.brics.jspointers.cfg2dataflow;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.Call;
import dk.brics.jscontrolflow.statements.CallConstructor;
import dk.brics.jscontrolflow.statements.CallProperty;
import dk.brics.jscontrolflow.statements.CallVariable;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.DeleteNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadDynamicNode;
import dk.brics.jspointers.dataflow.LoadNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.dataflow.StoreDynamicNode;
import dk.brics.jspointers.dataflow.StoreIfPresentNode;
import dk.brics.jspointers.dataflow.StoreNode;
import dk.brics.jspointers.dataflow.StubNode;
import dk.brics.jspointers.dataflow.VarReadGlobalNode;
import dk.brics.jspointers.dataflow.VarReadInterscopeNode;
import dk.brics.jspointers.dataflow.VarReadNode;
import dk.brics.jspointers.dataflow.VarWriteGlobalNode;
import dk.brics.jspointers.dataflow.VarWriteInterscopeNode;
import dk.brics.jspointers.dataflow.VarWriteNode;
import dk.brics.jsutil.MultiMap;

public interface IControlflow2DataflowBinding {
    void addReadProperty(ReadProperty stm, LoadNode node);
    void addReadProperty(ReadProperty stm, LoadDynamicNode node);

    void addWriteProperty(WriteProperty stm, StoreNode node);
    void addWriteProperty(WriteProperty stm, StoreDynamicNode node);

    void addCall(Call stm, InvokeNode node);
    void addCallProperty(CallProperty stm, LoadAndInvokeNode node);
    void addCallProperty(CallProperty stm, LoadDynamicNode load, InvokeNode invoke);
    void addCallConstructor(CallConstructor stm, AllocNode alloc, InvokeNode invoke);
    void addCallVariable(CallVariable stm, VarReadNode read, InvokeNode invoke);
    void addCallVariable(CallVariable stm, VarReadInterscopeNode read, InvokeNode invoke);
    void addCallVariable(CallVariable stm, VarReadGlobalNode read, InvokeNode invoke);
    void addCallVariable(CallVariable stm, LoadNode load, WithScope withScope, InvokeNode invoke);
    void addCallVariable(CallVariable stm, InvokeNode invoke);

    void addReadVariable(ReadVariable stm, VarReadNode node);
    void addReadVariable(ReadVariable stm, VarReadInterscopeNode node);
    void addReadVariable(ReadVariable stm, VarReadGlobalNode node);
    void addReadVariable(ReadVariable stm, LoadNode node, WithScope withScope);

    void addWriteVariable(WriteVariable stm, VarWriteNode node);
    void addWriteVariable(WriteVariable stm, VarWriteInterscopeNode node);
    void addWriteVariable(WriteVariable stm, VarWriteGlobalNode node);
    void addWriteVariable(WriteVariable stm, StoreIfPresentNode node, WithScope withScope);

    void addNewObject(NewObject stm, AllocNode node);
    
    void addDeleteProperty(DeleteProperty stm, DeleteNode node);
	void addBinaryOperation(BinaryOperation stm, StubNode arg1, StubNode arg2);
	void addCreateFunction(CreateFunction stm, InitializeFunctionNode node);
    void addGetNextProperty(GetNextProperty stm, LoadDynamicNode node);
    void setReachingDefinitions(Function function, PrivateVariables privateVars, ReachingDefinitions reachingDefs);
	void addWith(EnterWith stm, StubNode stub);
	
	void addVariableOutputPoint(Function func, int var, OutputPoint op);
	
	void setDefUse(MultiMap<VariableDefinition,OutputPoint> defs, MultiMap<VariableDefinition,InputPoint> uses);
}
