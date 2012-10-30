package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.encapsulateprty.InitialiserAnalysis;

public class InitialiserTests {
	private static final String BASE = "test" + File.separator + "Initialiser" + File.separator;
	
	private void runTest(String name) {
		Master input = new Master(new File(BASE, name));
		NodeFinder finder = new NodeFinder(input, IFunction.class, IInvocationNode.class, AAssignExp.class);
		for(IFunction fun : finder.getAllNodesOfType(IFunction.class)) {
			String comm = TestUtil.precedingComment(fun);
			if("/* initialiser */".equals(comm)) {
				InitialiserAnalysis analysis = new InitialiserAnalysis(input, finder);
				Assert.assertTrue("Analysis failed to prove function an initialiser.", analysis.isInitialiser(fun));
			} else if("/* not initialiser */".equals(comm)) {
				InitialiserAnalysis analysis = new InitialiserAnalysis(input, finder);
				Assert.assertFalse("Analysis unsoundly considers function an initialiser.", analysis.isInitialiser(fun));				
			}
		}
	}
	
	@Test
	public void constructor1() {
		runTest("constructor1.js");
	}
	
	@Test
	public void constructor2() {
		runTest("constructor2.js");
	}
	
	@Test
	public void constructor3() {
		runTest("constructor3.js");
	}
	
	@Test
	public void constructor4() {
		runTest("constructor4.js");
	}
	
	@Test
	public void constructor5() {
		runTest("constructor5.js");
	}
	
	@Test
	public void constructor6() {
		runTest("constructor6.js");
	}
	
	@Test
	public void constructor7() {
		runTest("constructor7.js");
	}
	
	@Test
	public void constructor8() {
		runTest("constructor8.js");
	}
	
	@Test
	public void constructor9() {
		runTest("constructor9.js");
	}
	
	@Test
	public void ambig1() {
		runTest("ambig1.js");
	}
	
	@Test
	public void ambig2() {
		runTest("ambig2.js");
	}
	
	@Test
	public void rec() {
		runTest("rec.js");
	}
	
	@Test
	public void bind() {
		runTest("bind.js");
	}
	
	@Test
	public void call() {
		runTest("call.js");
	}
	
	@Test
	public void call2() {
		runTest("call2.js");
	}
}
