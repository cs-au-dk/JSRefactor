package dk.brics.jsrefactoring.inlining;

import java.io.File;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsrefactoring.CommandLineUtil;
import dk.brics.jsrefactoring.Diagnostic;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.changes.Change;

public class InlineMain {
	public static void main(String[] args) {
		Master input = new Master(new File(args[0]));
		
		AInvokeExp invoke = null;
		int lineNr = CommandLineUtil.promptInt("Enter line number of invocation");
		for (AInvokeExp exp : input.getAllNodesOfType(AInvokeExp.class)) {
			if (input.isNativeCode(exp))
				continue;
			if (exp.getLparen().getLine() != lineNr)
				continue;
			invoke = exp;
		}
		
		if (invoke == null) {
			System.err.println("No invoke at that line");
			return;
		}
		
		InlineToOneShotClosure inl = new InlineToOneShotClosure(input, invoke);
		
		for (Diagnostic diag : inl.getDiagnostics()) {
			System.err.println(diag.getMessage());
		}
		
		for (Change ch : inl.getChanges()) {
			ch.perform();
		}
		
		System.out.println(AstUtil.toSourceString(input.getUserFiles().get(0).getAst()));
	}
}
