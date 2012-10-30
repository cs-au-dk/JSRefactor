package dk.brics.jsparser.html;

import java.io.File;

public class JavaScriptSource {
    private String file;
	private int lineNr;
	private int pos;
	private String source;
	private Kind kind;
	
	public enum Kind {
		SCRIPT,
		EVENT_ATTRIBUTE,
		LINK_HREF,
	}
	
	public JavaScriptSource() {
	}
	public JavaScriptSource(String file, int lineNr, int pos, String source, Kind kind) {
	    this.file = file;
		this.lineNr = lineNr;
		this.pos = pos;
		this.source = source;
		this.kind = kind;
	}
	
	public String getFile() {
        return file;
    }
	public void setFile(String file) {
        this.file = file;
    }
	public Kind getKind() {
		return kind;
	}
	public void setKind(Kind kind) {
		this.kind = kind;
	}
	public int getLineNr() {
		return lineNr;
	}
	public void setLineNr(int lineNr) {
		this.lineNr = lineNr;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	
}
