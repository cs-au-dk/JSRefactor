package dk.brics.jscontrolflow.display;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.analysis.reachdef.ArgumentsArrayVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ParameterVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.ReachingDefinitions;
import dk.brics.jscontrolflow.analysis.reachdef.SelfVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.StatementVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.UninitializedVariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinition;
import dk.brics.jscontrolflow.analysis.reachdef.VariableDefinitionQuestionAnswer;
import dk.brics.jsutil.DotUtil;

public class ReachingDefinitions2Dot {
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

    public static void printToFile(File file, Function function, ReachingDefinitions reachDef) {
        printToFile(file, function, reachDef, ExceptionalEdgeDisplay.SHOW);
    }
    public static void printToFile(File file, Function function, ReachingDefinitions reachDef, ExceptionalEdgeDisplay exceptionalEdges) {
        PrintStream stream = null;
        try {
            stream = new PrintStream(file);
            print(stream, function, reachDef, exceptionalEdges);
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
    public static void print(final PrintStream out, Function function, ReachingDefinitions reachDef, ExceptionalEdgeDisplay exceptionalEdges) {
        out.println("digraph {");
        out.println("  rankdir=\"TD\"");
        final Map<Block,Integer> block2index = new HashMap<Block,Integer>();
        int nextindex=1;
        // print blocks
        for (Block block : function.getBlocks()) {
            int index = nextindex++;
            block2index.put(block, index);
            out.printf("  BB_%d [shape=record, label=\"{", index); // start of block decl
            for (Statement stm : block.getStatements()) {
                String stmString = Statement2Dot.toDot(stm);
                String labelString = DotUtil.escapeLabel(stmString);
                if (stm != block.getFirst()) {
                    out.print("|");
                }
                out.printf("<S_%d>", stm.getSerial()); // give the record entry an ID
                out.print(labelString);
            }
            out.println("}\"]"); // end of block decl
        }
        // print parameter nodes
        for (int i=0; i<function.getParameterNames().size(); i++) {
            out.printf("PAR_%d [shape=box, label=\"Parameter %d: %s\"]\n", i, i, DotUtil.escapeLabel(function.getParameterNames().get(i)));
        }
        // print arguments node
        if (!function.hasExplicitArgumentsDeclaration()) {
            out.println("ARGUMENTS [shape=box, label=\"arguments array\"]");
        }
        // print control-flow edges
        List<String> controlColors = new ArrayList<String>();
        controlColors.add("black"); // make all control edges black
        int colorindex=0;
        for (Block block : function.getBlocks()) {
            int index1 = block2index.get(block);
            for (Block succ : block.getSuccessors()) {
                int index2 = block2index.get(succ);
                String color = controlColors.get(++colorindex % controlColors.size());
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
        // print reaching definitions
        colorindex = 0;
        List<String> reachColors = new ArrayList<String>();
        reachColors.add("blue1");
        reachColors.add("green1");
        reachColors.add("red1");
        for (Block block : function.getBlocks()) {
            final int dstBlock = block2index.get(block);
            for (final Statement stm : block.getStatements()) {
                for (int readVar : stm.getReadVariables()) {
                    Set<VariableDefinition> vardefs = reachDef.getReachingDefinitions(stm, readVar);
                    for (VariableDefinition vardef : vardefs) {
                        String srcId = vardef.apply(new VariableDefinitionQuestionAnswer<Void,String>() {
                            @Override
                            public String caseStatement(StatementVariableDefinition def, Void arg) {
                                return String.format("BB_%d:S_%d", block2index.get(def.getStatement().getBlock()), def.getStatement().getSerial());
                            }
                            @Override
                            public String caseParameter(ParameterVariableDefinition def, Void arg) {
                                return String.format("PAR_%d", def.getIndex());
                            }
                            @Override
                            public String caseArgumentsArray(ArgumentsArrayVariableDefinition def, Void arg) {
                                return "ARGUMENTS";
                            }
                            @Override
                            public String caseUninitialized(UninitializedVariableDefinition def, Void arg) {
                            	return "UNINITIALIZED";
                            }
                            @Override
                            public String caseSelf(SelfVariableDefinition def, Void arg) {
                            	return "SELF";
                            }
                        }, null);
                        String color = reachColors.get(++colorindex % reachColors.size());
                        out.printf("%s -> BB_%d:S_%d [color=%s]\n", srcId, dstBlock, stm.getSerial(), color);
                    }
                }
            }
        }
        // print entry edge
        out.println("  INIT [shape=plaintext, label=\"\"]");
        out.printf("  INIT -> BB_%d [tailport=s, headport=n, label=\"Entry\"]\n", block2index.get(function.getEntry()));
        out.println("}");
    }
}
