package dk.brics.jsrefactoring;

import java.io.File;

import dk.brics.jscontrolflow.Function;
import dk.brics.jsparser.html.JavaScriptSource.Kind;
import dk.brics.jsparser.node.Start;

public class InputFile {
    private final File file;
    private final int startLineNumber;
    private final Start ast;
    private final Function cfg;
    private final Kind kind;
    
    public InputFile(File file, Kind kind, int startLineNumber, Start ast, Function cfg) {
        this.file = file;
        this.kind = kind;
        this.startLineNumber = startLineNumber;
        this.ast = ast;
        this.cfg = cfg;
    }

    public File getFile() {
        return file;
    }
    
    public int getStartLineNumber() {
        return startLineNumber;
    }
    
    public Start getAst() {
        return ast;
    }
    
    public Function getCfg() {
        return cfg;
    }

    public Kind getKind() {
    	return kind;
    }
}