package dk.brics.jspointers.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jscontrolflow.statements.IPropertyAccessStatement;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jsutil.MultiMap;

/**
 * Contains the nodes of a dataflow graph, and some useful sets and relations to the TAJS flowgraph it
 * was built from.
 * <p/>
 * This class does not maintain any invariants by itself - it is a completely logic-free container object.
 * 
 * @author Asger
 */
public class DataflowGraph {
    private MultiMap<InvokeStatement, IInvocationFlowNode> calls = new MultiMap<InvokeStatement, IInvocationFlowNode>();
    private Map<Function, ReturnNode> normalReturns = new HashMap<Function, ReturnNode>();
    private Map<Function, ReturnNode> exceptionalReturns = new HashMap<Function, ReturnNode>();
    private InitializeNode initializer;
    private Set<Function> topLevels = new HashSet<Function>();
    private Set<Function> harnessFunctions = new HashSet<Function>();
    private Map<Function,FunctionInstanceNode> functionInstanceFlowNodes = new HashMap<Function, FunctionInstanceNode>();
    private Map<String,Function> namedHarnessFunctions = new HashMap<String, Function>();
    private MultiMap<Function,FlowNode> function2flownodes = new MultiMap<Function,FlowNode>();

    private MultiMap<IPropertyAccessStatement,ILoadFlowNode> loads = new MultiMap<IPropertyAccessStatement, ILoadFlowNode>();
    private MultiMap<WriteProperty,IStoreFlowNode> stores = new MultiMap<WriteProperty, IStoreFlowNode>();

    public MultiMap<IPropertyAccessStatement, ILoadFlowNode> getLoads() {
        return loads;
    }
    public MultiMap<WriteProperty, IStoreFlowNode> getStores() {
        return stores;
    }

    private Set<Function> entryFunctions = new HashSet<Function>();

    public Set<Function> getEntryFunctions() {
        return entryFunctions;
    }

    public MultiMap<Function, FlowNode> getFunctionFlownodes() {
        return function2flownodes;
    }
    public Map<String, Function> getNamedHarnessFunctions() {
        return namedHarnessFunctions;
    }
    public Set<Function> getHarnessFunctions() {
        return harnessFunctions;
    }

    public Set<Function> getTopLevels() {
        return topLevels;
    }

    public Map<Function, FunctionInstanceNode> getFunctionInstanceFlowNodes() {
        return functionInstanceFlowNodes;
    }

    public InitializeNode getInitializer() {
        return initializer;
    }
    public void setInitializer(InitializeNode initializer) {
        this.initializer = initializer;
    }
    public MultiMap<InvokeStatement, IInvocationFlowNode> getCalls() {
        return calls;
    }
    public Map<Function, ReturnNode> getNormalReturns() {
        return normalReturns;
    }
    public Map<Function, ReturnNode> getExceptionalReturns() {
        return exceptionalReturns;
    }
}
