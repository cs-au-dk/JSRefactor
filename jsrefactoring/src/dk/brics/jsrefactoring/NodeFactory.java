package dk.brics.jsrefactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ABitwiseAndBinop;
import dk.brics.jsparser.node.ABitwiseOrBinop;
import dk.brics.jsparser.node.ABitwiseXorBinop;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADivideBinop;
import dk.brics.jsparser.node.AEmptyForInit;
import dk.brics.jsparser.node.AEmptyStmt;
import dk.brics.jsparser.node.AEqualBinop;
import dk.brics.jsparser.node.AEqualStrictBinop;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AGetObjectLiteralProperty;
import dk.brics.jsparser.node.AGreaterBinop;
import dk.brics.jsparser.node.AGreaterEqualBinop;
import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.AInBinop;
import dk.brics.jsparser.node.AInstanceofBinop;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ALessBinop;
import dk.brics.jsparser.node.ALessEqualBinop;
import dk.brics.jsparser.node.ALogicalAndBinop;
import dk.brics.jsparser.node.ALogicalOrBinop;
import dk.brics.jsparser.node.AMinusBinop;
import dk.brics.jsparser.node.AModuloBinop;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalAssignOp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.ANotEqualBinop;
import dk.brics.jsparser.node.ANotEqualStrictBinop;
import dk.brics.jsparser.node.ANullConst;
import dk.brics.jsparser.node.ANumberConst;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APlusBinop;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AReturnStmt;
import dk.brics.jsparser.node.ASetObjectLiteralProperty;
import dk.brics.jsparser.node.AShiftLeftBinop;
import dk.brics.jsparser.node.AShiftRightBinop;
import dk.brics.jsparser.node.AShiftRightUnsignedBinop;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.ATimesBinop;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.PAssignOp;
import dk.brics.jsparser.node.PBinop;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PObjectLiteralProperty;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.TAnd;
import dk.brics.jsparser.node.TAndAnd;
import dk.brics.jsparser.node.TColon;
import dk.brics.jsparser.node.TComma;
import dk.brics.jsparser.node.TDot;
import dk.brics.jsparser.node.TEq;
import dk.brics.jsparser.node.TEqEq;
import dk.brics.jsparser.node.TEqEqEq;
import dk.brics.jsparser.node.TFunction;
import dk.brics.jsparser.node.TGt;
import dk.brics.jsparser.node.TGtEq;
import dk.brics.jsparser.node.TGtGt;
import dk.brics.jsparser.node.TGtGtGt;
import dk.brics.jsparser.node.TId;
import dk.brics.jsparser.node.TIn;
import dk.brics.jsparser.node.TInstanceof;
import dk.brics.jsparser.node.TLbrace;
import dk.brics.jsparser.node.TLparen;
import dk.brics.jsparser.node.TLt;
import dk.brics.jsparser.node.TLtEq;
import dk.brics.jsparser.node.TLtLt;
import dk.brics.jsparser.node.TMinus;
import dk.brics.jsparser.node.TModulo;
import dk.brics.jsparser.node.TNotEq;
import dk.brics.jsparser.node.TNotEqEq;
import dk.brics.jsparser.node.TNull;
import dk.brics.jsparser.node.TNumberLiteral;
import dk.brics.jsparser.node.TOr;
import dk.brics.jsparser.node.TOrOr;
import dk.brics.jsparser.node.TPlus;
import dk.brics.jsparser.node.TRbrace;
import dk.brics.jsparser.node.TReturn;
import dk.brics.jsparser.node.TRparen;
import dk.brics.jsparser.node.TSemicolon;
import dk.brics.jsparser.node.TSlash;
import dk.brics.jsparser.node.TStar;
import dk.brics.jsparser.node.TThis;
import dk.brics.jsparser.node.TVar;
import dk.brics.jsparser.node.TWhitespace;
import dk.brics.jsparser.node.TXor;
import dk.brics.jsparser.node.Token;
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
	
	public static AInvokeExp createInvokeExp(PExp fn, List<PExp> args) {
		return connectTokens(new AInvokeExp(fn, new TLparen(), args, new TRparen()));
	}
	
	public static AAssignExp createAssignExp(PExp left, PExp right) {
		return connectTokens(new AAssignExp(left, new ANormalAssignOp(new TEq()), right));
	}
	
	public static AAssignExp createAssignExp(PExp left, PAssignOp op, PExp right) {
		return connectTokens(new AAssignExp(left, op, right));
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
			properties.add(new ANormalObjectLiteralProperty(new AIdentifierPropertyName(new TId(init.fst)), new TColon(), init.snd));
		// need to insert commas between the initializers
		AObjectLiteralExp res = connectTokens(new AObjectLiteralExp(new TLbrace(), properties , null, new TRbrace()));
		for(int i=0;i<inits.size()-1;++i) {
			PObjectLiteralProperty prop = res.getProperties().get(i);
			Token last = AstUtil.getFirstAndLastToken(prop).last;
			AstUtil.insertTokenAfter(last, new TComma());
		}
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

    // connect tokens within subtree in depth-first order
    private static <T extends NodeInterface> T connectTokens(T subtree) {
    	final Token[] prev = new Token[1];
        subtree.apply(new DepthFirstAdapter() {
            @Override
            public void defaultToken(Token token) {
                Token prevToken = prev[0];
				if(prevToken != null)
               		prevToken.setNext(token);
               	token.setPrevious(prevToken);
                prev[0] = token;
                // allow for comma after parameters
                if(token.parent() instanceof IFunction)
                	if(((IFunction)token.parent()).getParameters().contains(token))
                		sneakInComma();
            }
            
            @Override public void outAVarDecl(AVarDecl node) { sneakInComma(); }
            @Override public void outANormalObjectLiteralProperty(ANormalObjectLiteralProperty node) { sneakInComma(); }
            @Override public void outAGetObjectLiteralProperty(AGetObjectLiteralProperty node) { sneakInComma(); }
            @Override public void outASetObjectLiteralProperty(ASetObjectLiteralProperty node) { sneakInComma(); }
            
            private void sneakInComma() {
            	// hack: the commas between variable declarations and property initializers do not 
            	// appear to be in the AST, so we need to smuggle them in
            	Token prevToken = prev[0];
            	if(prevToken != null && prevToken.getNext() instanceof TComma)
            		prev[0] = prevToken.getNext();
            }
        });
        return subtree;
    }

    // node clone methods mess up the token chain
	@SuppressWarnings("unchecked")
	public static <T extends NodeInterface> T clone(T node) {
		return connectTokens((T)node.clone());
	}

}
