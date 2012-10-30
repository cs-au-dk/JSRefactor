package dk.brics.jsrefactoring;

import java.util.LinkedList;
import java.util.List;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ABlockStmt;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AReturnStmt;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.AVarForInLvalue;
import dk.brics.jsparser.node.AVarForInit;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PAssignOp;
import dk.brics.jsparser.node.PBinop;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PObjectLiteralProperty;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.node.TComma;
import dk.brics.jsparser.node.TEndl;
import dk.brics.jsparser.node.TWhitespace;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.node.TokenEnum;

/**
 * Utility class that inserts whitespace and indentation for newly created AST nodes.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 */
public class PrettyPrinter {
	public static int DEFAULT_INDENTATION = 4;
	
	public static void pp(Node nd) {
		// try to guess a starting indentation
		int indent = 0;
		if(nd.parent() instanceof ABlock) {
			ABlock block = (ABlock)nd.parent();
			int idx = block.getStatements().indexOf(nd);
			if(idx > 0) {
				indent = getIndent(block.getStatements().get(idx-1));
			} else if(idx == 0 && block.getStatements().size() > 1) {
				indent = getIndent(block.getStatements().get(idx+1));
			}
		}
		pp(nd, indent, DEFAULT_INDENTATION);
	}
	
	// insert default whitespace and newline into the given subtree
	// does not take account of existing whitespace
	public static void pp(Node nd, int startIndent, final int increment) {
		final int[] indent = new int[1];
		indent[0] = startIndent;
		nd.apply(new DepthFirstAdapter() {
			@Override
			public void inABlockStmt(ABlockStmt stmt) {
				indent[0] += increment;
				AstUtil.insertTokenAfter(stmt.getLbrace(), new TEndl("\n")); 
				super.inABlockStmt(stmt);
			}
			
			@Override
			public void outABlockStmt(ABlockStmt stmt) {
				indent[0] -= increment;
				AstUtil.insertTokenBefore(new TWhitespace(repeat(indent[0], ' ')), stmt.getRbrace());
				AstUtil.insertTokenAfter(stmt.getRbrace(), new TEndl("\n"));
				super.outABlockStmt(stmt);
			}
			
			@Override
			public void inAObjectLiteralExp(AObjectLiteralExp lit) {
				indent[0] += increment;
				AstUtil.insertTokenAfter(lit.getLbrace(), new TEndl("\n"));
				super.inAObjectLiteralExp(lit);
			}
			
			@Override
			public void outAObjectLiteralExp(AObjectLiteralExp lit) {
				indent[0] -= increment;
				AstUtil.insertTokenBefore(new TWhitespace(repeat(indent[0], ' ')), lit.getRbrace());
				super.outAObjectLiteralExp(lit);
			}
			
			@Override
			public void defaultOutPAssignOp(PAssignOp op) {
				// insert spaces around assignment operator
				TokenPair tks = AstUtil.getFirstAndLastToken(op);
				AstUtil.insertTokenBefore(new TWhitespace(" "), tks.first);
				AstUtil.insertTokenAfter(tks.last, new TWhitespace(" "));
			}
			
			@Override
			public void defaultOutPBinop(PBinop op) {
				// insert spaces around binary operator
				TokenPair tks = AstUtil.getFirstAndLastToken(op);
				AstUtil.insertTokenBefore(new TWhitespace(" "), tks.first);
				AstUtil.insertTokenAfter(tks.last, new TWhitespace(" "));
			}
			
			@Override
			public void inABody(ABody node) {
				indent[0] += increment;
				super.inABody(node);
			}
			
			@Override
			public void outABody(ABody node) {
				indent[0] -= increment;
				super.outABody(node);
			}
			
			@Override
			public void outAFunctionExp(AFunctionExp node) {
				// insert space after keyword "function"
				if(node.getName() != null)
					AstUtil.insertTokenAfter(node.getFunction(), new TWhitespace(" "));
				// insert spaces before parameters (except the first one)
				for(int i=1;i<node.getParameters().size();++i)
					AstUtil.insertTokenBefore(new TWhitespace(" "), node.getParameters().get(i));
				// insert space after closing parenthesis of argument list
				AstUtil.insertTokenAfter(node.getRparen(), new TWhitespace(" "));
				// insert newline after opening brace
				AstUtil.insertTokenAfter(node.getLbrace(), new TEndl("\n"));
				// indent closing brace
				AstUtil.insertTokenBefore(new TWhitespace(repeat(indent[0], ' ')), node.getRbrace());
				super.outAFunctionExp(node);
			}
			
			@Override
			public void outAReturnStmt(AReturnStmt node) {
				// insert space after keyword "return"
				AstUtil.insertTokenAfter(node.getReturn(), new TWhitespace(" "));
				super.outAReturnStmt(node);
			}
			
			@Override
			public void outAVarDecl(AVarDecl node) {
				// insert space before name
				AstUtil.insertTokenBefore(new TWhitespace(" "), node.getName());
				// insert space around "=", if exists
				if(node.getEq() != null) {
					AstUtil.insertTokenBefore(new TWhitespace(" "), node.getEq());
					AstUtil.insertTokenAfter(node.getEq(), new TWhitespace(" "));
				}
				super.outAVarDecl(node);
			}
			
			@Override
			public void outANormalObjectLiteralProperty(ANormalObjectLiteralProperty node) {
				AstUtil.insertTokenAfter(node.getColon(), new TWhitespace(" "));
				super.outANormalObjectLiteralProperty(node);
			}
			
			@Override
			public void defaultInPStmt(PStmt node) {
				indent(node, indent[0]);
			}
			
			@Override
			public void defaultOutPStmt(PStmt node) {
				TokenPair tks = AstUtil.getFirstAndLastToken(node);
				AstUtil.insertTokenAfter(tks.last, new TEndl("\n"));
			}
			
			@Override
			public void defaultInPObjectLiteralProperty(PObjectLiteralProperty prop) {
				indent(prop, indent[0]);
			}
			
			@Override
			public void defaultOutPObjectLiteralProperty(PObjectLiteralProperty prop) {
				Token last = AstUtil.getFirstAndLastToken(prop).last;
				if(last.getNext() instanceof TComma)
					last = last.getNext();
				AstUtil.insertTokenAfter(last, new TEndl("\n"));
			}
			
			// TODO: many missing
		});
	}

