package dk.brics.jspointers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.ast2cfg.Ast2Cfg;
import dk.brics.jscontrolflow.ast2cfg.AstBinding;
import dk.brics.jscontrolflow.display.Function2Dot;
import dk.brics.jscontrolflow.statements.InvokeStatement;
import dk.brics.jsparser.ASTPrinter;
import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.analysis.QuestionAdapter;
import dk.brics.jsparser.html.ExtractFromHtml;
import dk.brics.jsparser.html.JavaScriptSource;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AConditionalExp;
import dk.brics.jsparser.node.AForStmt;
import dk.brics.jsparser.node.AIfStmt;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.AWhileStmt;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;
import dk.brics.jsparser.parser.ParserException;
import dk.brics.jspointers.analysis.CallsiteSensitiveInvocation;
import dk.brics.jspointers.analysis.InvocationStrategy;
import dk.brics.jspointers.analysis.JSAnalysis;
import dk.brics.jspointers.analysis.ObjectSensitiveInvocation;
import dk.brics.jspointers.cfg2dataflow.Controlflow2DataflowBinding;
import dk.brics.jspointers.cfg2dataflow.DataflowCreator;
import dk.brics.jspointers.dataflow.AbstractFlowNodeVisitor;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.FunctionInstanceNode;
import dk.brics.jspointers.dataflow.IInvocationFlowNode;
import dk.brics.jspointers.dataflow.InputPoint;
import dk.brics.jspointers.dataflow.InvokeNode;
import dk.brics.jspointers.dataflow.LoadAndInvokeNode;
import dk.brics.jspointers.dataflow.OutputPoint;
import dk.brics.jspointers.display.DataflowToDot;
import dk.brics.jspointers.display.DisplayUtil;
import dk.brics.jspointers.display.TestDisplay;
import dk.brics.jspointers.harness.HarnessFiles;
import dk.brics.jspointers.lattice.contexts.Context;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NamedPropertyKey;
import dk.brics.jspointers.lattice.keys.OutputPointKey;
import dk.brics.jspointers.lattice.values.CoercedPrimitiveObjectValue;
import dk.brics.jspointers.lattice.values.NullValue;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jspointers.lattice.values.PrimitiveValue;
import dk.brics.jspointers.lattice.values.UndefinedValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jspointers.solver.Solver;
import dk.brics.jsutil.CollectionUtil;
import dk.brics.jsutil.MultiMap;

public class Main {

	private boolean printDebugFiles;
    private List<InputFile> harness;
    private AstBinding astBinding = new AstBinding();
	private List<InputFile> userFiles;
	private Set<Function> userCode;
	private Set<Function> harnessCode;
	private int loadCodeTime;
	private DataflowGraph dataflow;
	private Controlflow2DataflowBinding dataflowBinding= new Controlflow2DataflowBinding();
	private int createDataflowTime;
	private AnalysisResult<Key,Set<Value>> result;
	private int solveAnalysisTime;
	private MultiMap<Key, Value> contextInsensitiveMap = new MultiMap<Key, Value>();
	private Timer timer;
	private MultiMap<Function,UserFunctionValue> reachableInstances = new MultiMap<Function, UserFunctionValue>();
	private MultiMap<UserFunctionValue,Context> reachableContexts = new MultiMap<UserFunctionValue, Context>(); 
	private MultiMap<Function,Context> functionReachableContexts = new MultiMap<Function, Context>(); 
	private Map<Start,InputFile> ast2inputFile = new HashMap<Start, Main.InputFile>();
	private Set<String> knownPropertyNames = new HashSet<String>();
    private JSAnalysis analysis;
    private InvocationStrategy contextSensitivity;
	
