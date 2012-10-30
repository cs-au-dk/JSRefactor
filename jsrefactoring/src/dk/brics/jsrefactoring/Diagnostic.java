package dk.brics.jsrefactoring;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.node.Node;

public class Diagnostic {
	public enum Severity { FATAL, ERROR, WARNING, NOTE };
	
	private Node location;
	private String message;
	private Severity severity;
	
	public Diagnostic(Node location, String message, Severity severity) {
		this.location = location;
		this.message = message;
		this.severity = severity;
	}
	
	public Diagnostic(String message, Severity severity) {
		this(null, message, severity);
	}
	
	public Node getLocation() {
		return location;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public int getStartLine() {
		if(location == null)
			return -1;
		TokenPair tks = AstUtil.getFirstAndLastToken(location);
		return tks.first.getLine();
	}
}
