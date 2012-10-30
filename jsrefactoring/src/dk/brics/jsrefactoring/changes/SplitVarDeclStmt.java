package dk.brics.jsrefactoring.changes;

import java.util.Collections;
import java.util.Set;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.Start;
import dk.brics.jsparser.node.TSemicolon;
import dk.brics.jsparser.node.TVar;
import dk.brics.jsparser.node.TWhitespace;
import dk.brics.jsparser.node.Token;
import dk.brics.jsparser.node.TokenEnum;
import dk.brics.jsrefactoring.PrettyPrinter;

public class SplitVarDeclStmt extends Change {
	private final AVarDeclStmt stmt;
	private final int index;

	public SplitVarDeclStmt(AVarDeclStmt stmt, int index) {
		this.stmt = stmt;
		this.index = index;
	}

	@Override
	public Set<Start> getAffectedScripts() {
		return Collections.singleton(stmt.getRoot());
	}

	@Override
	public void perform() {
		if(index == 0 || index == stmt.getDecls().size())
			return;
		AVarDecl vd = stmt.getDecls().get(index);
		TokenPair tokens = AstUtil.getFirstAndLastToken(vd);
		
		// look for preceding comma
		Token prec_comma = tokens.first.getPrevious();
		while(prec_comma.kindToken() != TokenEnum.COMMA)
			prec_comma = prec_comma.getPrevious();
		
		// replace comma with "; var "
		Token[] newTokens = new Token[] { new TSemicolon(), new TWhitespace(" "), new TVar(), new TWhitespace(" ") };
		Token tk = prec_comma.getPrevious();
		for(int i=0;i<newTokens.length;++i) {
			AstUtil.insertTokenAfter(tk, newTokens[i]);
			tk = newTokens[i];
		}
		PrettyPrinter.connect(tk, prec_comma.getNext());
	}

	@Override
	public <Q, A> A apply(ChangeVisitor<Q, A> v, Q arg) {
		return v.caseSplitVarDeclStmt(this, arg);
	}

}
