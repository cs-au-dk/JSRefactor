package dk.brics.jspointers.cfg2dataflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.IStatement;
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
import dk.brics.jscontrolflow.statements.IPropertyAccessStatement;
import dk.brics.jscontrolflow.statements.IVariableAccessStatement;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.DeleteNode;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.ILoadFlowNode;
import dk.brics.jspointers.dataflow.IPropertyAccessFlowNode;
import dk.brics.jspointers.dataflow.IStoreFlowNode;
import dk.brics.jspointers.dataflow.IVariableAccessFlowNode;
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
import dk.brics.jsutil.Pair;

public class Controlflow2DataflowBinding implements IControlflow2DataflowBinding {
    private MultiMap<IStatement,FlowNode> properties = new MultiMap<IStatement,FlowNode>();
    private MultiMap<IVariableAccessStatement,IVariableAccessFlowNode> variables = new MultiMap<IVariableAccessStatement,IVariableAccessFlowNode>();
    private MultiMap<IStatement,FlowNode> invokes = new MultiMap<IStatement,FlowNode>();
    private MultiMap<IStatement,FlowNode> allocs = new MultiMap<IStatement,FlowNode>();
    private MultiMap<BinaryOperation,StubNode> inExps = new MultiMap<BinaryOperation, StubNode>();
    private MultiMap<CreateFunction,InitializeFunctionNode> functions = new MultiMap<CreateFunction, InitializeFunctionNode>();
    private MultiMap<GetNextProperty,LoadDynamicNode> getprtys = new MultiMap<GetNextProperty, LoadDynamicNode>();
    private Map<Function,ReachingDefinitions> reachingDefs = new HashMap<Function, ReachingDefinitions>();
    private Map<Function,PrivateVariables> privateVars = new HashMap<Function, PrivateVariables>();
    private Map<EnterWith, StubNode> with = new HashMap<EnterWith, StubNode>();
    private MultiMap<Pair<Function,Integer>, OutputPoint> var2outputPoints = new MultiMap<Pair<Function,Integer>, OutputPoint>();
	private MultiMap<VariableDefinition, OutputPoint> definitions;
	private MultiMap<VariableDefinition, InputPoint> uses;
    
    @SuppressWarnings("unchecked")
    private <T> Set<? extends T> lookupProperty(IStatement stm) {
        return (Set<? extends T>)properties.getView(stm);
    }
    @SuppressWarnings("unchecked")
    private <T> Set<? extends T> lookupInvoke(IStatement stm) {
        return (Set<? extends T>)invokes.getView(stm);
    }
    @SuppressWarnings("unchecked")
    private <T> Set<? extends T> lookupAlloc(IStatement stm) {
        return (Set<? extends T>)allocs.getView(stm);
    }
    
    public Set<? extends IPropertyAccessFlowNode> getPropertyAccess(IPropertyAccessStatement stm) {
        return lookupProperty(stm);
    }
    public Set<? extends ILoadFlowNode> getReadProperty(ReadProperty stm) {
        return lookupProperty(stm);
    }
    public Set<? extends IStoreFlowNode> getWriteProperty(WriteProperty stm) {
        return lookupProperty(stm);
    }
    public Set<? extends IInvocationFlowNode> getInvoke(InvokeStatement stm) {
        return lookupInvoke(stm);
    }
    public Set<? extends AllocNode> getNewObject(NewObject stm) {
        return lookupAlloc(stm);
    }
    public Set<? extends IPropertyAccessFlowNode> getWithScopeAccess(IVariableAccessStatement stm) {
        return lookupProperty(stm);
    }
    public Set<? extends StubNode> getInExpObjectArg(BinaryOperation stm) {
    	if (stm.getOperator() != BinaryOperation.Operator.IN)
    		throw new IllegalArgumentException(stm + " is not an IN operation");
    	return inExps.getView(stm);
    }
    public Set<? extends InitializeFunctionNode> getFunctionNodes(CreateFunction stm) {
    	return functions.getView(stm);
    }
    public Set<? extends LoadDynamicNode> getGetNextPropertyNodes(GetNextProperty stm) {
        return getprtys.getView(stm);
    }
    public ReachingDefinitions getReachingDefinitions(Function function) {
        return reachingDefs.get(function);
    }
    public PrivateVariables getPrivateVariables(Function function) {
        return privateVars.get(function);
    }
    public Set<? extends IVariableAccessFlowNode> getVariableAccess(IVariableAccessStatement stm) {
        return variables.getView(stm);
    }
    public StubNode getWithNode(EnterWith stm) {
    	return with.get(stm);
    }
    
