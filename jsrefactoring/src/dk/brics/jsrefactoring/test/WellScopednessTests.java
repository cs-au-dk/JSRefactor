package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.encapsulateprty.ScopeConsistencyAnalysis;

public class WellScopednessTests {
	private static final String BASE = "test" + File.separator + "WellScopedness" + File.separator;
	
	private void runTest(String name) {
		Master input = new Master(new File(BASE, name));
		NodeFinder finder = new NodeFinder(input, IFunction.class, IInvocationNode.class, AAssignExp.class);
		for(IFunction fun : finder.getAllNodesOfType(IFunction.class)) {
			String comm = TestUtil.precedingComment(fun);
			if("/* well-scoped */".equals(comm)) {
				IFunction enc = fun.parent().getAncestor(IFunction.class);
				ScopeConsistencyAnalysis analysis = new ScopeConsistencyAnalysis(input, finder, input.getFunctions(enc));
				Assert.assertTrue("Analysis failed to prove function well-scoped.", analysis.isWellScoped(fun));
			} else if("/* not well-scoped */".equals(comm)) {
				IFunction enc = fun.parent().getAncestor(IFunction.class);
				ScopeConsistencyAnalysis analysis = new ScopeConsistencyAnalysis(input, finder, input.getFunctions(enc));
				Assert.assertFalse("Analysis unsoundly considers function well-scoped.", analysis.isWellScoped(fun));				
			}
		}
	}
	
	@Test
	public void simple() {
		runTest("simple.js");
	}
	
	@Test
	public void assign() {
		runTest("assign.js");
	}
	
	@Test
	public void indirectAssign() {
		runTest("indirectAssign.js");
	}
	
	@Test
	public void indirectAssign2() {
		runTest("indirectAssign2.js");
	}
	
	@Test
	public void with() {
		runTest("with.js");
	}
	
	@Test
	public void directInvoke() {
		runTest("directInvoke.js");
	}
	
	@Test
	public void protoInvoke() {
		runTest("protoInvoke.js");
	}
	
	@Test
	public void reflInvoke() {
		runTest("reflInvoke.js");
	}
	
	@Test
	public void reflAssign() {
		runTest("reflAssign.js");
	}
	
	@Test
	public void param1() {
		runTest("param1.js");
	}
	
	@Test
	public void dom() {
		runTest("dom.js");
	}
	
	@Test
	public void arg1() {
		runTest("arg1.js");
	}
}
