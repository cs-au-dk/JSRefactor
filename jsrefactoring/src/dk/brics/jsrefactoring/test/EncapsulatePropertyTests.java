package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.encapsulateprty.EncapsulateProperty;

/**
 * Test suite for {@link EncapsulateProperty}.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class EncapsulatePropertyTests extends RefactoringTestSuite {
	private static final String BASE = "test" + File.separator + "EncapsulateProperty" + File.separator;
	private static final String MARKER = "/* encapsulate */";
	
	@Override
	protected String getBaseDir() {
		return BASE;
	}

	// Finds property expression followed by comment /* encapsulate */, and constructs an EncapsulateProperty 
	// refactoring to encapsulate it.
	@Override
	protected Refactoring getRefactoring(Master input) {
        for (APropertyExp candidate : input.getAllNodesOfType(APropertyExp.class)) {
        	if(MARKER.equals(TestUtil.followingComment(candidate)))
        		return new EncapsulateProperty(input, candidate);
        }
        Assert.fail("marker not found");
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
	public void simple4() {
		runTest("simple4");
	}
	
	@Test
	public void unreachable() {
		runTest("unreachable");
	}
	
	@Test
	public void objlit() {
		runTest("objlit");
	}
	
	@Test
	public void objlit2() {
		runTest("objlit2");
	}
	
	@Test
	public void objlit3() {
		runTest("objlit3");
	}
	
	@Test
	public void delete() {
		runTest("delete");
	}
	
	@Test
	public void rw() {
		runTest("rw");
	}
	
	@Test
	public void rw2() {
		runTest("rw2");
	}
	
	@Test
	public void capture1() {
		runTest("capture1");
	}
	
	@Test
	public void capture2() {
		runTest("capture2");
	}
	
	@Test
	public void capture3() {
		runTest("capture3");
	}
	
	@Test
	public void capture4() {
		runTest("capture4");
	}
	
	@Test
	public void multiple() {
		runTest("multiple");
	}
	
	@Test
	public void function1() {
		runTest("function1");
	}
	
	@Test
	public void function2() {
		runTest("function2");
	}
	
	@Test
	public void differentReceiver() {
		runTest("differentReceiver");
	}
	
	@Test
	public void shapes() {
		runTest("shapes");
	}
	
	@Test
	public void shapes2() {
		runTest("shapes2");
	}
	@Test
	public void dyn1() {
		runTest("dyn1");
	}
	
	@Test
	public void dyn2() {
		runTest("dyn2");
	}
	
	@Test
	public void proto1() {
		runTest("proto1");
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
	public void call3() {
		runTest("call3");
	}
	
	@Test
	public void call4() {
		runTest("call4");
	}
	
	@Test
	public void noCover() {
		runTest("noCover");
	}
	
	@Test
	public void noCover2() {
		runTest("noCover2");
	}
	
	@Test
	public void toStringTest() {
		runTest("toString");
	}
	
	@Test
	public void valueOfTest() {
		runTest("valueOf");
	}
	
	@Test
	public void in() {
		runTest("in");
	}
	
	@Test
	public void proto2() {
	    runTest("proto2");
	}
    
    @Test
    public void param1() {
        runTest("param1");
    }
    
    @Test
    public void proto3() {
    	runTest("proto3");
    }
    
    @Test
    public void proto4() {
    	runTest("proto4");
    }
}
