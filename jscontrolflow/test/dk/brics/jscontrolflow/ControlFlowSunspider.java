package dk.brics.jscontrolflow;

import java.io.File;

import org.junit.Test;

public class ControlFlowSunspider {

    private Function sunspider(String name) {
        return TestUtil.testFile(new File("../TAJS/test/sunspider/"+name));
    }

    @Test
    public void _3DCube() {
        sunspider("3d-cube.js");
    }
    @Test
    public void _3DMorph() {
        sunspider("3d-morph.js");
    }
    @Test
    public void _3DRaytrace() {
        sunspider("3d-raytrace.js");
    }
    @Test
    public void accessBinaryTrees() {
        sunspider("access-binary-trees.js");
    }
    @Test
    public void accessFannkuch() {
        sunspider("access-fannkuch.js");
    }
    @Test
    public void accessNBody() {
        sunspider("access-nbody.js");
    }
    @Test
    public void bitops3BitBitsInByte() {
        sunspider("bitops-3bit-bits-in-byte.js");
    }
    @Test
    public void bitopsBitsInByte() {
        sunspider("bitops-bits-in-byte.js");
    }
    @Test
    public void bitopsBitwiseAnd() {
        sunspider("bitops-bitwise-and.js");
    }
    @Test
    public void bitopsNsieveBits() {
        sunspider("bitops-nsieve-bits.js");
    }
    @Test
    public void controlFlowRecursive() {
        sunspider("controlflow-recursive.js");
    }
    @Test
    public void cryptoAes() {
        sunspider("crypto-aes.js");
    }
    @Test
    public void cryptoMd5() {
        sunspider("crypto-md5.js");
    }
    @Test
    public void cryptoSha1() {
        sunspider("crypto-sha1.js");
    }
}
