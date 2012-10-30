package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowRBTree {

    @Test
    public void redblackTrees() {
        TestUtil.testFile(new File("../jspointers/test/asf/persistent-redblack-tree.js"));
    }
}
