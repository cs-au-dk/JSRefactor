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
 * Receives notification of which AST nodes correspond to which
 * statements in the control-flow graph.
 * <p/>
 * Due to cloning of <tt>finally</tt> blocks in the control-flow graph, 
 * a particular <tt>add</tt>-method may be called with the same AST
 * node more than once; but this <i>only</i> occurs when such cloning takes place.
 * <p/>
 * Some AST nodes may be sent to more than one <tt>add</tt> method, for example,
 * in an expression like <tt>x++</tt>, the <tt>ANameExp</tt> representing <tt>x</tt>
 * will be sent to both {@link #addNameExp(ANameExp, ReadVariable) addNameExp} and 
 * {@link #addNameLvalue(ANameExp, WriteVariable) addNameLvalue}, since it is both read from
 * and written to. 
 */
public interface IAstBinding {
	void addAssignExp(AAssignExp node, WriteStatement write);
    void addBinopExp(ABinopExp exp, BinaryOperation stm);
    void addLogicalExp(ABinopExp node, Phi stm);
    void addArrayExp(AArrayLiteralExp exp, NewArray stm);
    void addConditionalExp(AConditionalExp exp, Phi stm);
    void addConstExp(AConstExp exp, Assignment asn);
    void addThisExp(AThisExp exp, ReadThis stm);
    void addRegexpExp(ARegexpExp exp, NewRegexp stm);
    void addFunctionExp(AFunctionExp exp, CreateFunction stm);
    void addObjectLiteralExp(AObjectLiteralExp exp, NewObject stm);
    void addPrefixUnopExp(APrefixUnopExp exp, UnaryOperation stm);
    void addDeleteProperty(APrefixUnopExp deleteExp, APropertyExp propertyExp, DeleteProperty stm);
    void addDeleteDynamicProperty(APrefixUnopExp exp, ADynamicPropertyExp propertyExp, DeleteProperty stm);
    void addInvalidDelete(APrefixUnopExp exp, BooleanConst stm);
    void addPostfixUnopExp(APostfixUnopExp exp, UnaryOperation stm);
    void addNameExp(ANameExp exp, ReadVariable stm);
    void addPropertyExp(IPropertyAccessNode exp, ReadProperty stm);
    void addNameLvalue(ANameExp exp, WriteVariable stm);
    void addPropertyLvalue(IPropertyAccessNode exp, WriteProperty stm);
    void addVarDeclLvalue(AVarDecl decl, WriteVariable stm);
	void addObjectLiteralProperty(ANormalObjectLiteralProperty property, WriteProperty stm);
    void addReturnVoid(AReturnStmt node, ReturnVoid stm);
    void addReturn(AReturnStmt node, Return stm);
    void addThrow(AThrowStmt node, Throw stm);
    void addCatch(ACatchClause node, Catch ct, WriteVariable write);
    void addWith(AWithStmt node, EnterWith enter, LeaveScope leave, Block exceptionalBlock);
    void addCondition(PExp exp, Assertion trueAssertion, Assertion falseAssertion);
    void addFunction(AFunctionDeclStmt fun, CreateFunction stm);
    
    void addCallProperty(AInvokeExp invoke, APropertyExp exp, CallProperty statement);
    void addCallProperty(AInvokeExp invoke, ADynamicPropertyExp exp, CallProperty statement);
    void addCallVariable(AInvokeExp invoke, ANameExp exp, CallVariable statement);
    void addCall(AInvokeExp invoke, Call statement);
    void addNewExp(ANewExp exp, CallConstructor stm);
    
    void addGetNextProperty(AForInStmt forin, GetNextProperty stm);
    
    /**
     * Sets the (unique) scope associated with the given AST node. Will be called
     * before the {@link Scope} object is used in any statements.
     * @param node an AST node
     * @param scope a CFG scope object
     */
    void setScope(IScopeBlockNode node, Scope scope);
    
    /**
     * Invoked for all expressions EXCEPT the following:
     * <ul>
     * <li>The condition of an if statement or conditional expression (nested in 0 or more parentheses).
     * <li>The function expression of a non-constructor invocation (nested in 0 or more parentheses). 
     * <li>The operand to a <tt>delete</tt> expression (nested in 0 or more parentheses). 
     * </ul>
     * @param exp an expression not of the type listed above
     * @param var a variable holding the result of the expression
     * @param stm statement after which the expression just completed 
     */
    void addExp(PExp exp, int var, Statement stm);
    
    /**
     * Invoked for all statements.
     * @param stmt an AST statement
     * @param stm statement after which the statement just completed
     */
    void addStmt(PStmt stmt, Statement stm);
}
