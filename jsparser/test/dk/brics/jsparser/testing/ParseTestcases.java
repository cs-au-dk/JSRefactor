package dk.brics.jsparser.testing;

import java.io.File;

import org.junit.Test;

public class ParseTestcases {

    private void parseTest(String name) {
        TestUtil.parseFile(new File("testcases/"+name));
    }

    @Test
    public void regex1() {
        parseTest("regex1.js");
    }

    @Test
    public void syntax1() {
        parseTest("syntax1.js");
    }
    @Test
    public void syntax2() {
        parseTest("syntax2.js");
    }
    @Test
    public void syntax3() {
        parseTest("syntax3.js");
    }
    @Test
    public void syntax4() {
        parseTest("syntax4.js");
    }
    @Test
    public void syntax5() {
        parseTest("syntax5.js");
    }
    @Test
    public void syntax6() {
        parseTest("syntax6.js");
    }
    @Test
    public void semicolons1() {
        parseTest("semicolons1.js");
    }
    @Test
    public void semicolons2() {
        parseTest("semicolons2.js");
    }
    @Test
    public void semicolons3() {
        parseTest("semicolons3.js");
    }
    @Test
    public void semicolons4() {
        parseTest("semicolons4.js");
    }
    @Test
    public void semicolonOrRegexp() {
        parseTest("semicolon-or-regexp.js");
    }
    @Test
    public void returnFromToplevel() {
        parseTest("return-from-toplevel.js");
    }

}
