package dk.brics.jsrefactoring.test;

import java.io.File;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;

/**
 * Some automatic and heuristic testing of {@link Master#getConstructedBy(dk.brics.jsparser.node.IFunction)}
 * @author asf
 *
 */
public class TestConstructedBy {
	private void testFile(String filename) {
		Master master = new Master(new File(filename));
		
		for (IFunction func : master.getAllNodesOfType(IFunction.class)) {
			if (master.isNativeCode((Node)func))
				continue;
			Set<ObjectValue> cons = master.getConstructedBy(func);
			Set<ObjectValue> thisVals = master.getReceivers(func);
			if (!thisVals.containsAll(cons)) {
				Assert.fail("Not all constructed objects are this values: " + func.getFunction().getLine());
			}
		}
	}

	@Test
	public void test0() {
		testFile("../jspointers/test/nano/test0.js");
	}
	@Test
	public void deltaBlue() {
		testFile("../jspointers/test/google/delta-blue.js");
	}
}
