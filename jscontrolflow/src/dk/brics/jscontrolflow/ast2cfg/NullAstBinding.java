package dk.brics.jscontrolflow.ast2cfg;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.statements.Assertion;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.BooleanConst;
import dk.brics.jscontrolflow.statements.Call;
import dk.brics.jscontrolflow.statements.CallConstructor;
import dk.brics.jscontrolflow.statements.CallProperty;
import dk.brics.jscontrolflow.statements.CallVariable;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.EnterWith;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.LeaveScope;
import dk.brics.jscontrolflow.statements.NewArray;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.NewRegexp;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadThis;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.Return;
import dk.brics.jscontrolflow.statements.ReturnVoid;
import dk.brics.jscontrolflow.statements.Throw;
import dk.brics.jscontrolflow.statements.UnaryOperation;
import dk.brics.jscontrolflow.statements.WriteProperty;
import dk.brics.jscontrolflow.statements.WriteStatement;
import dk.brics.jscontrolflow.statements.WriteVariable;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.AConditionalExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.APostfixUnopExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.AReturnStmt;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.AThrowStmt;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AWithStmt;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PStmt;

/**
 * Implementation of {@link IAstBinding} that does nothing.
 * Use this if you are not interested in the relation between AST
 * and control-flow graph.
 */
public class NullAstBinding implements IAstBinding {

    @Override
	public void addStmt(PStmt stmt, Statement stm) {
	}

	@Override
    public void addObjectLiteralProperty(ANormalObjectLiteralProperty property,
            WriteProperty stm) {
    }

    @Override
    public void addExp(PExp exp, int var, Statement stm) {
    }

    @Override
    public void addBinopExp(ABinopExp exp, BinaryOperation stm) {
    }

    @Override
    public void addGetNextProperty(AForInStmt forin, GetNextProperty stm) {
    }

    @Override
    public void addLogicalExp(ABinopExp node, Phi stm) {
    }

    @Override
    public void addArrayExp(AArrayLiteralExp exp, NewArray stm) {
    }

    @Override
    public void addConditionalExp(AConditionalExp exp, Phi phi) {
    }

    @Override
    public void addConstExp(AConstExp exp, Assignment asn) {
    }

    @Override
    public void addThisExp(AThisExp exp, ReadThis stm) {
    }

    @Override
    public void addRegexpExp(ARegexpExp exp, NewRegexp stm) {
    }

    @Override
    public void addFunctionExp(AFunctionExp exp, CreateFunction stm) {
    }

    @Override
    public void addObjectLiteralExp(AObjectLiteralExp exp, NewObject stm) {
    }

    @Override
    public void addNewExp(ANewExp exp, CallConstructor stm) {
    }

    @Override
    public void addCallProperty(AInvokeExp invoke, APropertyExp exp,
            CallProperty statement) {
    }

    @Override
    public void addCallProperty(AInvokeExp invoke, ADynamicPropertyExp exp,
            CallProperty statement) {
    }

    @Override
    public void addCallVariable(AInvokeExp invoke, ANameExp exp,
            CallVariable statement) {
    }

    @Override
    public void addCall(AInvokeExp invoke, Call statement) {
    }

    @Override
    public void addPrefixUnopExp(APrefixUnopExp exp, UnaryOperation stm) {
    }

    @Override
    public void addDeleteProperty(APrefixUnopExp deleteExp,
            APropertyExp propertyExp, DeleteProperty stm) {
    }

    @Override
    public void addDeleteDynamicProperty(APrefixUnopExp exp,
            ADynamicPropertyExp propertyExp, DeleteProperty stm) {
    }

    @Override
    public void addPostfixUnopExp(APostfixUnopExp exp, UnaryOperation stm) {
    }

    @Override
    public void addNameExp(ANameExp exp, ReadVariable stm) {
    }


    @Override
    public void addNameLvalue(ANameExp exp, WriteVariable stm) {
    }


    @Override
    public void addPropertyExp(IPropertyAccessNode exp, ReadProperty stm) {
    }

    @Override
    public void addPropertyLvalue(IPropertyAccessNode exp, WriteProperty stm) {
    }

    @Override
    public void addVarDeclLvalue(AVarDecl decl, WriteVariable stm) {
    }

    @Override
    public void addInvalidDelete(APrefixUnopExp exp, BooleanConst stm) {
    }

    @Override
    public void addReturnVoid(AReturnStmt node, ReturnVoid stm) {
    }

    @Override
    public void addReturn(AReturnStmt node, Return stm) {
    }

    @Override
    public void addThrow(AThrowStmt node, Throw stm) {
    }

    @Override
    public void addCatch(ACatchClause node, Catch ct, WriteVariable write) {
    }

    @Override
    public void addWith(AWithStmt node, EnterWith enter, LeaveScope leave,
            Block exceptionalBlock) {
    }
    @Override
    public void addCondition(PExp exp, Assertion trueAssertion,
            Assertion falseAssertion) {
    }

	@Override
	public void addFunction(AFunctionDeclStmt fun, CreateFunction stm) {
	}

    @Override
    public void setScope(IScopeBlockNode node, Scope scope) {
    }

	@Override
	public void addAssignExp(AAssignExp node, WriteStatement write) {
		// TODO Auto-generated method stub
		
	}
	
}