	public List<InputFile> getHarness() {
		return harness;
	}
	public AstBinding getAstBinding() {
		return astBinding;
	}
	public DataflowGraph getDataflow() {
		return dataflow;
	}
	public Controlflow2DataflowBinding getDataflowBinding() {
		return dataflowBinding;
	}
	public List<InputFile> getAllInputs() {
		List<InputFile> files = new ArrayList<InputFile>();
		files.addAll(harness);
		files.addAll(userFiles);
		return files;
	}
	public List<InputFile> getUserFiles() {
		return userFiles;
	}
	public Set<Function> getUserCode() {
		return userCode;
	}
	public Set<Function> getHarnessCode() {
		return harnessCode;
	}
	public int getLoadCodeTime() {
		return loadCodeTime;
	}
	public int getCreateDataflowTime() {
		return createDataflowTime;
	}
	public MultiMap<Key, Value> getContextInsensitiveMap() {
		return contextInsensitiveMap;
	}
	public AnalysisResult<Key, Set<Value>> getResult() {
		return result;
	}
	public int getSolveAnalysisTime() {
		return solveAnalysisTime;
	}
	public MultiMap<UserFunctionValue, Context> getReachableContexts() {
		return reachableContexts;
	}
	public MultiMap<Function, UserFunctionValue> getReachableInstances() {
		return reachableInstances;
	}
	public MultiMap<Function, Context> getFunctionReachableContexts() {
		return functionReachableContexts;
	}
	public Set<String> getKnownPropertyNames() {
		return knownPropertyNames;
	}

    public class InputFile {
        private File file;
        private int lineNumber;
        private Start ast;
        private Function cfg;

        public InputFile(File file, int lineNumber, Start ast, Function cfg) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.ast = ast;
            this.cfg = cfg;
        }

