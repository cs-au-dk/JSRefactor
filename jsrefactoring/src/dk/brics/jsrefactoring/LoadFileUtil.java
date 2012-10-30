package dk.brics.jsrefactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.ast2cfg.Ast2Cfg;
import dk.brics.jscontrolflow.ast2cfg.IAstBinding;
import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.SetRoot;
import dk.brics.jsparser.html.ExtractFromHtml;
import dk.brics.jsparser.html.JavaScriptSource;
import dk.brics.jsparser.html.JavaScriptSource.Kind;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;
import dk.brics.jsparser.parser.ParserException;
import dk.brics.jspointers.harness.HarnessFiles;

public class LoadFileUtil {

    public static InputFile loadJavaScriptFile(IAstBinding binding, File file) {
        try {
            Start ast = parseFile(file);
            return loadJavaScriptFile(binding, ast, file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        } catch (LexerException ex) {
            throw new RuntimeException(ex);
        }
    }
	public static InputFile loadJavaScriptFile(IAstBinding binding, Start ast, File file) {
		Function cfg = Ast2Cfg.convert(ast.getBody(), binding, file);
		return new InputFile(file, Kind.SCRIPT, 0, ast, cfg);
	}
    public static InputFile loadJavaScriptFileFromURL(IAstBinding binding, URL url) {
        try {
            Start ast = parseURL(url);
            Function cfg = Ast2Cfg.convert(ast.getBody(), binding, new File(url.getFile()));
            return new InputFile(new File(url.getFile()), Kind.SCRIPT, 0, ast, cfg);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        } catch (LexerException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static List<InputFile> loadHTMLFile(IAstBinding binding, File file) {
        try {
            List<JavaScriptSource> scripts = ExtractFromHtml.extract(file);
            List<InputFile> result = new ArrayList<InputFile>();
            for (JavaScriptSource script : scripts) {
                Start ast = parseString(script.getSource());
                Function cfg = Ast2Cfg.convert(ast.getBody(), binding, new File(script.getFile()));
                result.add(new InputFile(new File(script.getFile()), script.getKind(), script.getLineNr(), ast, cfg));
            }
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        } catch (LexerException ex) {
            throw new RuntimeException(ex);
        }
    }
    private static List<InputFile> loadFile(IAstBinding binding, File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return loadHTMLFile(binding, file);
        } else {
            return Collections.singletonList(loadJavaScriptFile(binding, file));
        }
    }

    public static Start parseFile(File file) throws ParserException, LexerException, IOException, FileNotFoundException {
        Start ast = new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file), 64))).parse();
        SetRoot.setRoot(ast);
        return ast;
    }
    public static Start parseURL(URL url) throws ParserException, LexerException, IOException, FileNotFoundException {
        Start ast = new Parser(new SemicolonInsertingLexer(new PushbackReader(new InputStreamReader(url.openStream()), 64))).parse();
        SetRoot.setRoot(ast);
        return ast;
    }
    public static Start parseString(String string) throws ParserException, LexerException, IOException, FileNotFoundException {
        Start ast = new Parser(new SemicolonInsertingLexer(new PushbackReader(new StringReader(string), 64))).parse();
        SetRoot.setRoot(ast);
        return ast;
    }

    public static List<InputFile> loadInputFiles(IAstBinding binding, File ... files) {
        List<InputFile> list = new ArrayList<InputFile>();
        for (File file : files) {
            list.addAll(loadFile(binding, file));
        }
        return list;
    }
    public static List<InputFile> loadInputFiles(IAstBinding binding, Start[] scripts, File[] files) {
        List<InputFile> list = new ArrayList<InputFile>();
        for(int i=0;i<scripts.length;++i)
        	list.add(loadJavaScriptFile(binding, scripts[i], files[i]));
        return list;
    }
    public static List<InputFile> loadHarnessFiles(IAstBinding binding) {
        List<InputFile> list = new ArrayList<InputFile>();
        for (URL url : HarnessFiles.getHarnessFiles()) {
            list.add(loadJavaScriptFileFromURL(binding, url));
        }
        return list;
    }

    
    public static Set<Function> getTopLevelFunctions(Collection<InputFile> inputs) {
        Set<Function> result = new HashSet<Function>();
        for (InputFile input : inputs) {
            result.add(input.getCfg());
        }
        return result;
    }
}
