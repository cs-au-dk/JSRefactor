package dk.brics.jsparser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.lexer.Lexer;
import dk.brics.jsparser.lexer.Lexer.TokenPredicate;
import dk.brics.jsparser.lexer.LexerException;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ACommaExp;
import dk.brics.jsparser.node.AEmptyExp;
import dk.brics.jsparser.node.AEmptyForInit;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AForStmt;
import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.ALvalueForInLvalue;
import dk.brics.jsparser.node.ANumberPropertyName;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APostfixUnopExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AStringPropertyName;
import dk.brics.jsparser.node.EAssignOp;
import dk.brics.jsparser.node.EOF;
import dk.brics.jsparser.node.EPostfixUnop;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PForInit;
import dk.brics.jsparser.node.PPropertyName;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.node.TWhitespace;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.node.TokenEnum;

/**
 * Contains static methods for working with the AST.
 * 
 * @author asf
 */
public class AstUtil {
    /**
     * Returns whether the given expression is in a right hand side position.
     */
    public static boolean isRValue(final PExp exp) {
        if(exp.parent() == null)
        	return false;
        return exp.parent().apply(new AnswerAdapter<Boolean>() {
            @Override public Boolean caseAAssignExp(AAssignExp node) {
            	return !(node.getOp().kindPAssignOp() == EAssignOp.NORMAL &&
            			 node.getLeft() == exp);
            }
            @Override public Boolean caseAPrefixUnopExp(APrefixUnopExp node) {
            	return node.getOp().kindPPrefixUnop() != EPrefixUnop.DELETE;
            }
            @Override public Boolean caseAParenthesisExp(AParenthesisExp node) {
            	return isRValue(node);
            }
            @Override public Boolean caseALvalueForInLvalue(ALvalueForInLvalue node) {
            	return false;
            }
            @Override public Boolean defaultNode(Node node) {
                return true;
            }
        });
    }

    /**
     * Returns whether the given expression is in a left hand side position.
     */
    public static boolean isLValue(final PExp exp) {
        if(exp.parent() == null)
            return false;
        return exp.parent().apply(new AnswerAdapter<Boolean>() {
            @Override public Boolean caseAAssignExp(AAssignExp node) {
            	return node.getLeft() == exp;
            }
            @Override public Boolean caseAPrefixUnopExp(APrefixUnopExp node) {
                EPrefixUnop kind = node.getOp().kindPPrefixUnop();
                return kind == EPrefixUnop.INCREMENT || kind == EPrefixUnop.DECREMENT
                	|| kind == EPrefixUnop.DELETE;
            }
            @Override public Boolean caseAPostfixUnopExp(APostfixUnopExp node) {
                EPostfixUnop kind = node.getOp().kindPPostfixUnop();
                return kind == EPostfixUnop.INCREMENT || kind == EPostfixUnop.DECREMENT;
            }
            @Override public Boolean caseAParenthesisExp(AParenthesisExp node) {
            	return isLValue(node);
            }
            @Override public Boolean caseALvalueForInLvalue(ALvalueForInLvalue node) {
            	return true;
            }
            @Override public Boolean defaultNode(Node node) {
                return false;
            }
        });
    }

    /**
     * Converts a number to a string using JavaScript's conversion.
     * @param d a number
     * @return a string
     */
    public static String numberToString(double d) {
        return "" + d; // TODO compare with ECMA
    }

    /**
     * Returns the name of the property represented by the given property name.
     * @param name a property name node
     * @return a string
     */
    public static String getPropertyName(PPropertyName name) {
        return name.apply(new AnswerAdapter<String>() {
            @Override
            public String caseAIdentifierPropertyName(AIdentifierPropertyName node) {
                return Literals.parseIdentifier(node.getName().getText());
            }
            @Override
            public String caseANumberPropertyName(ANumberPropertyName node) {
                return numberToString(Literals.parseNumberLiteral(node.getNumberLiteral().getText()));
            }
            @Override
            public String caseAStringPropertyName(AStringPropertyName node) {
                return Literals.parseStringLiteral(node.getStringLiteral().getText());
            }
        });
    }

