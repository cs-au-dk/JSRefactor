package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;

public abstract class RefactoringTestSuite {
	protected abstract String getBaseDir();
	protected abstract Refactoring getRefactoring(Master input);
	
	/**
	 * Runs the test with the given name.
	 * 
	 * @param name
	 */
	protected void runTest(String name) {
		File inFile = new File(getBaseDir() + name + File.separator + "in" + File.separator + "test.js");
		File outFile = new File(getBaseDir() + name + File.separator + "out" + File.separator + "test.js");
		Assert.assertTrue(inFile.getAbsolutePath() + " should exist.", inFile.exists());
		Master input = new Master(inFile);
		Refactoring refactoring = getRefactoring(input);
		refactoring.execute();
		String out = AstUtil.toSourceString(input.getUserFiles().get(0).getAst()).trim();
		if(refactoring.getDiagnostics().isEmpty()) {
			if(!outFile.exists())
				Assert.assertEquals("<failure>", out);
			Assert.assertEquals(TestUtil.slurpFile(outFile), out);			
		} else {
			if(outFile.exists())
				Assert.assertEquals(TestUtil.slurpFile(outFile), refactoring.getDiagnostics().get(0).getMessage());			
		}
	}
}
