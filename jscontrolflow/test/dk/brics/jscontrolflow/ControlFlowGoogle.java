package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowGoogle {

    private Function google(String name) {
        return TestUtil.testFile(new File("../TAJS/test/google/"+name));
    }

    @Test
    public void deltaBlue() {
        google("delta-blue.js");
    }
    @Test
    public void richards() {
        google("richards.js");
    }
    @Test
    public void benchpress() {
        google("benchpress.js");
    }
    @Test
    public void cryptobench() {
        google("cryptobench.js");
    }
    @Test
    public void splay() {
        google("splay.js");
    }
}
