package dk.brics.jspointers.cfg2dataflow;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import dk.brics.jspointers.dataflow.AllocNode;
import dk.brics.jspointers.dataflow.CoerceToObject;
import dk.brics.jspointers.dataflow.CoerceToPrimitive;
import dk.brics.jspointers.dataflow.ConstNode;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.FlowNodeVisitor;
import dk.brics.jspointers.dataflow.FunctionInstanceNode;
import dk.brics.jspointers.dataflow.GlobalExceptionNode;
import dk.brics.jspointers.dataflow.IdentityNode;
import dk.brics.jspointers.dataflow.InitializeFunctionNode;
import dk.brics.jspointers.dataflow.InitializeNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InterscopeIdentityNode;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.InvokeResultNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.LoadDirectNode;
import dk.brics.jspointers.dataflow.LoadDynamicNode;
import dk.brics.jspointers.dataflow.LoadNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.dataflow.PlusNode;
import dk.brics.jspointers.dataflow.ReturnNode;
import dk.brics.jspointers.dataflow.SetPrototype;
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
import dk.brics.jspointers.lattice.values.BasicType;
import dk.brics.jspointers.solver.Analysis;
import dk.brics.jspointers.solver.Callback;

public class BasicTypeAnalysis implements Analysis<FlowNode, OutputPoint, EnumSet<BasicType>> {

    private static final EnumSet<BasicType> ALL = EnumSet.allOf(BasicType.class);
    private FunctionInstanceNode root;

    public BasicTypeAnalysis(FunctionInstanceNode root) {
        this.root = root;
    }

    @Override
    public EnumSet<BasicType> emptyValue() {
        return EnumSet.noneOf(BasicType.class);
    }

    @Override
    public EnumSet<BasicType> bottom(OutputPoint key) {
        return EnumSet.noneOf(BasicType.class);
    }

    @Override
    public Iterable<? extends FlowNode> initialNodes() {
        return Collections.singletonList(root);
    }

    @Override
    public Iterable<? extends FlowNode> getInitialDependencies(OutputPoint key) {
        List<FlowNode> list = new LinkedList<FlowNode>();
        for (InputPoint ip : key.getDestinations()) {
            list.add(ip.getFlowNode());
        }
        return list;
    }

    @Override
    public EnumSet<BasicType> clone(EnumSet<BasicType> value) {
        return value.clone();
    }

    @Override
    public void transfer(FlowNode t, final Callback<OutputPoint, EnumSet<BasicType>> callback) {
        t.apply(new FlowNodeVisitor() {

            private EnumSet<BasicType> read(InputPoint ip) {
                EnumSet<BasicType> set = EnumSet.noneOf(BasicType.class);
                for (OutputPoint op : ip.getSources()) {
                    set.addAll(callback.readableValueAt(op));
                }
                return set;
            }

            private void addToOutputPoint(OutputPoint op, BasicType type) {
                if (callback.modifiableValueAt(op).add(type)) {
                    callback.markChanged(op);
                }
            }
            private void addToOutputPoint(OutputPoint op, EnumSet<BasicType> set) {
                if (callback.modifiableValueAt(op).addAll(set)) {
                    callback.markChanged(op);
                }
            }

            @Override
            public void caseConst(ConstNode node) {
                addToOutputPoint(node.getResult(), node.getValue().getBasicType());
            }

            @Override
            public void caseInvoke(InvokeNode node) {
            }

            @Override
            public void caseLoad(LoadNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseLoadDirect(LoadDirectNode node) {
            	addToOutputPoint(node.getResult(), ALL);
            }
            
            @Override
            public void casePlus(PlusNode node) {
                EnumSet<BasicType> arg = read(node.getArgument());
                if (arg.contains(BasicType.STRING) || arg.contains(BasicType.OBJECT)) {
                    addToOutputPoint(node.getResult(), BasicType.STRING);
                }
                if (arg.contains(BasicType.NUMBER) || arg.contains(BasicType.OBJECT)) {
                    addToOutputPoint(node.getResult(), BasicType.NUMBER);
                }
            }

            @Override
            public void caseStoreIfPresent(StoreIfPresentNode node) {
            }

            @Override
            public void caseStore(StoreNode node) {
            }
            
            @Override
            public void caseSetPrototype(SetPrototype node) {
            }

            @Override
            public void caseVarReadInterscope(VarReadInterscopeNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseVarWriteInterscope(VarWriteInterscopeNode node) {
            }

            @Override
            public void caseReturn(ReturnNode node) {
            }

            @Override
            public void caseInvokeResult(InvokeResultNode node) {
                addToOutputPoint(node.getResult(), ALL);
                addToOutputPoint(node.getExceptionalResult(), ALL);
            }

            @Override
            public void caseStoreDynamic(StoreDynamicNode node) {
            }

            @Override
            public void caseLoadDynamic(LoadDynamicNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseInitialize(InitializeNode node) {
            }

            @Override
            public void caseInitializeFunction(InitializeFunctionNode node) {
                addToOutputPoint(node.getResult(), BasicType.OBJECT);
            }

            @Override
            public void caseFunctionInstance(FunctionInstanceNode node) {
                addToOutputPoint(node.getResult(), BasicType.OBJECT);
            }

            @Override
            public void caseVarRead(VarReadNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseVarWrite(VarWriteNode node) {
            }

            @Override
            public void caseAlloc(AllocNode node) {
                addToOutputPoint(node.getResult(), BasicType.OBJECT);
            }

            @Override
            public void caseVarReadGlobal(VarReadGlobalNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseVarWriteGlobal(VarWriteGlobalNode node) {
            }

            @Override
            public void caseGlobalException(GlobalExceptionNode node) {
                addToOutputPoint(node.getResult(), BasicType.OBJECT);
            }

            @Override
            public void caseIdentity(IdentityNode node) {
                addToOutputPoint(node.getResult(), read(node.getValue()));
            }

            @Override
            public void caseCoerceToPrimitive(CoerceToPrimitive node) {
                addToOutputPoint(node.getExceptionalResult(), ALL);
            }
            
            @Override
            public void caseCoerceToObject(CoerceToObject node) {
            	addToOutputPoint(node.getResult(), BasicType.OBJECT);
            }

            @Override
            public void caseLoadAndInvoke(LoadAndInvokeNode node) {
            }

            @Override
            public void caseInterscopeIdentity(InterscopeIdentityNode node) {
                addToOutputPoint(node.getResult(), ALL);
            }

            @Override
            public void caseStub(StubNode node) {
            }

        });
    }



}
