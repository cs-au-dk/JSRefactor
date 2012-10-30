package dk.brics.jsparser;

import java.io.PrintStream;

import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.TSemicolon;
import dk.brics.jsparser.node.Token;

/**
 * Prints the AST, showing its tree structure.
 */
public class ASTPrinter extends DepthFirstAdapter {

    private int indent = 0;
    private PrintStream out;

    public ASTPrinter() {
        this.out = System.out;
    }
    public ASTPrinter(PrintStream out) {
        this.out = out;
    }

    private void printIndent() {
        for (int i=0; i<indent; i++) {
            out.print("| ");
        }
    }
    @Override
    public void defaultIn(Node node) {
        printIndent();
        out.print(node.getClass().getSimpleName());
        out.println();
        indent++;
    }
    @Override
    public void defaultOut(Node node) {
        indent--;
    }
    @Override
    public void defaultToken(Token token) {
        printIndent();
        out.print(token.getClass().getSimpleName());
        out.print(" [");
        out.print(token.getText());
        out.print("]");
        if (token instanceof TSemicolon) {
            TSemicolon semi = (TSemicolon)token;
            if (semi.isAutomaticallyInserted()) {
                out.print(" (auto)");
            }
        }
        out.println();
    }

}
