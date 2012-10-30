package dk.brics.jsrefactoring.callgraph;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.Master;

public class PrintCallInfo {
    public static void main(String[] args) throws Exception {
        
        Master master = new Master(new File(args[0]));
        
        for (IInvocationNode invoke : master.getAllNodesOfType(IInvocationNode.class)) {
            if (master.isNativeCode(invoke))
                continue;
            
            File file = master.getSourceFile(invoke);
            int line = master.getTranslatedLineNumber(invoke);
            
            Set<String> targets = new HashSet<String>();
            for (FunctionValue farg : master.getCalledFunctions(invoke)) {
                targets.add(getFunctionName(farg, master));
            }
            System.out.printf("%20s:%-3d %30s can call %s\n",
                    file.getName(), 
                    line, 
                    nameExpression(invoke.getFunctionExp()), targets);
        }
    }
    
    private static String nameExpression(final NodeInterface exp) {
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
    private static String getFunctionName(FunctionValue value, Master master) {
        if (value instanceof UserFunctionValue) {
            UserFunctionValue uf = (UserFunctionValue)value;
            Function callee = uf.getFunction();
            if (master.isNativeCode(uf)) {
                // TODO: Function.getSourceLocation() is only meant for debugging - should use AST information instead
                IFunction astNode = master.getFunctionNode(uf);
                if (astNode.getName() != null) {
                    return "native:" + astNode.getName().getText();
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
