package dk.brics.jscontrolflow;

import java.io.File;
import java.io.FileReader;
import java.io.PushbackReader;

import dk.brics.jscontrolflow.analysis.privatevars.PrivateVariables;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.ast2cfg.Ast2Cfg;
import dk.brics.jscontrolflow.ast2cfg.NullAstBinding;
import dk.brics.jscontrolflow.checks.CheckDefiniteAssignment;
import dk.brics.jscontrolflow.checks.CheckMaybeAssignment;
import dk.brics.jscontrolflow.checks.CheckWellformed;
import dk.brics.jscontrolflow.display.Function2Dot;
import dk.brics.jscontrolflow.display.ReachingDefinitions2Dot;
import dk.brics.jscontrolflow.transforms.SimplifyGraph;
import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.node.AFinallyClause;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;

public class TestUtil {
    public static Function testFile(File file) {
        try {
            Start root = new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file)))).parse();
            Function toplevel = Ast2Cfg.convert(root.getBody(), new NullAstBinding(), file);

            File dir = new File("test-output/"+file.getName()+"-out");
            dir.mkdirs();
            
            // print out if something goes wrong, otherwise wait until graph is simplified
            for (Function func : toplevel.getTransitiveInnerFunctions(true)) {
                String name = getFunctionName(toplevel, func);
                Function2Dot.printToFile(new File(dir, name + "-notsimple.dot"), func, Function2Dot.ExceptionalEdgeDisplay.COMPRESS);
            }

            try {
                CheckWellformed.check(toplevel);
                SimplifyGraph.simplifyAll(toplevel);
                if (root.getDescendants(AFinallyClause.class).size() > 0) {
                    CheckMaybeAssignment.check(toplevel);
                } else {
                    CheckDefiniteAssignment.check(toplevel);
                }
                CheckWellformed.check(toplevel);
            } finally {
                // print out if something goes wrong, otherwise wait until graph is simplified
                for (Function func : toplevel.getTransitiveInnerFunctions(true)) {
                    String name = getFunctionName(toplevel, func);
                    Function2Dot.printToFile(new File(dir, name + ".dot"), func, Function2Dot.ExceptionalEdgeDisplay.COMPRESS);
                }
            }

            // run some analyses to see if they crash (can't test correctness here easily)
            PrivateVariables vars = new PrivateVariables(toplevel);
            for (Function func : toplevel.getTransitiveInnerFunctions(true)) {
                ReachingDefinitions reachDef = new ReachingDefinitions(func, vars, func != toplevel);
                String name = getFunctionName(toplevel, func);
                ReachingDefinitions2Dot.printToFile(new File(dir, name + "-reach.dot"), func, reachDef);
            }

            return toplevel;
        } catch (Exception ex) {
            throw new RuntimeException("\r\n"+ex.getMessage(), ex);
        }
    }

    private static String getFunctionName(Function toplevel, Function func) {
        String name;
        if (func == toplevel) {
            name = "main";
        } else if (func.getName() != null) {
            name = func.getName();
        } else {
            name = "anon-" + func.getSourceLocation().getLineNumber();
        }
        return name;
    }
}
