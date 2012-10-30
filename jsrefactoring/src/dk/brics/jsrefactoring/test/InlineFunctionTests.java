package dk.brics.jsrefactoring.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.inlining.InlineToOneShotClosure;

public class InlineFunctionTests extends RefactoringTestSuite {
	private static final String BASE = "test" + File.separator + "InlineFunction" + File.separator;
	private static final String marker = "/* inline */";

	@Override
	protected String getBaseDir() {
		return BASE;
	}

	@Override
	protected Refactoring getRefactoring(Master input) {
		for(AInvokeExp inv : input.getAllNodesOfType(AInvokeExp.class))
			if(marker.equals(TestUtil.followingComment(inv)))
				return new InlineToOneShotClosure(input, inv);
		Assert.fail("Marker not found.");
		return null;
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
	public void qual1() {
		runTest("qual1");
	}
	
	@Test
	public void qual2() {
		runTest("qual2");
	}
	
	@Test
	public void closurevars1() {
		runTest("closurevars1");
	}
	
	@Test
	public void closurevars2() {
		runTest("closurevars2");
	}
	@Test
	public void closurevars3() {
		runTest("closurevars3");
	}
	@Test
	public void closurevars4() {
		runTest("closurevars4");
	}
	@Test
	public void closurevars5() {
		runTest("closurevars5");
	}
	@Test
	public void closurevars6() {
		runTest("closurevars6");
	}
	@Test
	public void closurevars7() {
		runTest("closurevars7");
	}
	@Test
	public void defineProperty() {
		runTest("defineProperty");
	}
	@Test
	public void getOwnPropertyDescriptor() {
		runTest("getOwnPropertyDescriptor");
	}
	@Test
	public void convolutedCall1() {
		runTest("convolutedCall1");
	}
	@Test
	public void convolutedCall2() {
		runTest("convolutedCall2");
	}
	@Test
	public void superconstructor1() {
		runTest("superconstructor1");
	}
	@Test
	public void superconstructor2() {
		runTest("superconstructor2");
	}
	
	@Test
	public void with1() {
		runTest("with1");
	}
}
