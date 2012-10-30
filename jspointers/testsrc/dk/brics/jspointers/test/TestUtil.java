package dk.brics.jspointers.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;

import dk.brics.jsparser.SemicolonInsertingLexer;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.parser.Parser;

public class TestUtil {
    public static Start parseFile(File file) {
        try {
            return new Parser(new SemicolonInsertingLexer(new PushbackReader(new FileReader(file), 256))).parse();
        } catch (Exception ex) {
            throw new RuntimeException("\r\n"+ex.getMessage(), ex);
        }
    }
    
    public static String readFile(File file) {
    	StringBuilder b = new StringBuilder();
    	try {
	    	BufferedReader reader = new BufferedReader(new FileReader(file));
	    	for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
	    		b.append(line).append("\r\n");
	    	}
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    	return b.toString();
    }
    public static String readReader(Reader rd) {
    	StringBuilder b = new StringBuilder();
    	try {
	    	BufferedReader reader = new BufferedReader(rd);
	    	for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
	    		b.append(line).append("\r\n");
	    	}
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    	return b.toString();
    }
    public static String readResource(String resource) {
    	StringBuilder b = new StringBuilder();
    	try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtil.class.getClassLoader().getResourceAsStream(resource)));
	    	for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
	    		b.append(line).append("\r\n");
	    	}
    	} catch (IOException ex) {
    		throw new RuntimeException(ex);
    	}
    	return b.toString();
    }
}
