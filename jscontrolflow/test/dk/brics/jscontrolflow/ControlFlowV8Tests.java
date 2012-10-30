package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowV8Tests {

    private Function test(String name) {
        return TestUtil.testFile(new File("../TAJS/test/v8tests/"+name));
    }

    @Test
    public void Try() {
        test("try.js");
    }
}
