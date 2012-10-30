package dk.brics.jsrefactoring.evaluate;

import java.io.File;

import dk.brics.jsparser.node.IFunction;
import dk.brics.jsrefactoring.Master;

public class ShowUnreachableFunctions {
    public static void main(String[] args) throws Exception {
        System.out.printf("Analyzing %s\n", args[0]);
        Master m = new Master(new File(args[0]));
        
        int total = 0;
        for (IFunction func : m.getAllNodesOfType(IFunction.class)) {
            if (m.isNativeCode(func))
                continue;
            if (!m.isFunctionReachable(func)) {
                System.out.printf("%4d %s\n", func.getLbrace().getLine(), getFuncName(func));
                total++;
            }
        }
        
        System.out.printf("\nTotal: %d", total);
    }
    public static String getFuncName(IFunction func) {
        if (func.getName() == null) {
            return "<anonymous>";
        } else {
            return func.getName().getText();
        }
    }
}
