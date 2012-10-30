package dk.brics.jsparser;

import java.io.IOException;
import java.io.PushbackReader;

import dk.brics.jsparser.lexer.Lexer;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.EOF;
import dk.brics.jsparser.node.TEndl;
import dk.brics.jsparser.node.TRbrace;
import dk.brics.jsparser.node.TRegexpLiteral;
import dk.brics.jsparser.node.TSemicolon;
import dk.brics.jsparser.node.TSlash;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.parser.TokenIndex;

public class SemicolonInsertingLexer extends Lexer {

	public SemicolonInsertingLexer(PushbackReader in) {
		super(in);
	}
	
	/*
	 * Warning: This code is very ugly and error prone.
	 */
	
	private boolean previousPreviousWasEndl = false;
	private boolean previousWasEndl = false;
	private boolean insertSemicolonAsNext = false;
	private Token lastToken;
	private Token bufferedToken;
	private boolean hasInsertedSemicolon = false;
	private Token errtoken;
	
	private Token nonEndlToken; // useful for debugging info
	
	/*
	 * If the offending token is a TSlash, then treat it as a regexp literal instead.
	 * Otherwise, try inserting a semicolon in front of it, unless we already inserted one here.
	 * 
	 * Note that the regexp literal may itself become an offending token and then have a semicolon
	 * inserted in front of it. Also note that no statement can start with a TSlash token, so there
	 * is no need to attempt to insert a semicolon before the slash.
	 */
	
	@Override
	public boolean errorOccurred() throws IOException {
		if (errtoken != null)
			return false;
		if (lastToken instanceof TSlash) {
			super.state = State.REGEXP;
			this.lastToken = null;
			super.token = null;
			this.previousWasEndl = this.previousPreviousWasEndl;
			return true;
		} else if ((previousWasEndl || lastToken instanceof TRbrace || lastToken instanceof EOF) && !hasInsertedSemicolon) {
			// symbol was not accepted, try inserting a semicolon
			bufferedToken = lastToken;
			insertSemicolonAsNext = true;
			super.token = null;
			return true;
		} else if (bufferedToken != null) {
			// a parser error will occur. make sure actual offending token
			// is used for error reporting instead of the inserted semicolon
			errtoken = bufferedToken;
			super.token = null;
			return true;
		} else {
			return false;
		}
	}
	TokenIndex converter = new TokenIndex();
	
	private boolean isIgnored(Token tok) {
		converter.index = -1;
		tok.apply(converter);
		return converter.index == -1;
	}
	
	@Override
	protected Token getToken() throws IOException, LexerException {
		if (errtoken != null) {
			return errtoken;
		}
		// FIXME: Insert semicolon after return,break,continue,throw
		previousPreviousWasEndl = previousWasEndl;
		if (insertSemicolonAsNext) {
			TSemicolon semi = new TSemicolon();
			semi.setAutomaticallyInserted(true);
			hasInsertedSemicolon = true;
			insertSemicolonAsNext = false;
			return semi;
		} else if (bufferedToken != null) {
			lastToken = bufferedToken;
			bufferedToken = null;
			// note: hasInsertedSemicolon should remain true
			return lastToken;
		} else {
			Token tok = super.getToken();
			if (tok instanceof TRegexpLiteral) {
				TRegexpLiteral regexp = (TRegexpLiteral) tok;
				regexp.setText("/" + regexp.getText()); // insert the missing slash
				regexp.setPos(regexp.getPos()-1);
			}
			if (!(tok instanceof TRegexpLiteral) && !isIgnored(tok)) {
				previousWasEndl = false;
				nonEndlToken = tok;
			}
			while (tok instanceof TEndl || isIgnored(tok)) {
				if (tok instanceof TEndl) {
					previousWasEndl = true;
				}
				tok = super.getToken();
			}
			lastToken = tok;
			hasInsertedSemicolon = false;
			super.state = State.NORMAL; // override sablecc state change
			return tok;
		}
	}
	
}
