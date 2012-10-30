package dk.brics.jspointers.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.*;
import dk.brics.jsutil.Pair;

/**
 * Factory class for AST nodes. The factory methods create token nodes where needed
 * and stitch up the list of tokens in depth-first order.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 */
public class NodeFactory {
	public static AThisExp createThisExp() {
		return new AThisExp(new TThis());
	}
	
	public static AConstExp createStringLiteral(String s) {
		return new AConstExp(new AStringConst(new TStringLiteral(Literals.unparseStringLiteral(s, '"'))));
	}
	
	public static AConstExp createLiteral(int n) {
		return new AConstExp(new ANumberConst(new TNumberLiteral(String.valueOf(n))));
	}
	
	public static AConstExp createNullExp() {
		return new AConstExp(new ANullConst(new TNull()));
	}
	
	public static AParenthesisExp createParenExp(PExp exp) {
		return connectTokens(new AParenthesisExp(new TLparen(), exp, new TRparen()));
	}
	
	public static ANameExp createNameExp(String name) {
		return connectTokens(new ANameExp(new TId(name)));
	}
	
	public static APropertyExp createPropertyExp(PExp qual, String name) {
		return connectTokens(new APropertyExp(qual, new TDot(), new TId(name)));
	}
	
	public static AInvokeExp createInvokeExp(PExp fn, List<? extends PExp> args) {
		AInvokeExp exp = connectTokens(new AInvokeExp(fn, new TLparen(), args, new TRparen()));
		insertCommas(exp.getArguments());
		return exp;
	}
	
	public static AAssignExp createAssignExp(PExp left, PExp right) {
		return connectTokens(new AAssignExp(left, new ANormalAssignOp(new TEq()), right));
	}
	
	public static AAssignExp createAssignExp(PExp left, PAssignOp op, PExp right) {
		return connectTokens(new AAssignExp(left, op, right));
	}
	
	public static AArrayLiteralExp createArrayLiteralExp(List<PExp> elems) {
		AArrayLiteralExp exp = connectTokens(new AArrayLiteralExp(new TLbrack(), elems, new TRbrack()));
		insertCommas(exp.getValues());
		return exp;
	}

	private static void insertCommas(List<? extends NodeInterface> elems) {
		boolean first=true;
		for (NodeInterface exp : elems) {
			if (first) {
				first = false;
			} else {
				TokenPair tp = AstUtil.getFirstAndLastToken(exp);
				AstUtil.insertTokenBefore(new TComma(), tp.first);
			}
		}
	}
	
	private static PBinop createPBinOp(EBinop op) {
		switch(op) {
		case SHIFT_LEFT:
			return new AShiftLeftBinop(new TLtLt());
		case INSTANCEOF:
			return new AInstanceofBinop(new TInstanceof());
		case LOGICAL_OR:
			return new ALogicalOrBinop(new TOrOr());
		case LOGICAL_AND:
			return new ALogicalAndBinop(new TAndAnd());
		case NOT_EQUAL_STRICT:
			return new ANotEqualStrictBinop(new TNotEqEq());
		case LESS:
			return new ALessBinop(new TLt());
		case GREATER:
			return new AGreaterBinop(new TGt());
		case SHIFT_RIGHT:
			return new AShiftRightBinop(new TGtGt());
		case BITWISE_XOR:
			return new ABitwiseXorBinop(new TXor());
		case TIMES:
			return new ATimesBinop(new TStar());
		case IN:
			return new AInBinop(new TIn());
		case EQUAL:
			return new AEqualBinop(new TEqEq());
		case BITWISE_OR:
			return new ABitwiseOrBinop(new TOr());
		case NOT_EQUAL:
			return new ANotEqualBinop(new TNotEq());
		case EQUAL_STRICT:
			return new AEqualStrictBinop(new TEqEqEq());
		case DIVIDE:
			return new ADivideBinop(new TSlash());
		case GREATER_EQUAL:
			return new AGreaterEqualBinop(new TGtEq());
		case SHIFT_RIGHT_UNSIGNED:
			return new AShiftRightUnsignedBinop(new TGtGtGt());
		case LESS_EQUAL:
			return new ALessEqualBinop(new TLtEq());
		case PLUS:
			return new APlusBinop(new TPlus());
		case MODULO:
			return new AModuloBinop(new TModulo());
		case MINUS:
			return new AMinusBinop(new TMinus());
		case BITWISE_AND:
			return new ABitwiseAndBinop(new TAnd());
		}
		return null;
	}

	public static ABinopExp createBinOp(EBinop op, PExp left, PExp right) {
		return connectTokens(new ABinopExp(left, createPBinOp(op), right));
	}
	
	public static AObjectLiteralExp createObjectLiteral(List<Pair<String, PExp>> inits) {
		List<PObjectLiteralProperty> properties = new LinkedList<PObjectLiteralProperty>();
		for(Pair<String, PExp> init : inits)
			properties.add(connectTokens(new ANormalObjectLiteralProperty(new AIdentifierPropertyName(new TId(init.fst)), new TColon(), init.snd)));
		// need to insert commas between the initializers
		AObjectLiteralExp res = connectTokens(new AObjectLiteralExp(new TLbrace(), properties , null, new TRbrace()));
		insertCommas(res.getProperties());
//		for(int i=0;i<inits.size()-1;++i) {
//			PObjectLiteralProperty prop = res.getProperties().get(i);
//			Token last = AstUtil.getFirstAndLastToken(prop).last;
//			AstUtil.insertTokenAfter(last, new TComma());
//		}
		return res;
	}
	
