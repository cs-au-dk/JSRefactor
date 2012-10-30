package dk.brics.jsparser.testing;

import java.io.File;

import org.junit.Test;

public class ParseGoogle {

    private void parseTest(String name) {
        TestUtil.parseFile(new File("../TAJS/test/google/"+name));
    }

    @Test
    public void deltaBlue() {
        parseTest("delta-blue.js");
    }
    @Test
    public void richards() {
        parseTest("richards.js");
    }
    @Test
    public void benchpress() {
        parseTest("benchpress.js");
    }
    @Test
    public void cryptobench() {
        parseTest("cryptobench.js");
    }
    @Test
    public void splay() {
        parseTest("splay.js");
    }

}
