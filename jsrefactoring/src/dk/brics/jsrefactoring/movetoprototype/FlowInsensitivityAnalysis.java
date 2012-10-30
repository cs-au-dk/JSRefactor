package dk.brics.jsrefactoring.movetoprototype;

import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ACommaExp;
import dk.brics.jsparser.node.AConditionalExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.EPrefixUnop;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PObjectLiteralProperty;

/**
 * This analysis does a syntactic check to determine whether a given expression is 
 * <i>flow insensitive</i>, meaning that its evaluation has no side effects, does 
 * not read or write variables or properties, does not delete properties and throws 
 * no exceptions. Thus, a flow insensitive expression does not have any incoming or
 * outgoing data flow dependencies, and no outgoing control dependencies.
 * 
 * <p>
 * The analysis is slightly unsound in that it considers unary and binary expressions to be
 * flow insensitive, even though their evaluation might actually throw an exception (for instance
 * if the right hand operand to <code>instanceof</code> is not an object).
 * </p>
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
final class FlowInsensitivityAnalysis extends AnswerAdapter<Boolean> {
	@Override
	public Boolean defaultPExp(PExp exp) {
		return false;
	}

	@Override
	public Boolean caseAArrayLiteralExp(AArrayLiteralExp ary) {
		for(PExp value : ary.getValues())
			if(!value.apply(this))
				return false;
		return true;
	}

	@Override
	public Boolean caseABinopExp(ABinopExp exp) {
		return exp.getLeft().apply(this) && exp.getRight().apply(this);
	}

	@Override
	public Boolean caseACommaExp(ACommaExp exp) {
		return exp.getFirstExp().apply(this) && exp.getSecondExp().apply(this);
	}

	@Override
	public Boolean caseAConditionalExp(AConditionalExp exp) {
		return exp.getCondition().apply(this) && exp.getTrueExp().apply(this) && exp.getFalseExp().apply(this);
	}

	@Override
	public Boolean caseAConstExp(AConstExp node) {
		return true;
	}

	@Override
	public Boolean caseAFunctionExp(AFunctionExp node) {
		return true;
	}

	@Override
	public Boolean caseAObjectLiteralExp(AObjectLiteralExp lit) {
		for(PObjectLiteralProperty prop : lit.getProperties()) {
			switch(prop.kindPObjectLiteralProperty()) {
			case GET:
			case SET:
				return false;
			default:
				String name = Literals.getName(prop.getName());
				if(name.equals("toString") || name.equals("valueOf"))
					return false;
				if(!((ANormalObjectLiteralProperty)prop).getValue().apply(this))
					return false;
			}
		}
		return true;
	}

	@Override
	public Boolean caseAParenthesisExp(AParenthesisExp node) {
		return node.getExp().apply(this);
	}

	@Override
	public Boolean caseAPrefixUnopExp(APrefixUnopExp node) {
		EPrefixUnop kind = node.getOp().kindPPrefixUnop();
		if(kind == EPrefixUnop.DELETE || kind == EPrefixUnop.INCREMENT || kind == EPrefixUnop.DECREMENT)
			return false;
		return node.getExp().apply(this);
	}

	@Override
	public Boolean caseARegexpExp(ARegexpExp node) {
		return true;
	}
}