	public static AExpStmt createExpStmt(PExp exp) {
		return connectTokens(new AExpStmt(exp, new TSemicolon()));
	}
	
	public static AReturnStmt createReturnStmt(PExp exp) {
		return connectTokens(new AReturnStmt(new TReturn(), exp, new TSemicolon()));
	}
	
	public static ABlock createBlock(PStmt... stmts) {
		return connectTokens(new ABlock(new TWhitespace(""), Arrays.asList(stmts)));
	}
	
	public static AFunctionExp createFunctionExp(List<String> parmnames, ABlock body) {
		return createFunctionExp(null, parmnames, body);
	}
	
	public static AFunctionExp createFunctionExp(String name, List<String> parmnames, ABlock body) {
		List<Token> parms = new ArrayList<Token>();
		for(int i=0;i<parmnames.size();++i) {
			TId tk = new TId(parmnames.get(i));
			parms.add(tk);
			if(i < parmnames.size() - 1)
				AstUtil.insertTokenAfter(tk, new TComma());
		}
		return connectTokens(new AFunctionExp(new TFunction(), name == null ? null : new TId(name), new TLparen(), parms, new TRparen(), 
											  new TLbrace(), new ABody(body), new TRbrace()));
	}
	
	public static AVarDeclStmt createVarDeclStmt(String... names) {
		List<Pair<String, PExp>> inits = new LinkedList<Pair<String,PExp>>();
		for(String name : names)
			inits.add(Pair.<String, PExp>make(name, null));
		return createVarDeclStmt(inits);
	}
	public static AVarDeclStmt createVarDeclStmt(String name, PExp init) {
		return createVarDeclStmt(Collections.singletonList(Pair.make(name, init)));
	}
	public static AVarDeclStmt createVarDeclStmt(List<Pair<String, PExp>> inits) {
		List<AVarDecl> decls = new LinkedList<AVarDecl>();
		for(Pair<String, PExp> init : inits) {
			if(init.snd == null)
				decls.add(new AVarDecl(new TId(init.fst), null, null));
			else
				decls.add(new AVarDecl(new TId(init.fst), new TEq(), init.snd));
		}
		AVarDeclStmt res = connectTokens(new AVarDeclStmt(new TVar(), decls, new TSemicolon()));
		// need to insert commas between the var decls
		for(int i=0;i<inits.size()-1;++i) {
			AVarDecl decl = res.getDecls().get(i);
			Token last = AstUtil.getFirstAndLastToken(decl).last;
			AstUtil.insertTokenAfter(last, new TComma());
		}
		return res;
	}
    
	public static AEmptyStmt createEmptyStmt() {
		return connectTokens(new AEmptyStmt(new TSemicolon()));
	}
	
	public static AEmptyForInit createEmptyForInit() {
		return new AEmptyForInit();
	}
	
	private static <T extends NodeInterface> T connectTokens(T node) {
		Token tok = null;
		for (Node child : node.getChildren(Node.class)) {
			TokenPair tp = AstUtil.getFirstAndLastToken(child);
			if (tok != null) {
				tok.setNext(tp.first);
				tp.first.setPrevious(tok);
			}
			tok = tp.last;
		}
		return node;
	}

//    // connect tokens within subtree in depth-first order
//    private static <T extends NodeInterface> T connectTokens(T subtree) {
//    	final Token[] prev = new Token[1];
//        subtree.apply(new DepthFirstAdapter() {
//            @Override
//            public void defaultToken(Token token) {
//                Token prevToken = prev[0];
//				if(prevToken != null)
//               		prevToken.setNext(token);
//               	token.setPrevious(prevToken);
//                prev[0] = token;
//                // allow for comma after parameters
//                if(token.parent() instanceof IFunction)
//                	if(((IFunction)token.parent()).getParameters().contains(token))
//                		sneakInComma();
//            }
//            
//            @Override public void outAVarDecl(AVarDecl node) { sneakInComma(); }
//            @Override public void outANormalObjectLiteralProperty(ANormalObjectLiteralProperty node) { sneakInComma(); }
//            @Override public void outAGetObjectLiteralProperty(AGetObjectLiteralProperty node) { sneakInComma(); }
//            @Override public void outASetObjectLiteralProperty(ASetObjectLiteralProperty node) { sneakInComma(); }
//            
////            @Override
////            public void caseAInvokeExp(AInvokeExp node) {
////            	node.getFunctionExp().apply(this);
////            	node.getLparen().apply(this);
////            	visitCommaList(node.getArguments());
////            	node.getRparen().apply(this);
////            }
////            
////            @Override
////            public void caseAArrayLiteralExp(AArrayLiteralExp node) {
////        		node.getLbrack().apply(this);
////            	visitCommaList(node.getValues());
////        		node.getRbrack().apply(this);
////            }
////            
////			private void visitCommaList(LinkedList<? extends Node> args) {
////				boolean first=false;
////				for (Node exp : args) {
////            		if (first) {
////            			first = false;
////            		} else {
////            			sneakInComma();
////            		}
////            		exp.apply(this);
////            	}
////			}
//            
//            private void sneakInComma() {
//            	// hack: the commas between variable declarations and property initializers do not 
//            	// appear to be in the AST, so we need to smuggle them in
//            	Token prevToken = prev[0];
//            	if(prevToken != null && prevToken.getNext() instanceof TComma)
//            		prev[0] = prevToken.getNext();
//            }
//        });
//        return subtree;
//    }

    // node clone methods mess up the token chain
	@SuppressWarnings("unchecked")
	public static <T extends NodeInterface> T clone(T node) {
		return connectTokens((T)node.clone());
	}

}
