package dk.brics.jscontrolflow.display;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.liveness.Liveness;
import dk.brics.jsutil.DotUtil;

public class Function2Dot {

    public enum ExceptionalEdgeDisplay {
        /**
         * No exceptional edges will be displayed.
         */
        HIDE,
        /**
         * Every block's exceptional edge will be displayed (if it has one).
         */
        SHOW,
        /**
         * Only blocks whose exception handler is different from one of its successors'
         * exception handler, and blocks without successors, will have their exceptional
         * edge displayed (if they have one).
         * <p/>
         * This is the recommended technique when not debugging control-flow specifically.
         */
        COMPRESS,
    }

    public static void printToFile(File file, Function function) {
        printToFile(file, function, ExceptionalEdgeDisplay.SHOW);
    }
    public static void printToFile(File file, Function function, ExceptionalEdgeDisplay exceptionalEdges) {
        PrintStream stream = null;
        try {
            stream = new PrintStream(file);
            print(stream, function, exceptionalEdges);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Prints a single function as a Graphviz dot graph.
     */
    public static void print(PrintStream out, Function function,  ExceptionalEdgeDisplay exceptionalEdges) {
        Liveness live = new Liveness(function);
        out.println("digraph {");
        out.println("  rankdir=\"TD\"");
        Map<Block,Integer> block2index = new HashMap<Block,Integer>();
        int nextindex=1;
        for (Block block : function.getBlocks()) {
            int index = nextindex++;
            block2index.put(block, index);
            out.printf("  BB_%d [shape=record, label=\"{", index); // start of block decl
            out.print(live.getLiveBefore(block).toString());
            for (Statement stm : block.getStatements()) {
                String stmString = Statement2Dot.toDot(stm);
                String labelString = DotUtil.escapeLabel(stmString);
//                if (stm != block.getFirst()) {
                out.print("|");
//                }
                out.print(labelString);
            }
            out.print("|");
            out.print(live.getLiveAfter(block).toString());
            out.println("}\"]"); // end of block decl
        }
        // print edges
        List<String> colors = new ArrayList<String>();
        colors.add("blue1");
        colors.add("green1");
        colors.add("red1");
        int colorindex=0;
        for (Block block : function.getBlocks()) {
            int index1 = block2index.get(block);
            for (Block succ : block.getSuccessors()) {
                int index2 = block2index.get(succ);
                String color = colors.get(++colorindex % colors.size());
                out.printf("  BB_%d -> BB_%d [tailport=s, headport=n, color=%s]\n", index1, index2, color);
            }
            if (block.getExceptionHandler() != null && exceptionalEdges != ExceptionalEdgeDisplay.HIDE) {
                int index2 = block2index.get(block.getExceptionHandler());
                boolean canCompress = exceptionalEdges == ExceptionalEdgeDisplay.COMPRESS && block.getSuccessors().size() > 0;
                for (Block succ : block.getSuccessors()) {
                    canCompress &= succ.getExceptionHandler() == block.getExceptionHandler();
                }
                if (!canCompress) {
                    out.printf("  BB_%d -> BB_%d [tailport=s, headport=n, color=gray]\n", index1, index2);
                }
            }
        }
        // print entry edge
        out.println("  INIT [shape=plaintext, label=\"\"]");
        out.printf("  INIT -> BB_%d [tailport=s, headport=n, label=\"Entry\"]\n", block2index.get(function.getEntry()));
        out.println("}");
    }
}
