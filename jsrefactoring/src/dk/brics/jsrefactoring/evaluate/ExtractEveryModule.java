package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.util.LinkedList;

import junit.framework.AssertionFailedError;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.html.JavaScriptSource.Kind;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.TEndl;
import dk.brics.jsparser.node.TMultiLineComment;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.node.TokenEnum;
import dk.brics.jsrefactoring.InputFile;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.PrettyPrinter;
import dk.brics.jsrefactoring.test.TestUtil;

public class ExtractEveryModule {
	private void testExtract(File benchmark) {
		System.out.println(benchmark);
		Master input = new Master(benchmark);
		String orig = TestUtil.pp(input);
		stripModuleComments(input);
		annotateModules(input);
		input = ExtractModules.test(input);
		//Assert.assertEquals(orig, TestUtil.pp(input));
	}
	
	// strip out any comments of the form "/* module M */" and "/* end M */"
	private void stripModuleComments(Master input) {
		for(InputFile f : input.getUserFiles())
			stripModuleComments(f);
	}
	
	private void stripModuleComments(InputFile file) {
		Token first = AstUtil.getFirstAndLastToken(file.getAst()).first;
		while(first.getPrevious() != null)
			first = first.getPrevious();
		
		for(Token tk=first;tk!=null;tk=tk.getNext()) {
			if(tk.kindToken() == TokenEnum.MULTILINECOMMENT) {
				if(tk.getText().startsWith("/* module ") || tk.getText().startsWith("/* end ")) {
					PrettyPrinter.connect(tk.getPrevious(), tk.getNext());
				}
			}
			// make implicit semicolons explicit and put them into the right place
			if(tk.isAutomaticallyInserted()) {
				Token prev = tk.getPrevious();
				PrettyPrinter.connect(prev, tk.getNext());
				while(prev.kindToken() == TokenEnum.ENDL || prev.kindToken() == TokenEnum.WHITESPACE)
					prev = prev.getPrevious();
				AstUtil.insertTokenAfter(prev, tk);
				tk.setAutomaticallyInserted(false);
			}
		}
	}
	
	// insert comments "/* module Mi */" and "/* end Mi */" around every script
	private void annotateModules(Master input) {
		int i=0;
		for(InputFile f : input.getUserFiles()) {
			if(f.getKind() == Kind.SCRIPT) {
				String moduleName = "M" + i++;
				LinkedList<PStmt> stmts = f.getAst().getBody().getBlock().getStatements();
				PStmt fst = stmts.getFirst(), last = stmts.getLast();
				
				Token beginComment = new TMultiLineComment("/* module " + moduleName + " */");
				Token endl = new TEndl("\n");
				AstUtil.insertTokenBefore(endl, PrettyPrinter.getInitialToken(fst));
				AstUtil.insertTokenBefore(beginComment, endl);
				
				Token endComment = new TMultiLineComment("/* end " + moduleName + " */");
				Token endl2 = new TEndl("\n");
				Token finalToken = PrettyPrinter.getFinalToken(last);
				Assert.assertFalse("Automatically inserted semicolon.", finalToken.isAutomaticallyInserted());
				AstUtil.insertTokenAfter(finalToken, endComment);
				AstUtil.insertTokenAfter(endComment, endl2);
			}
		}
		System.out.println(i + " modules");
	}
	
	@Test public void test() {
		testExtract(Benchmarks.getBenchmark("chrome", "anotherworld.html"));
	}
	
	@Test public void testAll() {
		for(File benchmark : Benchmarks.getBenchmarks())
			try {
				testExtract(benchmark);
			} catch(AssertionFailedError e) {
				System.out.println(e.getMessage());
			}
	}
}
