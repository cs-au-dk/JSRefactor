package dk.brics.jsparser.testing;

import java.io.File;

import org.junit.Test;

public class ParseSunspider {

    private void parseTest(String name) {
        TestUtil.parseFile(new File("../TAJS/test/sunspider/"+name));
    }

    @Test
    public void _3DCube() {
        parseTest("3d-cube.js");
    }
    @Test
    public void _3DMorph() {
        parseTest("3d-morph.js");
    }
    @Test
    public void _3DRaytrace() {
        parseTest("3d-raytrace.js");
    }
    @Test
    public void accessBinaryTrees() {
        parseTest("access-binary-trees.js");
    }
    @Test
    public void accessFannkuch() {
        parseTest("access-fannkuch.js");
    }
    @Test
    public void accessNBody() {
        parseTest("access-nbody.js");
    }
    @Test
    public void bitops3BitBitsInByte() {
        parseTest("bitops-3bit-bits-in-byte.js");
    }
    @Test
    public void bitopsBitsInByte() {
        parseTest("bitops-bits-in-byte.js");
    }
    @Test
    public void bitopsBitwiseAnd() {
        parseTest("bitops-bitwise-and.js");
    }
    @Test
    public void bitopsNsieveBits() {
        parseTest("bitops-nsieve-bits.js");
    }
    @Test
    public void controlFlowRecursive() {
        parseTest("controlflow-recursive.js");
    }
    @Test
    public void cryptoAes() {
        parseTest("crypto-aes.js");
    }
    @Test
    public void cryptoMd5() {
        parseTest("crypto-md5.js");
    }
    @Test
    public void cryptoSha1() {
        parseTest("crypto-sha1.js");
    }
}
