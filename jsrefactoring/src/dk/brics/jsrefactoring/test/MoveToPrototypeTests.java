package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.PAssignOp;
import dk.brics.jsrefactoring.InputFile;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.movetoprototype.MoveToPrototype;

/**
 * Test suite for {@link MoveToPrototype}.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class MoveToPrototypeTests extends RefactoringTestSuite {
	private static final String BASE = "test" + File.separator + "MoveToPrototype" + File.separator;
	private static final String MARKER = "/* move to prototype */";
	
	@Override
	protected String getBaseDir() {
		return BASE;
	}
	
	// The input to a test must contain a property expression followed by a comment
	// "/* move to prototype */"
	@Override
	protected Refactoring getRefactoring(Master input) {
        for (PAssignOp candidate : input.getAllNodesOfType(PAssignOp.class)) {
        	if(MARKER.equals(TestUtil.followingComment(candidate))) {
        		Assert.assertTrue(candidate.parent() instanceof AAssignExp);
        		return new MoveToPrototype(input, (AAssignExp)candidate.parent());
        	}
        }
        Assert.fail("marker not found");
        return null;
	}

	@Override
	protected void runTest(String name) {
		File inFile = new File(BASE + name + File.separator + "in" + File.separator + "test.js");
		File outFile = new File(BASE + name + File.separator + "out" + File.separator + "test.js");
		Assert.assertTrue(inFile.exists());
		Master input = new Master(inFile);
		Refactoring refactoring = getRefactoring(input);
		refactoring.execute();
		String out = AstUtil.toSourceString(input.getUserFiles().get(0).getAst()).trim();
		if(refactoring.getDiagnostics().isEmpty()) {
			if(!outFile.exists())
				Assert.assertEquals("<failure>", out);
			Assert.assertEquals(getExpectedOutput(new Master(outFile)), getExpectedOutput(input));
		} else {
			String msg = refactoring.getDiagnostics().get(0).getMessage();
			if(outFile.exists())
				Assert.assertEquals(TestUtil.slurpFile(outFile), msg);			
		}
	}

	// layout isn't quite right yet, so pretty-print instead
	public String getExpectedOutput(Master master) {
		StringBuffer res = new StringBuffer();
		for(InputFile f : master.getUserFiles())
			res.append(f.getAst().toString());
		return res.toString();
	}
	
	@Test
	public void simple() {
		runTest("simple");
	}
	
	@Test
	public void global1() {
		runTest("global1");
	}
	
	@Test
	public void global2() {
		runTest("global2");
	}
	
	@Test
	public void global3() {
		runTest("global3");
	}
	
	@Test
	public void create() {
		runTest("create");
	}
	
	@Test
	public void local() {
		runTest("local");
	}
	
	@Test
	public void thisref() {
		runTest("thisref");
	}
	
	@Test
	public void mutable() {
		runTest("mutable");
	}
	
	@Test
	public void multiProto() {
		runTest("multiProto");
	}
	
	@Test
	public void delete() {
		runTest("delete");
	}
	
	@Test
	public void cmp1() {
		runTest("cmp1");
	}
	
	@Test
	public void hasOwnProperty() {
		runTest("hasOwnProperty");
	}
	
	@Test
	public void closure1() {
		runTest("closure1");
	}
	
	@Test
	public void closure2() {
		runTest("closure2");
	}
	
	@Test
	public void closure3() {
		runTest("closure3");
	}
	
	@Test
	public void instanceprty1() {
		runTest("instanceprty1");
	}
	
	@Test
	public void instanceprty2() {
		runTest("instanceprty2");
	}
	
	@Test
	public void instanceprty3() {
		runTest("instanceprty3");
	}
	
	@Test
	public void instanceprty4() {
		runTest("instanceprty4");
	}
	
	@Test
	public void instanceprty5() {
		runTest("instanceprty5");
	}
	
	@Test
	public void instanceprty6() {
		runTest("instanceprty6");
	}
	
	@Test
	public void varref() {
		runTest("varref");
	}
	
	@Test
	public void multiAssign() {
		runTest("multiAssign");
	}
	
	@Test
	public void multiAssign2() {
		runTest("multiAssign2");
	}
	
	@Test
	public void proto1() {
		runTest("proto1");
	}
	
	@Test
	public void proto2() {
		runTest("proto2");
	}
	
	// the following two test cases are identical except for statement reordering; this shows the highly
	// flow-dependent nature of this refactoring, which makes it tough to implement using the current framework
	@Test
	public void shapes() {
		runTest("shapes");
	}
	
	@Test
	public void shapes2() {
		runTest("shapes2");
	}
}