    public Set<OutputPoint> getVariableOutputPoints(Function func, int var) {
        return var2outputPoints.getView(Pair.make(func,var));
    }
    
    @Override
    public void addReadProperty(ReadProperty stm, LoadNode node) {
        properties.add(stm, node);
    }

    @Override
    public void addReadProperty(ReadProperty stm, LoadDynamicNode node) {
        properties.add(stm, node);
    }

    @Override
    public void addWriteProperty(WriteProperty stm, StoreNode node) {
        properties.add(stm, node);
    }

    @Override
    public void addWriteProperty(WriteProperty stm, StoreDynamicNode node) {
        properties.add(stm, node);
    }

    @Override
    public void addCall(Call stm, InvokeNode node) {
        invokes.add(stm, node);
    }

    @Override
    public void addCallProperty(CallProperty stm, LoadAndInvokeNode node) {
        properties.add(stm, node);
        invokes.add(stm, node);
    }
    @Override
    public void addCallProperty(CallProperty stm, LoadDynamicNode load, InvokeNode invoke) {
        properties.add(stm, load);
        invokes.add(stm, invoke);
    }

    @Override
    public void addCallConstructor(CallConstructor stm, AllocNode alloc, InvokeNode invoke) {
        invokes.add(stm, invoke);
    }

    @Override
    public void addCallVariable(CallVariable stm, VarReadNode read, InvokeNode invoke) {
        invokes.add(stm, invoke);
        variables.add(stm, read);
    }

    @Override
    public void addCallVariable(CallVariable stm, VarReadInterscopeNode read, InvokeNode invoke) {
        invokes.add(stm, invoke);
        variables.add(stm, read);
    }

    @Override
    public void addCallVariable(CallVariable stm, VarReadGlobalNode read, InvokeNode invoke) {
        invokes.add(stm, invoke);
        variables.add(stm, read);
    }

    @Override
    public void addCallVariable(CallVariable stm, LoadNode load, WithScope withScope, InvokeNode invoke) {
        invokes.add(stm, invoke);
        properties.add(stm, load);
    }

    @Override
    public void addCallVariable(CallVariable stm, InvokeNode invoke) {
        invokes.add(stm, invoke);
    }

    @Override
    public void addReadVariable(ReadVariable stm, VarReadNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addReadVariable(ReadVariable stm, VarReadInterscopeNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addReadVariable(ReadVariable stm, VarReadGlobalNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addReadVariable(ReadVariable stm, LoadNode node, WithScope withScope) {
        properties.add(stm, node);
    }

    @Override
    public void addWriteVariable(WriteVariable stm, VarWriteNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addWriteVariable(WriteVariable stm, VarWriteInterscopeNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addWriteVariable(WriteVariable stm, VarWriteGlobalNode node) {
        variables.add(stm, node);
    }

    @Override
    public void addWriteVariable(WriteVariable stm, StoreIfPresentNode node, WithScope withScope) {
        properties.add(stm, node);
    }

    @Override
    public void addNewObject(NewObject stm, AllocNode node) {
        allocs.add(stm, node);
    }
    
    @Override
    public void addDeleteProperty(DeleteProperty stm, DeleteNode node) {
        properties.add(stm, node);
    }
    @Override
    public void addBinaryOperation(BinaryOperation stm, StubNode arg1, StubNode arg2) {
    	switch (stm.getOperator()) {
    	case IN:
    		inExps.add(stm, arg2);
    		break;
    	}
    }
    
    @Override
    public void addCreateFunction(CreateFunction stm, InitializeFunctionNode node) {
    	functions.add(stm, node);
    }
    
    @Override
    public void addGetNextProperty(GetNextProperty stm, LoadDynamicNode node) {
        getprtys.add(stm, node);
    }
    
    @Override
    public void setReachingDefinitions(Function function, PrivateVariables privateVars, ReachingDefinitions reachingDefs) {
        this.reachingDefs.put(function, reachingDefs);
        this.privateVars.put(function, privateVars);
    }
    
    @Override
    public void addWith(EnterWith stm, StubNode stub) {
    	with.put(stm, stub);
    }
    
    @Override
    public void addVariableOutputPoint(Function func, int var, OutputPoint op) {
        var2outputPoints.add(Pair.make(func, var), op);
    }
    
    @Override
    public void setDefUse(MultiMap<VariableDefinition, OutputPoint> defs, MultiMap<VariableDefinition, InputPoint> uses) {
    	definitions = defs;
    	this.uses = uses;
    }
    
    public MultiMap<VariableDefinition, OutputPoint> getDefinitions() {
		return definitions;
	}
    public MultiMap<VariableDefinition, InputPoint> getUses() {
		return uses;
	}

}
