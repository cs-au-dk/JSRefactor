package dk.brics.jsrefactoring.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.node.TokenEnum;
import dk.brics.jsrefactoring.InputFile;
import dk.brics.jsrefactoring.Master;

public class TestUtil {
	public static Token followingCommentToken(NodeInterface nd) {
		Token last = AstUtil.getFirstAndLastToken(nd).last;
		Token token = last.getNext();
		while(isWSOrEndl(token))
			token = token.getNext();
		if(isComment(token))
			return token;
		return null;
	}
	
	public static String followingComment(NodeInterface nd) {
		Token token = followingCommentToken(nd);
		return token == null ? null : token.getText();
	}

	private static boolean isComment(Token token) {
		return token != null &&
			(token.kindToken()==TokenEnum.SINGLELINECOMMENT || token.kindToken()==TokenEnum.MULTILINECOMMENT);
	}

	private static boolean isWSOrEndl(Token token) {
		return token != null &&
			(token.kindToken()==TokenEnum.WHITESPACE || token.kindToken()==TokenEnum.ENDL);
	}

	public static Token precedingCommentToken(NodeInterface nd) {
		Token first = AstUtil.getFirstAndLastToken(nd).first;
		Token token = first.getPrevious();
		while(isWSOrEndl(token))
			token = token.getPrevious();
		if(isComment(token))
			return token;
		return null;
	}
	
	public static String precedingComment(NodeInterface nd) {
		Token token = precedingCommentToken(nd);
		return token == null ? null : token.getText();
	}

	public static String slurpFile(File f) {
		try {
			StringBuffer res = new StringBuffer();
			FileReader fr = new FileReader(f);
			for(int c=fr.read();c!=-1;c=fr.read())
				res.append((char)c);
			return res.toString().trim();
		} catch (FileNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		return null;
	}
	
	public static void writeFile(File f, String content) {
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(content);
			fw.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String pp(Master input) {
		StringBuffer buf = new StringBuffer();
		for(InputFile f : input.getUserFiles())
			buf.append(AstUtil.toSourceString(f.getAst()));
		return buf.toString();
	}

}
