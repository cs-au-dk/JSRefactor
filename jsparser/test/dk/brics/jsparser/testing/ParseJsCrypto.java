package dk.brics.jsparser.testing;

import java.io.File;

import org.junit.Test;

public class ParseJsCrypto {

    @Test
    public void jsCrypto() {
        TestUtil.parseFile(new File("../TAJS/test/jscrypto/jscrypto.js"));
    }
}