        public File getFile() {
            return file;
        }
        public Start getAst() {
            return ast;
        }
        public Function getCfg() {
            return cfg;
        }
        public int getLineNumber() {
            return lineNumber;
        }
    }

    private InputFile makeInputFile(File file, int lineNumber, Start ast) {
		Function cfg = Ast2Cfg.convert(ast.getBody(), astBinding, file);
		InputFile inputFile = new InputFile(file, lineNumber, ast, cfg);
		ast2inputFile.put(inputFile.getAst(), inputFile);
		return inputFile;
	}

    public static Start parseURL(URL url) {
        try {
			return new Parser(new SemicolonInsertingLexer(new PushbackReader(new InputStreamReader(url.openStream()), 64))).parse();
		} catch (FileNotFoundException e) {
            throw new RuntimeException(e);
		} catch (ParserException e) {
            throw new RuntimeException(e);
		} catch (LexerException e) {
            throw new RuntimeException(e);
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
    }
    public static Start parseFile(File file) {
        try {
			return new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file), 64))).parse();
		} catch (FileNotFoundException e) {
            throw new RuntimeException(e);
		} catch (ParserException e) {
            throw new RuntimeException(e);
		} catch (LexerException e) {
            throw new RuntimeException(e);
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
    }
    public static Start parseString(String source) {
    	try {
			return new Parser(new SemicolonInsertingLexer(new PushbackReader(new StringReader(source), 64))).parse();
		} catch (ParserException e) {
            throw new RuntimeException(e);
		} catch (LexerException e) {
            throw new RuntimeException(e);
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
    }
    
    public <T> Set<T> lookupIns(Key key, Class<T> clazz) {
        if (!contextInsensitiveMap.keySet().contains(key))
            return CollectionUtil.filter(analysis.bottom(key), clazz);
    	return CollectionUtil.filter(contextInsensitiveMap.getView(key), clazz);
    }
    public <T> Set<T> lookupSens(Key key, Class<T> clazz) {
    	if (result.containsKey(key))
    		return CollectionUtil.filter(result.get(key), clazz);
    	else
    		return CollectionUtil.filter(analysis.bottom(key), clazz);
    }
    public <T> Set<T> lookupIns(InputPoint ip, Class<T> clazz) {
    	Set<T> result = new HashSet<T>();
    	for (OutputPoint op : ip.getSources()) {
    		result.addAll(lookupIns(op.getKey(NullContext.Instance), clazz));
    	}
    	return result;
    }
    public Set<Value> lookupIns(InputPoint ip) {
    	return lookupIns(ip, Value.class);
    }
    public Set<Value> lookupIns(OutputPoint op) {
		return lookupIns(op, Value.class);
    }
    public <T> Set<T> lookupIns(OutputPoint op, Class<T> clazz) {
		return lookupIns(op.getKey(NullContext.Instance), clazz);
    }
    
    public Set<ObjectValue> getDirectPrototypes(Collection<? extends ObjectValue> objects) {
    	Set<ObjectValue> result = new HashSet<ObjectValue>();
    	for (ObjectValue obj : objects) {
    		result.addAll(lookupSens(obj.getPrototypeProperty(), ObjectValue.class));
    	}
    	return result;
    }
    
    public Set<ObjectValue> getAllPrototypes(Collection<? extends ObjectValue> objects, boolean includeSelf) {
    	Set<ObjectValue> result = new HashSet<ObjectValue>();
    	LinkedList<ObjectValue> queue = new LinkedList<ObjectValue>(objects);
    	if (includeSelf) {
    		result.addAll(objects);
    	}
    	while (!queue.isEmpty()) {
    		ObjectValue obj = queue.removeFirst();
    		for (ObjectValue proto : lookupSens(obj.getPrototypeProperty(), ObjectValue.class)) {
    			if (result.add(proto)) {
    				queue.add(proto);
    			}
    		}
    	}
    	return result;
    }
    
	public Set<ObjectValue> objects(PExp exp) {
		Set<Value> values = getValuesOfExp(exp);
		Set<ObjectValue> result = new HashSet<ObjectValue>();
		for (Value val : values) {
		    if (val instanceof ObjectValue) {
		        result.add((ObjectValue)val);
		    } else {
		        PrimitiveValue prim = (PrimitiveValue) val;
		        if (!(prim instanceof UndefinedValue) && !(prim instanceof NullValue)) {
		            result.add(new CoercedPrimitiveObjectValue(prim));
		        }
		    }
		}
		return result;
	}
    
    public Set<Value> getValuesOfExp(PExp exp) {
        final Set<Value> values = new HashSet<Value>();
		exp.parent().apply(new QuestionAdapter<PExp>() {
		    @Override
		    public void caseAParenthesisExp(AParenthesisExp node, PExp exp) {
		        node.parent().apply(this, node);
		    }
            @Override
		    public void caseAInvokeExp(AInvokeExp node, PExp exp) {
		        if (node.getFunctionExp() == exp) {
		            for (InvokeStatement stm : astBinding.getInvokeStatements(node)) {
		                for (IInvocationFlowNode flow : dataflowBinding.getInvoke(stm)) {
		                    flow.apply(new AbstractFlowNodeVisitor() {
                                @Override
                                public void caseInvoke(InvokeNode node) {
                                    values.addAll(lookupIns(node.getFunc()));
                                }
                                @Override
                                public void caseLoadAndInvoke(LoadAndInvokeNode node) {
                                    values.addAll(lookupIns(node.getInvokedFunction()));
                                }
		                    });
		                }
		            }
		        } else {
		            defaultCase(exp);
		        }
		    }
		    public void caseAPrefixUnopExp(APrefixUnopExp node, PExp exp) {
		        if (node.getOp().kindPPrefixUnop() == EPrefixUnop.DELETE) {
		            throw new IllegalArgumentException("Cannot query the operand of a delete operation");
		        } else {
		            defaultCase(exp);
		        }
		    }
		    @Override
		    public void caseAIfStmt(AIfStmt node, PExp exp) {
		        if (node.getCondition() == exp) {
		            throw new IllegalArgumentException("Cannot query the condition in an if statement");
		        } else {
		            defaultCase(exp);
		        }
		    }
		    @Override
		    public void caseAWhileStmt(AWhileStmt node, PExp exp) {
		        if (node.getCondition() == exp) {
                    throw new IllegalArgumentException("Cannot query the condition in a while statement");
		        } else {
		            defaultCase(exp);
		        }
		    }
		    @Override
		    public void caseAForStmt(AForStmt node, PExp exp) {
		        if (node.getCondition() == exp) {
		            throw new IllegalArgumentException("Cannot query the condition in a for statement");
		        } else {
		            defaultCase(exp);
		        }
		    }
		    @Override
		    public void caseAConditionalExp(AConditionalExp node, PExp exp) {
		        if (node.getCondition() == exp) {
		            throw new IllegalArgumentException("Cannot query the condition in a conditional expression");
		        } else {
		            defaultCase(exp);
		        }
		    }
		    @Override
		    public void defaultNode(Node node, PExp exp) {
		        defaultCase(exp);
		    }
		    
		    void defaultCase(PExp exp) {
		        Function func = getFunctionOfBody(exp.getAncestor(ABody.class));
		        for (int var : astBinding.getExpResultVar(exp)) {
		            for (OutputPoint op : dataflowBinding.getVariableOutputPoints(func, var)) {
		                values.addAll(contextInsensitiveMap.getView(op.getKey(NullContext.Instance)));
		            }
		        }
		    }
		}, exp);
        return values;
    }
    
    public Function getFunctionOfBody(ABody body) {
        IFunction func = body.getAncestor(IFunction.class);
        if (func == null) {
            return ast2inputFile.get(body.getAncestor(Start.class)).getCfg();
        } else {
            return astBinding.getFunction(func);
        }
    }
    
    private List<InputFile> loadInputFiles(File ... files) throws IOException {
        List<InputFile> list = new ArrayList<InputFile>();
        for (File file : files) {
        	String name = file.getName().toLowerCase();
        	if (name.endsWith(".html") || name.endsWith(".htm")) {
        		List<JavaScriptSource> sources = ExtractFromHtml.extract(file);
        		for (JavaScriptSource src : sources) {
        			list.add(makeInputFile(file, src.getLineNr(), parseString(src.getSource()))); // TODO -1 to line number?
        		}
        	} else {
        		list.add(makeInputFile(file, 0, parseFile(file)));
        	}
        }
        return list;
    }

    private List<InputFile> loadHarnessFiles() throws IOException {
    	List<InputFile> list = new ArrayList<InputFile>();
    	for (URL url : HarnessFiles.getHarnessFiles()) {
    		list.add(makeInputFile(new File(url.getFile()), 0, parseURL(url)));
    	}
    	return list;
    }

    public Main(boolean printDebugFiles, File ... files) throws IOException {
        this.printDebugFiles = printDebugFiles;
        this.harness = loadHarnessFiles();
        this.contextSensitivity = new ObjectSensitiveInvocation(1);
        execute(files);
    }
    public Main(boolean printDebugFiles, InvocationStrategy contextSensitivity, File ... files) throws IOException {
        this.printDebugFiles = printDebugFiles;
        this.harness = loadHarnessFiles();
        this.contextSensitivity = contextSensitivity == null ? new ObjectSensitiveInvocation(1) : contextSensitivity;
        execute(files);
    }

    private static Set<Function> getTopLevelFunctions(Collection<InputFile> inputs) {
        Set<Function> result = new HashSet<Function>();
        for (InputFile input : inputs) {
            result.add(input.getCfg());
        }
        return result;
    }
    private static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.addAll(b);
        return result;
    }

    private void execute(File ... files) throws IOException {
        timer = new Timer();
        timer.start();

        userFiles = loadInputFiles(files);
        userCode = getTopLevelFunctions(userFiles);
        harnessCode = getTopLevelFunctions(harness);

        loadCodeTime = timer.checkpoint();

        if (printDebugFiles) {
            for (Function toplevel : userCode) {
                for (Function func : toplevel.getTransitiveInnerFunctions(true)) {
                    outputControlflowGraph(func);
                }
            }
        }

		dataflow = DataflowCreator.convert(userCode, harnessCode, dataflowBinding);

        createDataflowTime = timer.checkpoint();
        
		analysis = new JSAnalysis(dataflow, contextSensitivity);
        result = Solver.solve(analysis);

        solveAnalysisTime = timer.checkpoint();

        for (Map.Entry<Key,Set<Value>> en : result.entrySet()) {
        	contextInsensitiveMap.addAll(en.getKey().makeContextInsensitive(), en.getValue());
        	if (en.getKey() instanceof OutputPointKey) {
        		OutputPointKey ok = (OutputPointKey) en.getKey();
        		if (ok.getOutputPoint().getFlowNode() instanceof FunctionInstanceNode) {
        			FunctionInstanceNode fin = (FunctionInstanceNode) ok.getOutputPoint().getFlowNode();
        			for (UserFunctionValue uf : CollectionUtil.filter(en.getValue(), UserFunctionValue.class)) {
        				reachableContexts.add(uf, ok.getContext());
        				reachableInstances.add(fin.getFunction(), uf);
        				functionReachableContexts.add(uf.getFunction(), ok.getContext());
        			}
        		}
        	}
        	else if (en.getKey() instanceof NamedPropertyKey) {
        		NamedPropertyKey np = (NamedPropertyKey) en.getKey();
        		knownPropertyNames.add(np.getProperty());
        	}
        }

//        if (measurePerformance) {
//            printTimeMeasures();
//        }
//        
//        if (!measurePerformance) {
//            printInfo();
//        }
    }

    public void printInfo() {
		for (InputFile input : userFiles) {
		    // print AST
		    printAST(input.getFile(), input.getAst().getBody());

		    // print dataflow
		    for (Function func : input.getCfg().getTransitiveInnerFunctions(true)) {
		        outputDataflowGraph(func, dataflow);
		    }

		    // print pointer info and stuff
		    TestDisplay display = new TestDisplay(input.getAst().getBody(), input.getCfg(), astBinding, dataflow, new DisplayUtil(contextInsensitiveMap, dataflow));
		    display.print();
		}
	}

	public void printTimeMeasures() {
		System.out.println("Time spent:");
		System.out.printf(Locale.ROOT, "Parsing and CFG creation   %.1f seconds\n", loadCodeTime / 1000.0);
		System.out.printf(Locale.ROOT, "CFG->Dataflow              %.1f seconds\n", createDataflowTime / 1000.0);
		System.out.printf(Locale.ROOT, "Find fixpoint              %.1f seconds\n", solveAnalysisTime / 1000.0);
		System.out.printf(Locale.ROOT, "Total time:                %.1f seconds\n", timer.totalTime() / 1000.0);
	}

    private File getOutputFolder(File srcFile, String subfolder) {
        File file = new File("output/" + srcFile.getName() + "-out", subfolder);
        file.mkdirs();
        return file;
    }

    private void printAST(File srcFile, ABody ast) {
        String name = srcFile.getName() + ".txt";
        File astDir = getOutputFolder(srcFile, "ast");
        File astFile = new File(astDir, name);
        PrintStream print = null;
        try {
            print = new PrintStream(astFile);
            ast.apply(new ASTPrinter(print));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (print != null) {
                print.close();
            }
        }
    }

    private void outputControlflowGraph(Function function) {
        File srcFile = function.getSourceLocation().getFile();
        String name = functionName(function);

        File cfgDir = getOutputFolder(srcFile, "controlflow");
        File cfgFile = new File(cfgDir, name + ".dot");
        Function2Dot.printToFile(cfgFile, function);
    }

    private void outputDataflowGraph(Function function, DataflowGraph dataflow) {
        File srcFile = function.getSourceLocation().getFile();
        File dataflowDir = getOutputFolder(srcFile, "dataflow");
        String name = functionName(function);
        File dstFile = new File(dataflowDir, name + ".dot");
        DataflowToDot.print(dataflow.getFunctionFlownodes().getView(function), dstFile);
    }

    private String functionName(Function function) {
        String name;
        if (function.getOuterFunction() == null) {
            name = "toplevel";
        } else if (function.getName() != null) {
            name = function.getName();
        } else {
            name = "anon-" + function.getSourceLocation().getLineNumber();
        }
        return name;
    }

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

    private static class CmdLineOptions {
        boolean measurePerformance;
        String filename;
        InvocationStrategy contextSensitivity;
    }
    private static CmdLineOptions parseOptions(String[] args) {
        CmdLineOptions options = new CmdLineOptions();
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                options.filename = arg;
            }
            else if (arg.equals("-performance")) {
                options.measurePerformance = true;
            }
            else if (arg.startsWith("-ctx=")) {
            	String ctx = arg.substring("-ctx=".length());
            	Matcher m = Pattern.compile("callsite([0-9]*)").matcher(ctx);
            	if (m.matches()) {
            		options.contextSensitivity = new CallsiteSensitiveInvocation(Integer.valueOf(m.group(1)));
            	}
            	else {
	            	m = Pattern.compile("object([0-9]*)").matcher(ctx);
	            	if (m.matches()) {
	            		options.contextSensitivity = new ObjectSensitiveInvocation(Integer.valueOf(m.group(1)));
	            	}
	            	else {
	            		System.err.println("Unknown context sensitivity: " + ctx);
	            	}
            	}
            }
            else {
                System.err.println("Unrecognized argument: " + arg);
            }
        }
        return options;
    }

    public static void main(String[] args) throws IOException {
        CmdLineOptions options = parseOptions(args);
        if (options.filename == null) {
            options.filename = "test/nano/polymorphism.js";
            // options.filename = "../TAJS/test/google/delta-blue.js";
        }
        Main main = new Main(!options.measurePerformance, options.contextSensitivity, new File(options.filename));
        if (options.measurePerformance) {
        	main.printTimeMeasures();
        } else {
        	main.printInfo();
        }
    }
}
