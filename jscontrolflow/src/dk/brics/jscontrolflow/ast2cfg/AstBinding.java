package dk.brics.jscontrolflow.ast2cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
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
import dk.brics.jscontrolflow.statements.IPropertyAccessStatement;
import dk.brics.jscontrolflow.statements.IVariableAccessStatement;
import dk.brics.jscontrolflow.statements.InvokeStatement;
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
import dk.brics.jsparser.node.IExpOrStmt;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IInvocationNode;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsutil.MultiMap;

/**
 * Stores the relationship between AST and CFG.
 * <p/>
 * AST nodes map to <i>sets</i> of statements because of cloning in the control-flow graph.
 * All statements in such a set are guaranteed to have the same type.
 */
public class AstBinding implements IAstBinding {
    private MultiMap<IInvocationNode,InvokeStatement> invocations = new MultiMap<IInvocationNode,InvokeStatement>();
    private MultiMap<IPropertyAccessNode, IPropertyAccessStatement> properties = new MultiMap<IPropertyAccessNode,IPropertyAccessStatement>();
    private Map<IPropertyAccessStatement,IPropertyAccessNode> propertyInverse = new HashMap<IPropertyAccessStatement, IPropertyAccessNode>();
    private MultiMap<ANameExp, IVariableAccessStatement> variables = new MultiMap<ANameExp, IVariableAccessStatement>();
    private Map<IVariableAccessStatement, ANameExp> variablesInverse = new HashMap<IVariableAccessStatement, ANameExp>();
    private MultiMap<PExp, Assignment> otherExps = new MultiMap<PExp, Assignment>();
    private MultiMap<ANormalObjectLiteralProperty, WriteProperty> objectLiteralProperties = new MultiMap<ANormalObjectLiteralProperty, WriteProperty>();
    private MultiMap<IFunction, CreateFunction> functions  = new MultiMap<IFunction, CreateFunction>();
    private Map<IFunction,Function> functionForward = new HashMap<IFunction,Function>();
    private Map<Function,IFunction> functionInverse = new HashMap<Function,IFunction>();
    private MultiMap<AForInStmt,GetNextProperty> forins = new MultiMap<AForInStmt, GetNextProperty>();
    private Map<GetNextProperty,AForInStmt> forinInverse = new HashMap<GetNextProperty, AForInStmt>();
    private MultiMap<AVarDecl, WriteVariable> varDecls = new MultiMap<AVarDecl, WriteVariable>();
    private Map<IScopeBlockNode, Scope> scopes = new HashMap<IScopeBlockNode, Scope>();
    private Map<Scope, IScopeBlockNode> scopeInverse = new HashMap<Scope, IScopeBlockNode>();
    private MultiMap<AWithStmt, EnterWith> with = new MultiMap<AWithStmt, EnterWith>();
    private MultiMap<PExp, Integer> exp2resultVar = new MultiMap<PExp, Integer>();
    private MultiMap<IExpOrStmt, Statement> completion = new MultiMap<IExpOrStmt, Statement>();
    
    @SuppressWarnings("unchecked")
    private <T> Set<? extends T> getOtherExp(PExp exp) {
        return (Set<? extends T>)otherExps.getView(exp);
    }

    public Set<? extends InvokeStatement> getInvokeStatements(IInvocationNode node) {
        return invocations.getView(node);
    }
    public Set<? extends IPropertyAccessStatement> getPropertyAccesses(IPropertyAccessNode exp) {
        return properties.getView(exp);
    }
    public Set<? extends IVariableAccessStatement> getVariableAccesses(ANameExp exp) {
        return variables.getView(exp);
    }
    public Set<? extends NewObject> getObjectLiteral(AObjectLiteralExp exp) {
        return getOtherExp(exp);
    }
    public Set<? extends WriteProperty> getObjectLiteralProperty(ANormalObjectLiteralProperty property) {
        return objectLiteralProperties.getView(property);
    }
    public Set<? extends BinaryOperation> getBinaryOperations(ABinopExp exp) {
    	return getOtherExp(exp);
    }
    public Set<? extends CreateFunction> getFunctions(IFunction fun) {
    	return functions.getView(fun);
    }
    public IFunction getFunctionNode(Function func) {
        return functionInverse.get(func);
    }
    public Function getFunction(IFunction node) {
        return functionForward.get(node);
    }
    public Set<? extends GetNextProperty> getGetNextProperty(AForInStmt stmt) {
        return forins.getView(stmt);
    }
    public AForInStmt getForInStmt(GetNextProperty stm) {
        return forinInverse.get(stm);
    }
    public Set<? extends WriteVariable> getVarDecl(AVarDecl decl) {
        return varDecls.getView(decl);
    }
    public IPropertyAccessNode getPropertyAccessNode(IPropertyAccessStatement stm) {
        return propertyInverse.get(stm);
    }
    public ANameExp getNameExp(IVariableAccessStatement stm) {
    	return variablesInverse.get(stm);
    }
    public Set<EnterWith> getEnterWith(AWithStmt stmt) {
    	return with.getView(stmt);
    }
    public Set<Integer> getExpResultVar(PExp exp) {
        return exp2resultVar.getView(exp);
    }
    
    public Set<? extends Assignment> getAllocation(PExp exp) {
    	return otherExps.getView(exp);
    }
    
