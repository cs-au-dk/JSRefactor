package dk.brics.jspointers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.ast2cfg.Ast2Cfg;
import dk.brics.jscontrolflow.ast2cfg.AstBinding;
import dk.brics.jscontrolflow.ast2cfg.IAstBinding;
import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;
import dk.brics.jsparser.parser.ParserException;
import dk.brics.jspointers.analysis.JSAnalysis;
import dk.brics.jspointers.analysis.ObjectSensitiveInvocation;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jspointers.solver.Solver;

public class Main {
	private static class Timer {
		private long startTime;
		private long lastCheckpoint;
		
		public void start() {
			this.startTime = System.currentTimeMillis();
			this.lastCheckpoint = startTime;
		}
		public int checkpoint() {
			long time = System.currentTimeMillis();
			int delta = (int)(time - this.lastCheckpoint);
			this.lastCheckpoint = time;
			return delta;
		}
		public int totalTime() {
			return (int)(System.currentTimeMillis() - startTime);
		}
	}
	private static String getFunctionName(Function func, boolean main) {
		if (main)
			return "<main>";
//		else if (func.getName() != null)
//			return func.getName();
		else
			return func.toString();
//			return removePathInfo(func.getSourceLocation().getFileName()) + "-" + func.getSourceLocation().getLineNumber();
	}
	private static ABody parse(File file) throws IOException {
        try {
            Start start = new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file)))).parse();
            return start.getBody();
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        } catch (LexerException ex) {
            throw new RuntimeException(ex);
        }
	}
	private static InputFile loadFile(File file) throws IOException {
	    ABody ast = parse(file);
	    AstBinding binding = new AstBinding();
	    Function func = Ast2Cfg.convert(ast, binding, file);
	    return new InputFile(file, ast, func, binding);
	}
	static class InputFile {
	    File file;
	    ABody ast;
	    Function cfg;
	    AstBinding binding;
        public InputFile(File file, ABody ast, Function cfg, AstBinding binding) {
            this.file = file;
            this.ast = ast;
            this.cfg = cfg;
            this.binding = binding;
        }
	}
	public static void main(String[] args) throws IOException {
		new File("output/flowgraphs").mkdir();
		new File("output/dataflows").mkdir();
		
		Timer timer = new Timer();
		timer.start();
		
		List<String> harnessfiles = Arrays.asList(
				"../jsdatalog/harness/Array.js",
				"../jsdatalog/harness/Boolean.js",
				"../jsdatalog/harness/Date.js",
				"../jsdatalog/harness/Math.js",
				"../jsdatalog/harness/Number.js",
				"../jsdatalog/harness/String.js",
				"../jsdatalog/harness/RegExp.js",
				"../jsdatalog/harness/Error.js",
				"../jsdatalog/harness/JSON.js");
//		List<String> harnessfiles = Collections.emptyList();
//		List<String> inputfiles = Arrays.asList("../jsdatalog/test/html/htmltest.html");
		List<String> inputfiles = Arrays.asList("../TAJS/test/google/delta-blue.js");
//		List<String> inputfiles = Arrays.asList("../jscontrolflow/testcases/switch1.js");
//		List<String> inputfiles = Arrays.asList("../TAJS/benchmark/chrome/anotherworld.html");
		
		Set<Function> harnessToplevels = new HashSet<Function>();
		for (String harnessFile : harnessfiles) {
			harnessToplevels.add(load(harnessFile));
		}
		Set<Function> usercode = new HashSet<Function>();
		for (String file : inputfiles) {
			usercode.add(load(file));
		}
		
		int flowgraphTime = timer.checkpoint();
		
		final DataflowGraph dataflow = DataflowCreator.convert(usercode, harnessToplevels);
		dataflow.getEntryFunctions().addAll(usercode);
		dataflow.getEntryFunctions().addAll(harnessToplevels);
		
		int dataflowTime = timer.checkpoint();
		
		// print .dot graph
//		for (Function function : flowgraph.getFlowGraph().getFunctions()) {
//			if (flowgraph.getHarnessFunctions().contains(function))
//				continue;
//			String name = getFunctionName(function, flowgraph.getFlowGraph().getMain() == function);
//			DataflowToDot.print(dataflow.getFunctionFlownodes().getView(function), new File("output/dataflows/" + name + ".dot"));
//		}

//		AnalysisResult<Key,Set<Value>> result = Solver.solve(
//				new JSAnalysis(dataflow, new CallsiteSensitiveInvocation(1)));
		AnalysisResult<Key,Set<Value>> result = Solver.solve(
				new JSAnalysis(dataflow, new ObjectSensitiveInvocation(20)));
		
		int analysisTime = timer.checkpoint();
		
		int totalTime = timer.totalTime();
		
		System.out.println("Done!");
		
		// annotate .js file
		System.out.println("Printing annotated JavaScript file");
		for (String file : inputfiles) {
			String name = removePathInfo(file);
//			AnnotateJs.annotateJavaScriptFile(file, "output/" + name, dataflow, result);
		}
		
		// print type info
//		PrintTypeInfo.printTypeInfo(dataflow, result);
		
		System.out.println("Time spent:");
		System.out.printf("Creating flow graph      %s\n", formatTime(flowgraphTime));
		System.out.printf("Creating dataflow graph  %s\n", formatTime(dataflowTime));
		System.out.printf("Analysis                 %s\n", formatTime(analysisTime));
		System.out.printf("Total                    %s\n", formatTime(totalTime));
	}
	
	private static String formatTime(int time) {
		return (time / 1000) + "." + (time % 1000) / 100 + " seconds";
	}
	public static String removePathInfo(String filename) {
        return new File(filename).getName();
    }

}
