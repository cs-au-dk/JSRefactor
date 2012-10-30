package dk.brics.jspointers.display;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
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

public class DataflowToDot {
    private static class Record {
        int id;
        String text;
        public Record(int id, String text) {
            this.id = id;
            this.text = text;
        }
    }
    public static void print(Set<FlowNode> nodes, File file) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            print(nodes, writer, file.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    public static void print(Set<FlowNode> nodes, final PrintWriter writer, final String debugName) {
        final Map<FlowNode,Integer> node2id = new HashMap<FlowNode,Integer>();
        final Map<InputPoint,Integer> input2id = new HashMap<InputPoint,Integer>();
        final Map<OutputPoint,Integer> output2id = new HashMap<OutputPoint,Integer>();
        int nextid = 0;
        writer.printf("digraph {\n");
        for (final FlowNode node : nodes) {
            if (node2id.containsKey(node)) {
                continue;
            }
            int index = nextid++;
            node2id.put(node, index);
            final List<Record> inputs = new ArrayList<Record>();
            final List<Record> outputs = new ArrayList<Record>();
            final StringBuffer name = new StringBuffer();
            node.apply(new FlowNodeVisitor() {
                int recordId = 0;
                private void addInput(String text, InputPoint ip) {
                    int id = recordId++;
                    inputs.add(new Record(id, text));
                    input2id.put(ip, id);
                }
                private void addIgnoredInput(InputPoint ip) {
                    if (ip.getSources().size() > 0) {
                        System.err.printf("%s has ignored input point with sources (%s)\n", ip.getFlowNode(), debugName);
                        // show the erronous input point so we can debug it
                        // find an input named IGNORE in the dataflow graph so see what went wrong
                        addInput("IGNORE", ip);
                    } else {
                        input2id.put(ip, -1);
                    }
                }
                private void addOutput(String text, OutputPoint op) {
                    int id = recordId++;
                    outputs.add(new Record(id, text));
                    output2id.put(op, id);
                }
                @Override
                public void caseInterscopeIdentity(InterscopeIdentityNode node) {
                    addInput("func-instance", node.getFunctionInstance());
                    name.append("interscope-id");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseStub(StubNode node) {
                    addInput("input", node.getInput());
                    name.append("stub");
                }
                @Override
                public void caseLoadAndInvoke(LoadAndInvokeNode node) {
                    addInput("base", node.getBase());
                    for (int i=0; i<node.getArguments().size(); i++) {
                        addInput("arg"+i, node.getArguments().get(i));
                    }
                    name.append("load-and-invoke[").append(node.getProperty()).append("]");
                    addOutput("func", node.getInvokedFunction());
                }
                @Override
                public void caseCoerceToPrimitive(CoerceToPrimitive node) {
                    addInput("value", node.getValue());
                    name.append("coerce-to-primitive");
                    addOutput("exceptional-result", node.getExceptionalResult());
                }
                @Override
                public void caseCoerceToObject(CoerceToObject node) {
                	addInput("argument", node.getArgument());
                	name.append("coerce-to-object");
                	addOutput("result", node.getResult());
                }
                @Override
                public void caseIdentity(IdentityNode node) {
                    addInput("value", node.getValue());
                    name.append("identity");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseVarReadGlobal(VarReadGlobalNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    name.append("var-read-global[").append(node.getVarName()).append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseVarWriteGlobal(VarWriteGlobalNode node) {
                    addInput("value", node.getValue());
                    name.append("var-write-global[").append(node.getVarName()).append("]");
                }
                @Override
                public void caseAlloc(AllocNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    name.append("alloc");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseConst(ConstNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    addOutput(null, node.getResult());
                    name.append(node.getValue().toString());
                }
                @Override
                public void caseInvoke(InvokeNode node) {
                    if (node.isThisArgOmitted() && !node.isConstructor()) {
                        addIgnoredInput(node.getBase());
                    } else {
                        addInput("this", node.getBase());
                    }
                    addInput("func", node.getFunc());
                    for (int i=0; i<node.getArguments().size(); i++) {
                        addInput("arg" + i, node.getArguments().get(i));
                    }
                    name.append("invoke");
                }
                @Override
                public void caseLoad(LoadNode node) {
                    addInput("base", node.getBase());
                    name.append("load[").append(node.getProperty()).append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseLoadDirect(LoadDirectNode node) {
                    addInput("base", node.getBase());
                    name.append("load-direct[").append(node.getProperty()).append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void casePlus(PlusNode node) {
                    addInput("arg", node.getArgument());
                    name.append("plus");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseSetPrototype(SetPrototype node) {
                	addInput("base", node.getBase());
                	addInput("value", node.getValue());
                	name.append("set-prototype");
                }
                @Override
                public void caseStoreIfPresent(StoreIfPresentNode node) {
                    addInput("base", node.getBase());
                    addInput("value", node.getValue());
                    name.append("store-if-present[").append(node.getProperty()).append("]");
                }
                @Override
                public void caseStore(StoreNode node) {
                    addInput("base", node.getBase());
                    addInput("value", node.getValue());
                    name.append("store[").append(node.getProperty()).append("]");
                }
                @Override
                public void caseVarRead(VarReadNode node) {
                    String func = funcName(node.getScope().getAncestorScope(Function.class));
                    addInput("function-instance", node.getFunctionInstance());
                    name.append("var-read[").append(func).append(",").append(node.getVarName()).append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseVarWrite(VarWriteNode node) {
                    addInput("value", node.getValue());
                    String func = funcName(node.getScope().getAncestorScope(Function.class));
                    name.append("var-write[").append(func).append(",").append(node.getVarName()).append("]");
                }
                @Override
                public void caseVarReadInterscope(VarReadInterscopeNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    String func = funcName(node.getScope().getAncestorScope(Function.class));
                    name.append("var-read-interscope[")
                    .append(func)
                    .append(",")
                    .append(node.getVarName())
                    .append(",")
                    .append(node.getDepth())
                    .append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseVarWriteInterscope(VarWriteInterscopeNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    addInput("value", node.getValue());
                    String func = funcName(node.getScope().getAncestorScope(Function.class));
                    name.append("var-write-interscope[")
                    .append(func)
                    .append(",")
                    .append(node.getVarName())
                    .append(",")
                    .append(node.getDepth())
                    .append("]");
                }
                @Override
                public void caseReturn(ReturnNode node) {
                    addInput("value", node.getValue());
                    String func = funcName(node.getFunction());
                    if (node.isExceptional()) {
                        name.append("exceptional-return");
                    } else {
                        name.append("return");
                    }
                    name.append("[").append(func).append("]");
                }
                @Override
                public void caseInvokeResult(InvokeResultNode node) {
                    if (!node.isConstructor()) {
                        addIgnoredInput(node.getAllocatedObject());
                    } else {
                        addInput("this", node.getAllocatedObject());
                    }
                    addInput("func", node.getFunc());
                    name.append("invoke-result");
                    addOutput("normal", node.getResult());
                    addOutput("exception", node.getExceptionalResult());
                }
                @Override
                public void caseLoadDynamic(LoadDynamicNode node) {
                    addInput("base", node.getBase());
                    addInput("property", node.getProperty());
                    name.append("load-dynamic");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseStoreDynamic(StoreDynamicNode node) {
                    addInput("base", node.getBase());
                    addInput("property", node.getProperty());
                    addInput("value", node.getValue());
                    name.append("store-dynamic");
                }
                @Override
                public void caseInitialize(InitializeNode node) {
                    name.append("initialize");
                }
                @Override
                public void caseInitializeFunction(InitializeFunctionNode node) {
                    addInput("outer", node.getOuterFunction());
                    name.append("initialize-function[").append(funcName(node.getFunction())).append("]");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseFunctionInstance(FunctionInstanceNode node) {
                    name.append("function-instance");
                    addOutput(null, node.getResult());
                }
                @Override
                public void caseGlobalException(GlobalExceptionNode node) {
                    addInput("function-instance", node.getFunctionInstance());
                    name.append("global-exception");
                    addOutput(null, node.getResult());
                }
                private String funcName(Function func) {
                    return func.getName() == null ? func.getSourceLocation().toString() : func.getName();
                }
            });
            StringBuffer label = new StringBuffer("{");
            if (inputs.size() > 0) {
                label.append("{");
            }
            for (int i=0; i<inputs.size(); i++) {
                if (i > 0) {
                    label.append("|");
                }
                Record rec = inputs.get(i);
                label.append("<").append(rec.id).append("> ").append(recordEscape(rec.text));
            }
            if (inputs.size() > 0) {
                label.append("}|");
            }
            label.append(recordEscape(name.toString()));
            if (outputs.size() > 0) {
                label.append("|{");
            }
            for (int i=0; i<outputs.size(); i++) {
                if (i > 0) {
                    label.append("|");
                }
                Record rec = outputs.get(i);
                if (rec.text == null) {
                    rec.text = "result";
                }
                label.append("<").append(rec.id).append("> ").append(recordEscape(rec.text));
            }
            if (outputs.size() > 0) {
                label.append("}");
            }
            label.append("}");
            writer.printf("    %d [shape=record,label=\"%s\"];\n", index, label);
        }

        // print edges
        for (FlowNode node : nodes) {
            int nodeId = node2id.get(node);
            for (InputPoint ip : node.getInputPoints()) {
                int ipId = input2id.get(ip);
                for (OutputPoint op : ip.getSources()) {
                    if (!node2id.containsKey(op.getFlowNode())) {
                        int id = nextid++;
                        node2id.put(op.getFlowNode(), id);
                        writer.printf("    %d [label=\"UNKN: %s\"];\n", id, op.getFlowNode().getClass().getSimpleName());
                        writer.printf("    %d -> %d:%d\n", id, nodeId, ipId);
                        continue;
                    }
                    if (!output2id.containsKey(op)) {
                        throw new RuntimeException("Missing output point for " + op.getFlowNode());
                    }
                    int opId = output2id.get(op);
                    int dstNodeId = node2id.get(op.getFlowNode());
                    writer.printf("    %d:%d -> %d:%d\n", dstNodeId, opId, nodeId, ipId);
                }
            }
            // connect invocations to their results
            if (node instanceof InvokeResultNode) {
                InvokeResultNode res = (InvokeResultNode)node;
                int resNodeId = node2id.get(res);
                int invokeNodeId = node2id.get(res.getInvocation());
                writer.printf("    %d -> %d [style=dotted, arrowType=none]\n", invokeNodeId, resNodeId);
            }
        }

        // close digraph
        writer.printf("}\n");
        writer.flush();
    }

    private static String recordEscape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case ' ':
            case '<':
            case '>':
            case '|':
            case '{':
            case '}':
            case '\\':
            case '"':
                b.append('\\').append(ch);
                break;
            default:
                b.append(ch);
                break;
            }
        }
        return b.toString();
    }

}