    /**
     * Replaces a node in the AST and updates the token chain accordingly.
     * @param oldNode node currently in the AST
     * @param newNode node to insert
     */
    public static void replaceNode(Node oldNode, Node newNode) {
    	TokenPair oldTokens = getFirstAndLastToken(oldNode);
    	TokenPair newTokens = getFirstAndLastToken(newNode);
    	if (oldTokens.first.getPrevious() != null) {
    		if (newTokens.first == null) {
    			oldTokens.first.getPrevious().setNext(oldTokens.last.getNext());
    		} else {
    			newTokens.first.setPrevious(oldTokens.first.getPrevious());
    			oldTokens.first.getPrevious().setNext(newTokens.first);
    		}
    	}
    	if (oldTokens.last.getNext() != null) {
    		if(newTokens.last == null) {
    			oldTokens.last.getNext().setPrevious(oldTokens.first.getPrevious());
    		} else {
    			newTokens.last.setNext(oldTokens.last.getNext());
    			oldTokens.last.getNext().setPrevious(newTokens.last);
    		}
    	}
    	oldNode.replaceBy(newNode);
    	setRoot(newNode, oldNode.getRoot());
    }
    
    /**
     * Sets the __root pointers in a subtree.
     * This has to be done, for instance, when inserting newly created nodes into the AST,
     * since their root would otherwise be null.
     */
    public static void setRoot(Node subtree, final Start root) {
    	subtree.apply(new DepthFirstAdapter() {
    		@Override
    		public void defaultIn(Node node) {
    			node.setRoot(root);
    			super.defaultIn(node);
    		}
    	});
    }
    
    /**
     * Inserts a given token before another token and updates the token chain accordingly.
     */
    public static void insertTokenBefore(Token newToken, Token oldToken) {
    	newToken.setNext(oldToken);
    	newToken.setPrevious(oldToken.getPrevious());
    	if(oldToken.getPrevious() != null)
    		oldToken.getPrevious().setNext(newToken);
    	oldToken.setPrevious(newToken);
    }

    /**
     * Inserts a given token after another token and updates the token chain accordingly.
     */
    public static void insertTokenAfter(Token oldToken, Token newToken) {
    	newToken.setPrevious(oldToken);
    	newToken.setNext(oldToken.getNext());
    	if(oldToken.getNext() != null)
    		oldToken.getNext().setPrevious(newToken);
    	oldToken.setNext(newToken);
    }

    /**
     * Returns the first and last token in the given node's subtree.
     * @param node an AST node
     * @return a token pair with first and last token.
     */
    public static TokenPair getFirstAndLastToken(NodeInterface node) {
        final TokenPair pair = new TokenPair(null, null);
        node.apply(new DepthFirstAdapter() {
            @Override
            public void defaultToken(Token token) {
                if (pair.first == null) {
                    pair.first = token;
                }
                pair.last = token;
            }
        });
        return pair;
    }
    

    /**
     * Returns the source representation of the given AST node.
     * Specifically, this is the concatenation of all tokens on the
     * token chain, starting with the first token in the node's subtree
     * and ending with the last token in the node's subtree.
     * @param node an AST node
     * @return a string
     */
    public static String toSourceString(Node node) {
    	if (node instanceof Start) {
    		// the root should include leading and trailing comments and whitespace
    		EOF eof = ((Start) node).getEOF();
    		Token t = eof;
    		while (t.getPrevious() != null) {
    			t = t.getPrevious();
    		}
    		return toSourceString(t, eof);
    	} else {
	        TokenPair tokens = getFirstAndLastToken(node);
	        return toSourceString(tokens.first, tokens.last);
    	}
    }
    
    public static String toSourceString(Token start, Token end) {
        StringBuilder b = new StringBuilder();
        Token tok = start;
        Collection<Token> seen = new HashSet<Token>(); 
        while (tok != end) {
        	if(seen.contains(tok))
        		throw new Error("circular token list");
            if (!tok.isAutomaticallyInserted()) {
                b.append(tok.getText());
            }
            seen.add(tok);
            if(tok.getNext() == null)
            	throw new Error("null token");
            tok = tok.getNext();
        }
        if (!tok.isAutomaticallyInserted()) {
            b.append(tok.getText());
        }
        return b.toString();
    }
    
