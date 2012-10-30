package dk.brics.jsparser;

import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.node.Token;

public class SetRoot {
    public static void setRoot(final Start ast) {
        ast.apply(new DepthFirstAdapter() {
            @Override
            public void defaultIn(Node node) {
                node.setRoot(ast);
            }
            @Override
            public void defaultToken(Token token) {
            	token.setRoot(ast);
            }
        });
    }
}
