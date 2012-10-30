package dk.brics.jspointers.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.ast2cfg.AstBinding;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.FunctionApplyNative;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;

public class TestDisplay {
    private ABody ast;
    private Function cfg;
    private DisplayUtil display;
    private DataflowGraph dataflow;
    private AstBinding binding;

    public TestDisplay(ABody ast, Function cfg, AstBinding binding, DataflowGraph dataflow, DisplayUtil display) {
        this.ast = ast;
        this.cfg = cfg;
        this.display = display;
        this.binding = binding;
        this.dataflow = dataflow;
    }

    public void print() {
        ast.apply(new DepthFirstAdapter() {
            @Override
            public void inAInvokeExp(AInvokeExp node) {
                List<String> targets = new ArrayList<String>();
                for (InvokeStatement stmt : binding.getInvokeStatements(node)) {
                    for (IInvocationFlowNode invoke : dataflow.getCalls().getView(stmt)) {
                        for (Value val : display.getFunctionArgs(invoke)) {
                            if (!(val instanceof FunctionValue)) {
                                continue;
                            }
                            FunctionValue fval = (FunctionValue)val;

                            if (fval == FunctionCallNative.Instance || fval == FunctionApplyNative.Instance) {
                                Set<Value> targets2 = display.getValuesAtInputPoint(invoke.getBase());
                                StringBuffer targetStr = new StringBuffer(getFunctionName(fval, dataflow));
                                targetStr.append("->{");
                                for (Value val2 : targets2) {
                                    if (!(val2 instanceof FunctionValue)) {
                                        continue;
                                    }
                                    FunctionValue fval2 = (FunctionValue)val2;
                                    targetStr.append(getFunctionName(fval2, dataflow));
                                }
                                targetStr.append("}");
                                targets.add(targetStr.toString());
                            } else {
                                targets.add(getFunctionName(fval, dataflow));
                            }
                        }
                    }
                }
                System.out.printf("At line %3d %30s can call %s\n", node.getLparen().getLine(), nameExpression(node.getFunctionExp()), targets);
            }
        });
    }
    private String nameExpression(final PExp exp) {
        return exp.apply(new AnswerAdapter<String>() {
            @Override
            public String caseAPropertyExp(APropertyExp node) {
                return "..." + node.getName().getText();
            }
            @Override
            public String defaultNode(Node node) {
                return node.toString().trim();
            }
        });
    }
    private static String getFunctionName(FunctionValue value, DataflowGraph dataflow) {
        if (value instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)value;
            Function callee = uf.getFunction();
            if (dataflow.getHarnessFunctions().contains(callee)) {
                if (callee.getName() != null) {
                    return "native:" + callee.getName();
                } else {
                    return "native:" + callee.getSourceLocation().getFile().getName() + ":" + callee.getSourceLocation().getLineNumber();
                }
            } else {
                if (callee.getName() != null) {
                    return callee.getName() + ":" + callee.getSourceLocation().getLineNumber();
                } else {
                    return callee.getSourceLocation().getFile().getName() + ":" + callee.getSourceLocation().getLineNumber();
                }
            }
        } else {
            NativeFunctionValue nf = (NativeFunctionValue)value;
            return "native:" + nf.getPrettyName();
        }
    }
}
