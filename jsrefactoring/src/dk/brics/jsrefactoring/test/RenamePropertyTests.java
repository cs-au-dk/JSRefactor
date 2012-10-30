package dk.brics.jsrefactoring.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.Token;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.Refactoring;
import dk.brics.jsrefactoring.nodes.NameExpAccess;
import dk.brics.jsrefactoring.nodes.ParameterAccess;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsrefactoring.nodes.VarDeclAccess;
import dk.brics.jsrefactoring.renameprty.RenameProperty;

/**
 * Test suite for {@link RenameProperty}.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public class RenamePropertyTests extends RefactoringTestSuite {
	private static final String BASE = "test" + File.separator + "RenameProperty" + File.separator;
	
	@Override
	protected String getBaseDir() {
		return BASE;
	}
	
	private String getRenameComment(NodeInterface ni) {
        String comm = TestUtil.followingComment(ni);
        if (comm == null || !comm.startsWith("/* -> "))
            return null;
        return comm.substring("/* -> ".length(), comm.length() - " */".length());
	}
	
	/*
	 * <p>The input to a test must contain a property access followed by a comment of the form
	 * <tt>/* -> x *&#47;</tt> indicating the new name the property should be renamed to, in this case
	 * <tt>x</tt>.</p>
	 */
	protected Refactoring getRefactoring(Master input) {
        for (APropertyExp candidate : input.getAllNodesOfType(APropertyExp.class)) {
            String comm = getRenameComment(candidate);
            if(comm != null)
        		return new RenameProperty(input, new PropertyExpAccess(candidate), comm);
        }
        for (ANameExp candidate : input.getAllNodesOfType(ANameExp.class)) {
            String comm = getRenameComment(candidate);
            if (comm != null)
                return new RenameProperty(input, new NameExpAccess(candidate), comm);
        }
        for (AVarDecl candidate : input.getAllNodesOfType(AVarDecl.class)) {
            String comm = getRenameComment(candidate.getName());
            if (comm != null)
                return new RenameProperty(input, new VarDeclAccess(candidate), comm);
        }
        for (IFunction func : input.getAllNodesOfType(IFunction.class)) {
            int index = 0;
            for (Token tok : func.getParameters()) {
                String comm = getRenameComment(tok);
                if (comm != null)
                    return new RenameProperty(input, new ParameterAccess(func, index), comm);
                index++;
            }
        }
        Assert.fail("Marker not found.");	
        return null;
	}
	
	@Test 
	public void test1() {
		runTest("test1");
	}
	
	@Test 
	public void test2() {
		runTest("test2");
	}
	
	@Test 
	public void test3() {
		runTest("test3");
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
	public void nocapture() {
		runTest("nocapture");
	}
	
	@Test
	public void test4() {
		runTest("test4");
	}
	
	@Test 
	public void test5() {
		runTest("test5");
	}
	
	@Test 
	public void test6() {
		runTest("test6");
	}
	
	@Test 
	public void test7() {
		runTest("test7");
	}
	
	@Test 
	public void test8() {
		runTest("test8");
	}
	
	@Test 
	public void test9() {
		runTest("test9");
	}
	
	@Test 
	public void test10() {
		runTest("test10");
	}
	
	@Test 
	public void test11() {
		runTest("test11");
	}
	
	@Test 
	public void test12() {
		runTest("test12");
	}
	
	@Test 
	public void test13() {
		runTest("test13");
	}
	
	@Test 
	public void test14() {
		runTest("test14");
	}
	
	@Test 
	public void test15() {
		runTest("test15");
	}
	
	@Test 
	public void test16() {
		runTest("test16");
	}
	
	@Test
	public void forin1() {
		runTest("forin1");
	}
    @Test
    public void forin2() {
        runTest("forin2");
    }
    @Test
    public void forin3() {
        runTest("forin3");
    }
    @Test
    public void forin4() {
        runTest("forin4");
    }
    @Test
    public void forin5() {
        runTest("forin5");
    }
	
	@Test
	public void objectprototype1() {
		runTest("objectprototype1");
	}
	@Test
	public void objectprototype2() {
		runTest("objectprototype2");
	}
	
	@Test
	public void newNameInExpr() {
		runTest("newNameInExpr");
	}
	
	@Test
	public void clash() {
		runTest("clash");
	}
	
	@Test
	public void invalidName1() {
		runTest("invalidName1");
	}
	
	@Test
	public void invalidName2() {
		runTest("invalidName2");
	}
	
	@Test
	public void invalidName3() {
		runTest("invalidName3");
	}
	
	@Test
	public void dynamicAccess1() {
		runTest("dynamicAccess1");
	}
	
	@Test
	public void dynamicAccess2() {
		runTest("dynamicAccess2");
	}
	
	@Test
	public void dynamicAccess3() {
		runTest("dynamicAccess3");
	}
	
	@Test
	public void dynamicAccessVar() {
		runTest("dynamicAccessVar");
	}
	
	@Test
	public void numericIn1() {
		runTest("numericIn1");
	}
	
	@Test
	public void numericIn2() {
		runTest("numericIn2");
	}
	
	@Test
	public void hasOwnProperty() {
		runTest("hasOwnProperty");
	}
	
	@Test
	public void getOwnPropertyNames() {
		runTest("getOwnPropertyNames");
	}
	
	@Test
	public void getOwnPropertyDescriptor() {
		runTest("getOwnPropertyDescriptor");
	}
	
	@Test
	public void keys() {
		runTest("keys");
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
    public void with1() {
        runTest("with1");
    }
    @Test
    public void with2() {
        runTest("with2");
    }
    @Test
    public void local1() {
        runTest("local1");
    }
    @Test
    public void local2() {
        runTest("local2");
    }
    @Test
    public void local3() {
        runTest("local3");
    }
    @Test
    public void local4() {
        runTest("local4");
    }
    @Test
    public void local5() {
        runTest("local5");
    }
    @Test
    public void local6() {
        runTest("local6");
    }
    
    @Test
    public void paramclash1() {
        runTest("paramclash1");
    }
    @Test
    public void paramclash2() {
        runTest("paramclash2");
    }
    @Test
    public void unusedparam1() {
        runTest("unusedparam1");
    }
    
    @Test
    public void method() {
    	runTest("method");
    }
    
    @Test
    public void renameToArguments() {
        runTest("renameToArguments");
    }
    
    @Test
    public void catch1() {
    	runTest("catch1");
    }
    
    @Test
    public void catch2() {
    	runTest("catch2");
    }
    
    @Test
    public void rbtree1() {
    	runTest("rbtree1");
    }
    @Test
    public void rbtree2_noclash() {
    	runTest("rbtree2_noclash");
    }
    @Test
    public void rbtree3_compareTo() {
    	runTest("rbtree3_compareTo");
    }
    @Test
    public void rbtree4_clashglobal() {
    	runTest("rbtree4_clashglobal");
    }
    
    @Test
    public void JSON_stringify() {
    	runTest("JSON_stringify");
    }
    @Test
    public void JSON_parse() {
    	runTest("JSON_parse");
    }
    
    @Test
    public void eval() {
    	runTest("eval");
    }
    @Test
    public void renameGlobalNative1() {
    	runTest("renameGlobalNative1");
    }
    @Test
    public void renameGlobalNative2() {
    	runTest("renameGlobalNative2");
    }
    @Test
    public void fundecl1() {
    	runTest("fundecl1");
    }
}