    @Override
    public void addCallProperty(AInvokeExp invoke, APropertyExp node, CallProperty statement) {
        properties.add(node, statement);
        propertyInverse.put(statement, node);
        invocations.add(invoke, statement);
    }
    @Override
    public void addCallProperty(AInvokeExp invoke, ADynamicPropertyExp node, CallProperty statement) {
        properties.add(node, statement);
        propertyInverse.put(statement, node);
        invocations.add(invoke, statement);
    }
    @Override
    public void addCallVariable(AInvokeExp invoke, ANameExp node, CallVariable statement) {
        variables.add(node, statement);
        variablesInverse.put(statement, node);
        invocations.add(invoke, statement);
    }
    @Override
    public void addCall(AInvokeExp invoke, Call statement) {
        invocations.add(invoke, statement);
    }
    
    @Override
    public void addPropertyExp(IPropertyAccessNode exp, ReadProperty stm) {
        properties.add(exp, stm);
        propertyInverse.put(stm, exp);
    }
    @Override
    public void addPropertyLvalue(IPropertyAccessNode exp, WriteProperty stm) {
        properties.add(exp, stm);
        propertyInverse.put(stm, exp);
    }

    @Override
    public void addNewExp(ANewExp key, CallConstructor value) {
        invocations.add(key, value);
        otherExps.add(key, value);
    }
    
    @Override
    public void addDeleteDynamicProperty(APrefixUnopExp exp, ADynamicPropertyExp propertyExp, DeleteProperty stm) {
        properties.add(propertyExp, stm);
        propertyInverse.put(stm, propertyExp);
    }
    @Override
    public void addDeleteProperty(APrefixUnopExp deleteExp, APropertyExp propertyExp, DeleteProperty stm) {
        properties.add(propertyExp, stm);
        propertyInverse.put(stm, propertyExp);
    }
    
    @Override
    public void addObjectLiteralExp(AObjectLiteralExp exp, NewObject stm) {
        otherExps.add(exp, stm);
    }
    @Override
    public void addObjectLiteralProperty(ANormalObjectLiteralProperty property, WriteProperty stm) {
        objectLiteralProperties.add(property, stm);
    }
    
    @Override
    public void addNameLvalue(ANameExp exp, WriteVariable stm) {
        variables.add(exp, stm);
        variablesInverse.put(stm, exp);
    }
    @Override
    public void addNameExp(ANameExp exp, ReadVariable stm) {
        variables.add(exp, stm);
        variablesInverse.put(stm, exp);
    }

    @Override
    public void addBinopExp(ABinopExp exp, BinaryOperation stm) {
    	otherExps.add(exp, stm);
    }
    @Override
    public void addLogicalExp(ABinopExp exp, Phi stm) {
    }
    @Override
    public void addArrayExp(AArrayLiteralExp exp, NewArray stm) {
    	otherExps.add(exp, stm);
    }
    @Override
    public void addConditionalExp(AConditionalExp exp, Phi stm) {
    }
    @Override
    public void addConstExp(AConstExp exp, Assignment stm) {
    }
    @Override
    public void addThisExp(AThisExp exp, ReadThis stm) {
    }
    @Override
    public void addRegexpExp(ARegexpExp exp, NewRegexp stm) {
    	otherExps.add(exp, stm);
    }
    @Override
    public void addFunctionExp(AFunctionExp exp, CreateFunction stm) {
    	functions.add(exp, stm);
    	functionInverse.put(stm.getFunction(), exp);
    	functionForward.put(exp, stm.getFunction());
    }
    @Override
    public void addPrefixUnopExp(APrefixUnopExp exp, UnaryOperation stm) {
    }
    @Override
    public void addInvalidDelete(APrefixUnopExp exp, BooleanConst stm) {
    }
    @Override
    public void addPostfixUnopExp(APostfixUnopExp exp, UnaryOperation stm) {
    }
    @Override
    public void addVarDeclLvalue(AVarDecl decl, WriteVariable stm) {
        varDecls.add(decl, stm);
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
    public void addWith(AWithStmt node, EnterWith enter, LeaveScope leave, Block exceptionalBlock) {
    	with.add(node, enter);
    }
    @Override
    public void addCondition(PExp exp, Assertion trueAssertion, Assertion falseAssertion) {
    }

	@Override
	public void addFunction(AFunctionDeclStmt fun, CreateFunction stm) {
		functions.add(fun, stm);
		functionInverse.put(stm.getFunction(), fun);
		functionForward.put(fun, stm.getFunction());
	}
	
	@Override
	public void addGetNextProperty(AForInStmt forin, GetNextProperty stm) {
	    forins.add(forin, stm);
	    forinInverse.put(stm, forin);
	}

    @Override
    public void setScope(IScopeBlockNode node, Scope scope) {
        scopes.put(node, scope);
        scopeInverse.put(scope, node);
    }
	
    public Scope getScope(IScopeBlockNode node) {
        return scopes.get(node);
    }
    public IScopeBlockNode getScopeNode(Scope scope) {
        return scopeInverse.get(scope);
    }

    MultiMap<AAssignExp, WriteStatement> assignExps = new MultiMap<AAssignExp, WriteStatement>();
	@Override
	public void addAssignExp(AAssignExp node, WriteStatement write) {
		assignExps.add(node, write);
	}
    
	public Set<WriteStatement> getWrites(AAssignExp node) {
		return assignExps.getView(node);
	}
	
	@Override
	public void addExp(PExp exp, int var, Statement stm) {
	    exp2resultVar.add(exp, var);
	    completion.add(exp, stm);
	}
	@Override
	public void addStmt(PStmt stmt, Statement stm) {
		completion.add(stmt, stm);
	}
	
	public Set<Statement> getCompletionPoint(IExpOrStmt expOrStmt) {
		return completion.getView(expOrStmt);
	}
}
