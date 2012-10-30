package dk.brics.jsparser.testing;

import junit.framework.Assert;

import org.junit.Test;

import dk.brics.jsparser.Literals;

public class TestLiterals {
    @Test
    public void unicode0() {
        Assert.assertEquals("\n", Literals.parseEscapeSequences("\\u000A"));
    }
    @Test
    public void unicode1() {
        Assert.assertEquals("\u00A0", Literals.parseEscapeSequences("\\u00A0"));
    }
    @Test
    public void unicode2() {
        Assert.assertEquals("\u0c00", Literals.parseEscapeSequences("\\u0c00"));
    }
    @Test
    public void unicode3() {
        Assert.assertEquals("\uD000", Literals.parseEscapeSequences("\\uD000"));
    }
    @Test
    public void unicode4() {
        Assert.assertEquals("1\uD00A1", Literals.parseEscapeSequences("1\\uD00A1"));
    }
    @Test
    public void unicode5() {
        Assert.assertEquals(
                "The quick brown fox jumped over the lazy dog", 
                Literals.parseEscapeSequences("The \\u0071uick brown fox jumped over the lazy dog"));
    }
    @Test
    public void unicode6() {
        Assert.assertEquals(
                "a\\\u0010b", 
                Literals.parseEscapeSequences("a\\\\\\u0010b"));
    }
    @Test
    public void hex1() {
        Assert.assertEquals("\n", Literals.parseEscapeSequences("\\x0A"));
    }
    @Test
    public void hex2() {
        Assert.assertEquals("\u00AB", Literals.parseEscapeSequences("\\xAB"));
    }
    @Test
    public void hex3() {
        Assert.assertEquals("ab\u00CDab", Literals.parseEscapeSequences("ab\\xCDab"));
    }
    @Test
    public void linecontinuation1() {
        Assert.assertEquals("abcd", Literals.parseEscapeSequences("ab\\\ncd"));
    }
    @Test
    public void linecontinuation2() {
        Assert.assertEquals("abcd", Literals.parseEscapeSequences("ab\\\rcd"));
    }
    @Test
    public void linecontinuation3() {
        Assert.assertEquals("abcd", Literals.parseEscapeSequences("ab\\\r\ncd"));
    }
    @Test
    public void linecontinuation4() {
        Assert.assertEquals("abcd", Literals.parseEscapeSequences("abcd\\\r"));
    }
    @Test
    public void linecontinuation5() {
        Assert.assertEquals("ab\ncd", Literals.parseEscapeSequences("ab\\\r\n\\ncd"));
    }
    @Test
    public void linecontinuation6() {
        Assert.assertEquals("ab\ncd", Literals.parseEscapeSequences("ab\\\r\\ncd"));
    }
    @Test
    public void nullchar1() {
        Assert.assertEquals("\0", Literals.parseEscapeSequences("\\0"));
    }

    @Test
    public void id1() {
        Assert.assertEquals(Literals.parseIdentifier("foo\\u0041"), "foo\u0041");
    }
    @Test
    public void id2() {
        Assert.assertEquals(Literals.parseIdentifier("\\u0041foo"), "\u0041foo");
    }
    @Test
    public void id3() {
        Assert.assertEquals(Literals.parseIdentifier("foo\\u0041bar\\u0044baz"), "foo\u0041bar\u0044baz");
    }
}
