package dk.brics.jsparser;

import dk.brics.jsparser.node.Token;

public class TokenPair {
    public Token first;
    public Token last;
    public TokenPair(Token first, Token last) {
        this.first = first;
        this.last = last;
    }
}