    /**
     * Determines whether the given string is a valid JavaScript name.
     */
    public static boolean isName(String str) {
    	Lexer l = new Lexer(new PushbackReader(new StringReader(str)));
    	try {
			TokenPredicate yes = new TokenPredicate() {
				@Override
				public boolean acceptable(Token token) {
					return true;
				}
			};
			switch (l.next(yes).kindToken()) {
			case ID:
			case GET: // 'get' and 'set' are also valid identifiers
			case SET:
				break;
			default:
				return false;
			}
			return l.next(yes).kindToken() == TokenEnum._EOF_;
		} catch (LexerException e) {
		} catch (IOException e) {
		}
		return false;
    }

    /**
     * Determines whether node <code>anc</code> a (reflexive, transitive) ancestor
     * of node <code>desc</desc>.
     */
	public static boolean isAncestor(NodeInterface anc, NodeInterface desc) {
		while(desc != null && desc != anc)
			desc = desc.parent();
		return desc == anc;
	}
	
	/**
	 * Creates a deep clone of the given AST node and the token
	 * chain it spans. The returned clone has no parent, and its token chain
	 * is intact, but disconnected from the original node's token chain.
	 * @param <T> type of the node
	 * @param node an AST node
	 * @return a newly created AST node
	 */
	@SuppressWarnings("unchecked")
	public static <T extends NodeInterface> T clone(T node) {
		Map<Node,Node> map = new HashMap<Node,Node>();
		T cloneRoot = (T) node.clone(map);
		TokenPair tp1 = getFirstAndLastToken(node);
		if (tp1.first == null) {
		    return cloneRoot; // there are no tokens in this subtree
		}
		TokenPair tp2 = getFirstAndLastToken(cloneRoot);
		Token tok1 = tp1.first;
		Token tok2 = tp2.first;
		tok2.setPrevious(null);
		while (tok1 != tp1.last) {
			Token next1 = tok1.getNext();
			Token next2;
			if (map.containsKey(next1)) {
				next2 = (Token) map.get(next1);
			} else {
				next2 = (Token) next1.clone();
			}
			if (next1.isAutomaticallyInserted())
			    next2.setAutomaticallyInserted(true);
			tok2.setNext(next2);
			next2.setPrevious(tok2);
			tok1 = next1;
			tok2 = next2;
		}
		tok2.setNext(null);
		return cloneRoot;
	}
	
	
	/**
	 * Returns the parent node of the given AST node, skipping over any intervening
	 * parenthesis expressions.
	 */
	public static Node getRealParent(APropertyExp exp) {
		Node ret = exp.parent();
		while(ret instanceof AParenthesisExp)
			ret = ret.parent();
		return ret;
	}
	
	/**
	 * Determines whether the given expression occurs in a void context, i.e. in
	 * a position where its value is not used.
	 */
	public static boolean inVoidContext(PExp exp) {
		Node parent = exp.parent();
		assert parent != null : "Node is not in tree.";
		if(parent instanceof AParenthesisExp)
			return inVoidContext((AParenthesisExp)parent);
		if(parent instanceof ACommaExp)
			return ((ACommaExp)parent).getFirstExp() == exp
				|| inVoidContext((ACommaExp)parent);
		if(parent instanceof PForInit)
			return true;
		if(parent instanceof AForStmt)
			return ((AForStmt)parent).getUpdate() == exp;
		return parent instanceof AExpStmt;
	}
	
	/**
	 * Inserts empty {@link TWhitespace} tokens at certain places to ensure
	 * that every subtree in the AST contain at least one token (and is
	 * thus connected to the token chain).
	 * @param ast an AST node
	 */
	public static void fillTokens(Node ast) {
	    ast.apply(new DepthFirstAdapter() {
	        Token prevToken = null;
	        Token insert(Token tok) {
	            if (prevToken != null) {
	                if (prevToken.getNext() != null) {
	                    tok.setNext(prevToken.getNext());
	                    prevToken.getNext().setPrevious(tok);
	                }
	                prevToken.setNext(tok);
	                tok.setPrevious(prevToken);
	            }
	            prevToken = tok;
	            return tok;
	        }
	        @Override
	        public void defaultToken(Token token) {
	            prevToken = token;
	        }
	        @Override
	        public void inABlock(ABlock node) {
	            node.setFakeToken(insert(new TWhitespace("")));
	        }
	        @Override
	        public void inAEmptyForInit(AEmptyForInit node) {
	            node.setFakeToken(insert(new TWhitespace("")));
	        }
	    });
	}
}
