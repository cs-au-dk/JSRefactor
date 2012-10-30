package dk.brics.jsparser;

import java.io.IOException;
import java.io.PushbackReader;

import dk.brics.jsparser.lexer.Lexer;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.EOF;
import dk.brics.jsparser.node.TEndl;
import dk.brics.jsparser.node.TRbrace;
import dk.brics.jsparser.node.TSemicolon;
import dk.brics.jsparser.node.TSlash;
import dk.brics.jsparser.node.Token;

public class SemicolonInsertingLexer extends Lexer {

    public SemicolonInsertingLexer(PushbackReader in) {
        super(in);
    }

    Token previousToken = null;
    Token previousNonIgnoredToken = null;
    Token bufferedToken = null;
    boolean wasEndl = false;

    private boolean isIgnored(Token token) {
        switch (token.kindToken()) {
        case WHITESPACE:
        case SINGLELINECOMMENT:
        case MULTILINECOMMENT:
        case ENDL:
            return true;
        default:
            return false;
        }
    }

    private boolean isEndlSensitive(Token token) {
        if (token == null) {
            return false;
        }
        switch (token.kindToken()) {
        case RETURN:
        case THROW:
        case BREAK:
        case CONTINUE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public Token peek(TokenPredicate predicate) throws LexerException, IOException {
        Token tok;
        if (this.token != null) {
            tok = this.token;
        } else {
            tok = readToken();
        }
        if (!predicate.acceptable(tok)) {
            if ((wasEndl || tok instanceof TRbrace || tok instanceof EOF) && predicate.acceptable(new TSemicolon())) {
                tok = insertSemicolon(tok);
            }
        }
        else if (tok instanceof TEndl && isEndlSensitive(previousNonIgnoredToken)) {
            tok = insertSemicolon(tok);
        }
        this.token = tok;
        return tok;
    }

    private Token insertSemicolon(Token tok) throws IOException {
        if (tok instanceof TSlash) {
            // the slash must be re-lexed as a regexp literal
            super.unread(tok);
            super.state = State.STMT;
            this.bufferedToken = null;
        } else {
            this.bufferedToken = tok;
        }
        tok = new TSemicolon(tok.getLine(), tok.getPos());
        tok.setAutomaticallyInserted(true);
        return tok;
    }

    @Override
    public Token next(TokenPredicate predicate) throws LexerException, IOException {
        Token tok;
        if (this.token != null) {
            tok = this.token;
        } else {
            tok = peek(predicate);
        }
        if (tok instanceof TEndl) {
            wasEndl = true;
        } else if (!isIgnored(tok)) {
            wasEndl = false;
        }
        if (previousToken != null) {
            tok.setPrevious(previousToken);
            previousToken.setNext(tok);
        }
        this.previousToken = tok;
        if (!isIgnored(tok)) {
            this.previousNonIgnoredToken = tok;
        }
        this.token = null;
        return tok;
    }

    private Token readToken() throws IOException, LexerException {
        Token tok;
        if (this.bufferedToken != null) {
            tok = this.bufferedToken;
            this.bufferedToken = null;
        } else {
            tok = getToken();
        }
        return tok;
    }

}
