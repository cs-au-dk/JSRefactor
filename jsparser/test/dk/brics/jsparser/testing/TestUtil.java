package dk.brics.jsparser.testing;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PushbackReader;

import dk.brics.jsparser.ASTPrinter;
import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;

public class TestUtil {
    public static Start parseFile(File file) {
        try {
            Start root = new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file), 256))).parse();
            PrintStream stream = new PrintStream(new File("output/" + file.getName() + "-ast.txt"));
            try {
                root.apply(new ASTPrinter(stream));
            } finally {
                stream.close();
            }
            return root;
        } catch (Exception ex) {
            throw new RuntimeException("\r\n"+ex.getMessage(), ex);
        }
    }
    
    public static void main(String[] args) {
      parseFile(new File(args[0]));
    }
}
