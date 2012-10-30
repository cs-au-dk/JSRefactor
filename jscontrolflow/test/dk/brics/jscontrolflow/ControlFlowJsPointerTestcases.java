package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowJsPointerTestcases {

    private void run(String name) {
        TestUtil.testFile(new File("../jspointers/test/nano/" + name));
    }

    @Test
    public void test0() {
        run("test0.js");
    }

    @Test
    public void argumentsarray1() {
        run("argumentsarray1.js");
    }

    @Test
    public void withThisArg() {
        run("with-thisarg.js");
    }
}
