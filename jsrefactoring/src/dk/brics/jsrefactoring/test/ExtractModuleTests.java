package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.extractmodule.ExtractModule;

public class ExtractModuleTests extends RefactoringTestSuite {
	private static final String BASE = "test" + File.separator + "ExtractModule" + File.separator;

	@Override
	protected String getBaseDir() {
		return BASE;
	}

	String startMarker1 = "/* extract module ", startMarker2 = " { */",
	   	   endMarker = "/* } */";
	@Override
	protected Refactoring getRefactoring(Master input) {
		String moduleName = null;
		int start = -1, end = -1;
		Start script = null;
		for(PStmt stmt : input.getAllNodesOfType(PStmt.class)) {
			String comm = TestUtil.precedingComment(stmt);
			if(comm != null && comm.startsWith(startMarker1) && comm.endsWith(startMarker2)) {
				if(!(stmt.parent() instanceof ABlock))
					continue;
				moduleName = comm.substring(startMarker1.length(), comm.length() - startMarker2.length());
				ABlock blk = (ABlock)stmt.parent();
				start = blk.getStatements().indexOf(stmt);
				Assert.assertTrue(script == null || blk.getRoot() == script);
				script = blk.getRoot();
			}
			comm = TestUtil.followingComment(stmt);
			if(comm != null && comm.equals(endMarker)) {
				if(!(stmt.parent() instanceof ABlock))
					continue;
				ABlock blk = (ABlock)stmt.parent();
				end = blk.getStatements().indexOf(stmt);
				Assert.assertTrue(script == null || blk.getRoot() == script);
			}
		}
		if(moduleName == null || start == -1 || end == -1)
			Assert.fail("Markers not found.");
		return new ExtractModule(input, moduleName, script, start, end); 
	}
	
	@Test
	public void simple1() {
		runTest("simple1");
	}
	
	@Test
	public void simple2() {
		runTest("simple2");
	}
	
	@Test
	public void simple3() {
		runTest("simple3");
	}
	
	@Test
	public void simple4() {
		runTest("simple4");
	}
	
	@Test
	public void simple5() {
		runTest("simple5");
	}
	
	@Test
	public void simple6() {
		runTest("simple6");
	}
	
	@Test
	public void simple7() {
		runTest("simple7");
	}
	
	@Test
	public void simple8() {
		runTest("simple8");
	}
	
	@Test
	public void simple9() {
		runTest("simple9");
	}
	
	@Test
	public void simple10() {
		runTest("simple10");
	}
	
	@Test
	public void simple11() {
		runTest("simple11");
	}
	
	@Test
	public void simple12() {
		runTest("simple12");
	}
	
	@Test
	public void simple13() {
		runTest("simple13");
	}
	
	@Test
	public void simple14() {
		runTest("simple14");
	}
	
	@Test
	public void invalidName() {
		runTest("invalidName");
	}
	
	@Test
	public void dyn() {
		runTest("dyn");
	}
	
	@Test
	public void ambiguous() {
		runTest("ambiguous");
	}
	
	@Test
	public void capture2() {
		runTest("capture2");
	}
	
	@Test
	public void in() {
		runTest("in");
	}
	
	@Test
	public void in2() {
		runTest("in2");
	}
	
	@Test
	public void in3() { 
		runTest("in3");
	}
	
	@Test
	public void forin() {
		runTest("forin");
	}
	
	@Test
	public void with() {
		runTest("with");
	}
	
	@Test
	public void this1() {
		runTest("this1");
	}
	
	@Test
	public void this2() {
		runTest("this2");
	}
	
	@Test
	public void impure() {
		runTest("impure");
	}
	
	@Test
	public void call1() {
		runTest("call1");
	}
	
	@Test
	public void call2() {
		runTest("call2");
	}
	
	@Test
	public void local1() {
		runTest("local1");
	}
	
	@Test
	public void managed1() {
		runTest("managed1");
	}
}
