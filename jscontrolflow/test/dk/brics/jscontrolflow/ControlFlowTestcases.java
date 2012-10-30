package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowTestcases {

    private Function testcase(String name) {
        return TestUtil.testFile(new File("testcases/"+name));
    }

    @Test
    public void tryFinally1() {
        testcase("try-finally1.js");
    }
    @Test
    public void breakInCatch() {
    	testcase("break-in-catch.js");
    }
    @Test
    public void switch1() {
        testcase("switch1.js");
    }
    @Test
    public void chaintest() {
    	testcase("chaintest.js");
    }
    @Test
    public void funcdeclInFinally() {
        testcase("funcdecl-in-finally.js");
    }
    @Test
    public void urlsubstring() {
        testcase("urlsubstring.js");
    }
    @Test
    public void urlsubstring2() {
        testcase("urlsubstring2.js");
    }
}
