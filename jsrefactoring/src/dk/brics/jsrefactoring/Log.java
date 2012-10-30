package dk.brics.jsrefactoring;

import java.util.ArrayList;
import java.util.List;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsrefactoring.Diagnostic.Severity;

/**
 * Logging class that collects diagnostics emitted by a refactoring.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class Log {
	public List<Diagnostic> diagnostics;

	public Log() {
		this.diagnostics = new ArrayList<Diagnostic>();
	}
	
	private void substitute(Object[] args) {
		for (int i=0; i<args.length; i++) {
			if (args[i] instanceof Node)
				args[i] = AstUtil.toSourceString((Node)args[i]);
		}
	}

	private void diagnostic(Severity severity, NodeInterface location, String format, Object ... args) {
		substitute(args);
		diagnostics.add(new Diagnostic((Node)location, String.format(format, args), severity));
	}
	private void diagnostic(Severity severity, String format, Object ... args) {
		substitute(args);
		diagnostics.add(new Diagnostic(String.format(format, args), severity));
	}
	
	public void fatal(NodeInterface location, String format, Object... args) {
		diagnostic(Severity.FATAL, location, format, args);
	}
	public void fatal(String format, Object... args) {
		diagnostic(Severity.FATAL, format, args);
	}

	public void error(NodeInterface location, String format, Object... args) {
		diagnostic(Severity.ERROR, location, format, args);
	}
	public void error(String format, Object... args) {
		diagnostic(Severity.ERROR, format, args);
	}

	public void warn(NodeInterface location, String format, Object... args) {
		diagnostic(Severity.WARNING, location, format, args);
	}
	public void warn(String format, Object... args) {
		diagnostic(Severity.WARNING, format, args);
	}
}