	public static int getIndent(Node nd) {
		return getIndent(AstUtil.getFirstAndLastToken(nd).first);
	}
	
	public static int getIndent(Token tk) {
		int indent = 0;
		// skip back over ignorable tokens
		while(ignored(tk.getPrevious()))
			tk = tk.getPrevious();
		// now sum up lengths of all ignorable tokens
		while(ignored(tk)) {
			indent += tk.getText().length();
			tk = tk.getNext();
		}
		return indent;
	}
	
	/**
	 * Indents every line in the source text of <code>nd</code> by the given depth.
	 */
	public static void indent(Node nd, int depth) {
		TokenPair tokens = AstUtil.getFirstAndLastToken(nd);
		Token tk = tokens.first, last = tokens.last;
		outer:
		while(tk != null) {
			// no need to indent empty lines
			if(tk.kindToken() != TokenEnum.ENDL)
				AstUtil.insertTokenBefore(new TWhitespace(repeat(depth, ' ')), tk);
			
			while(tk != null && tk.kindToken() != TokenEnum.ENDL) {
				if(tk == last)
					break outer;
				tk = tk.getNext();
			}
			if(tk != null)
				tk = tk.getNext();
		}
	}
	
	private static String repeat(int times, char c) {
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<times;++i)
			buf.append(c);
		return buf.toString();
	}
	
	// replaces the body of a function expression or declaration while maintaining the token chain
	public static void setBody(IFunction fun, ABody body) {
		fun.setBody(body);
		
		TokenPair tokens = AstUtil.getFirstAndLastToken(body);
		
		// find token that should come before the body
		Token before = getFollowingRealToken(fun.getLbrace());
		if(before.kindToken() != TokenEnum.ENDL)
			before = before.getPrevious();
		
		// find token that should come after the body
		Token after = getPrecedingRealToken(fun.getRbrace());
		after = after.getNext();
		
		if(tokens.first == null) {
			// empty body
			connect(before, after);
		} else {
			connect(before, getInitialTokenOfStmt(tokens));
			connect(getFinalTokenOfStmt(tokens), after);
		}
	}

	// insert statement into block while maintaining token chain
	public static void insertStmtIntoBlock(ABlock block, int index, PStmt stmt) {
		LinkedList<PStmt> stmts = block.getStatements();
		TokenPair tokens = AstUtil.getFirstAndLastToken(stmt);
		
		Token ini = getInitialTokenOfStmt(tokens),
			  fin = getFinalTokenOfStmt(tokens);
		if(fin.kindToken() != TokenEnum.ENDL) {
			AstUtil.insertTokenAfter(fin, new TEndl("\n"));
			fin = fin.getNext();
		}
		
		if(stmts.size() == index) {
			Token last = AstUtil.getFirstAndLastToken(block).last;
			Token anchor = last == null ? AstUtil.getFirstAndLastToken(block.parent()).last : getFollowingRealToken(last);
			if(anchor.kindToken()==TokenEnum.ENDL)
				anchor = anchor.getNext();
			connect(anchor.getPrevious(), ini);
			connect(fin, anchor);
			if(index > 0) {
				int stmtIndent = getIndent(stmt),
					prevIndent = getIndent(stmts.get(index-1));
				if(stmtIndent < prevIndent)
					indent(stmt, prevIndent - stmtIndent);
			}
		} else {
			PStmt next = stmts.get(index);
			Token anchor = getPrecedingRealToken(AstUtil.getFirstAndLastToken(next).first);
			connect(fin, anchor.getNext());
			connect(anchor, ini);
			int stmtIndent = getIndent(stmt),
				nextIndent = getIndent(next);
			if(stmtIndent < nextIndent)
				indent(stmt, nextIndent - stmtIndent);
		}
		stmts.add(index, stmt);
		AstUtil.setRoot(stmt, block.getRoot());
	}

	// insert a statement as a top-level statement into a script
	public static void insertStmtIntoScript(Start script, int index, PStmt stmt) {
		ABody body = script.getBody();
		List<PStmt> stmts = body.getBlock().getStatements();
		stmts.add(index, stmt);
		TokenPair tokens = AstUtil.getFirstAndLastToken(stmt);
		Token stmtIni = getInitialTokenOfStmt(tokens),
			  stmtFin = getFinalTokenOfStmt(tokens);
		if(index == 0) {
			Token anchor = script.getEOF();
			while(anchor.getPrevious() != null)
				anchor = anchor.getPrevious();
			PrettyPrinter.connect(null, stmtIni);
			PrettyPrinter.connect(stmtFin, anchor);
			if(index > 0) {
				int stmtIndent = getIndent(stmt),
					prevIndent = getIndent(stmts.get(index-1));
				if(stmtIndent < prevIndent)
					indent(stmt, prevIndent - stmtIndent);
			}
		} else {
			PStmt prev = stmts.get(index-1);
			Token prevStmtLast = getFinalTokenOfStmt(AstUtil.getFirstAndLastToken(prev));
			Token prevStmtLastNext = prevStmtLast.getNext();
			PrettyPrinter.connect(prevStmtLast, stmtIni);
			PrettyPrinter.connect(stmtFin, prevStmtLastNext);
			int stmtIndent = getIndent(stmt),
				prevIndent = getIndent(prev);
			if(stmtIndent < prevIndent)
				indent(stmt, prevIndent - stmtIndent);
		}
	}
	
	// remove a statement from a block while maintaining token chain
	// returns the first and last token removed
	public static TokenPair removeStmtFromBlock(ABlock block, int index) {
		LinkedList<PStmt> stmts = block.getStatements();
		PStmt stmt = stmts.get(index);
		TokenPair tokens = AstUtil.getFirstAndLastToken(stmt);
		Token first = getInitialTokenOfStmt(tokens);
		Token last = getFinalTokenOfStmt(tokens);
		connect(first.getPrevious(), last.getNext());
		stmts.remove(index);
		return new TokenPair(first, last);
	}
	
	// return the farthest token preceding tokens.first that is preceded by a non-ignorable
	// token (i.e., either null or something other than a comment or whitespace)
	private static Token getInitialTokenOfStmt(TokenPair tokens) {
		Token initial = tokens.first;
		while(ignored(initial.getPrevious()))
			initial = initial.getPrevious();
		return initial;
	}
	
	public static Token getInitialToken(PStmt stmt) {
		return getInitialTokenOfStmt(AstUtil.getFirstAndLastToken(stmt));
	}
	
	// return either the following end-of-line token or analogous to getInitialTokenOfStmt
	private static Token getFinalTokenOfStmt(TokenPair tokens) {
		Token fin = tokens.last;
		while(ignored(fin.getNext()))
			fin = fin.getNext();
		if(fin.getNext() != null && fin.getNext().kindToken() == TokenEnum.ENDL)
			fin = fin.getNext();
		return fin;
	}
	
	public static Token getFinalToken(PStmt stmt) {
		return getFinalTokenOfStmt(AstUtil.getFirstAndLastToken(stmt));
	}
	
	// removes a variable declaration from its parent node while maintaining token chain
	// the parent has to be either a variable declaration statement or a for-loop init
	public static void removeVarDecl(AVarDecl decl) {
		Node parent = decl.parent();
		boolean isLast = false;
		if(parent instanceof AVarDeclStmt) {
			AVarDeclStmt stmt = (AVarDeclStmt)parent;
			if(stmt.getDecls().size() == 1) {
				Node grandparent = stmt.parent();
				if(grandparent instanceof ABlock) {
					int idx = ((ABlock)grandparent).getStatements().indexOf(parent);
					removeStmtFromBlock((ABlock)grandparent, idx);
				} else {
					AstUtil.replaceNode(stmt, NodeFactory.createEmptyStmt());
				}
				return;
			}
			int idx = stmt.getDecls().indexOf(decl);
			isLast = idx == stmt.getDecls().size() - 1;
			stmt.getDecls().remove(idx);
		} else if(parent instanceof AVarForInit) {
			AVarForInit init = (AVarForInit)parent;
			if(init.getVarDecl().size() == 1) {
				AstUtil.replaceNode(init, NodeFactory.createEmptyForInit());
				return;
			}
			int idx = init.getVarDecl().indexOf(decl);
			isLast = idx == init.getVarDecl().size() - 1;
			init.getVarDecl().remove(idx);
		} else if(parent instanceof AVarForInLvalue) {
			throw new IllegalArgumentException("Cannot remove variable declaration from for-in loop.");
		}
		TokenPair tokens = AstUtil.getFirstAndLastToken(decl);
		if(isLast) {
			Token preceding = getPrecedingRealToken(tokens.first.getPrevious());
			if(preceding.kindToken() == TokenEnum.COMMA)
				preceding = preceding.getPrevious();
			connect(preceding, tokens.last.getNext());
		} else {
			Token following = getFollowingRealToken(tokens.last.getNext());
			if(following.kindToken() == TokenEnum.COMMA)
				following = following.getNext();
			connect(tokens.first.getPrevious(), following);
		}
	}
	
	// insert expression into argument list while maintaining token chain
	public static void insertExpIntoArglist(Token start, LinkedList<PExp> args, Token end, int index, PExp exp) {
		TokenPair tokens = AstUtil.getFirstAndLastToken(exp);
		TComma comma = new TComma();
		TWhitespace ws = new TWhitespace(" ");
		connect(comma, ws);
		if(index == 0) {
			if(args.isEmpty()) {
				connect(start, tokens.first);
				connect(tokens.last, end);
			} else {
				connect(ws, start.getNext());
				connect(tokens.last, comma);
				connect(start, tokens.first);
			}
		} else if(index == args.size()) {
			connect(end.getPrevious(), comma);
			connect(ws, tokens.first);
			connect(tokens.last, end);
		} else {
			PExp prev = args.get(index-1);
			start = findFollowingToken(AstUtil.getFirstAndLastToken(prev).last, TComma.class);
			connect(ws, start.getNext());
			connect(tokens.last, comma);
			connect(start, tokens.first);
		}
		args.add(index, exp);
		AstUtil.setRoot(exp, start.getRoot());
	}
	
	public static Token getPrecedingRealToken(Token tk) {
		tk = tk.getPrevious();
		while(ignored(tk))
			tk = tk.getPrevious();
		return tk;
	}
	
	public static Token getFollowingRealToken(Token tk) {
		tk = tk.getNext();
		while(ignored(tk))
			tk = tk.getNext();
		return tk;
	}
	
	private static boolean ignored(Token tk) {
		if(tk == null)
			return false;
		TokenEnum kind = tk.kindToken();
		return kind == TokenEnum.WHITESPACE || kind == TokenEnum.SINGLELINECOMMENT || kind == TokenEnum.MULTILINECOMMENT;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findFollowingToken(Token tk, Class<T> type) {
		while(tk != null && !type.isInstance(tk))
			tk = tk.getNext();
		return tk == null ? null : (T)tk;
	}
	
	public static void connect(Token tk1, Token tk2) {
		if(tk1 != null)
			tk1.setNext(tk2);
		if(tk2 != null)
			tk2.setPrevious(tk1);
	}
}
