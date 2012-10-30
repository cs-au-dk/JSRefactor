package dk.brics.jscontrolflow.ast2cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class CompositeAstBinding implements IAstBinding {
  private List<IAstBinding> children = new ArrayList<IAstBinding>();
  
  public List<IAstBinding> getChildren() {
    return children;
  }
  
  public CompositeAstBinding() {}
  
  public CompositeAstBinding(IAstBinding ... children) {
    this.children.addAll(Arrays.asList(children));
  }

  @Override
  public void addAssignExp(AAssignExp node, WriteStatement write) {
    for (IAstBinding c : children) {
      c.addAssignExp(node, write);
    }
  }

  @Override
  public void addBinopExp(ABinopExp exp, BinaryOperation stm) {
    for (IAstBinding c : children) {
      c.addBinopExp(exp, stm);
    }
  }

  @Override
  public void addLogicalExp(ABinopExp node, Phi stm) {
    for (IAstBinding c : children) {
      c.addLogicalExp(node, stm);
    }
  }

  @Override
  public void addArrayExp(AArrayLiteralExp exp, NewArray stm) {
    for (IAstBinding c : children) {
      c.addArrayExp(exp, stm);
    }
  }

  @Override
  public void addConditionalExp(AConditionalExp exp, Phi stm) {
    for (IAstBinding c : children) {
      c.addConditionalExp(exp, stm);
    }
  }

  @Override
  public void addConstExp(AConstExp exp, Assignment asn) {
    for (IAstBinding c : children) {
      c.addConstExp(exp, asn);
    }
  }

  @Override
  public void addThisExp(AThisExp exp, ReadThis stm) {
    for (IAstBinding c : children) {
      c.addThisExp(exp, stm);
    }
  }

  @Override
  public void addRegexpExp(ARegexpExp exp, NewRegexp stm) {
    for (IAstBinding c : children) {
      c.addRegexpExp(exp, stm);
    }
  }

  @Override
  public void addFunctionExp(AFunctionExp exp, CreateFunction stm) {
    for (IAstBinding c : children) {
      c.addFunctionExp(exp, stm);
    }
  }

  @Override
  public void addObjectLiteralExp(AObjectLiteralExp exp, NewObject stm) {
    for (IAstBinding c : children) {
      c.addObjectLiteralExp(exp, stm);
    }
  }

  @Override
  public void addPrefixUnopExp(APrefixUnopExp exp, UnaryOperation stm) {
    for (IAstBinding c : children) {
      c.addPrefixUnopExp(exp, stm);
    }
  }

  @Override
  public void addDeleteProperty(APrefixUnopExp deleteExp,
      APropertyExp propertyExp, DeleteProperty stm) {
    for (IAstBinding c : children) {
      c.addDeleteProperty(deleteExp, propertyExp, stm);
    }
  }

  @Override
  public void addDeleteDynamicProperty(APrefixUnopExp exp,
      ADynamicPropertyExp propertyExp, DeleteProperty stm) {
    for (IAstBinding c : children) {
      c.addDeleteDynamicProperty(exp, propertyExp, stm);
    }
  }

  @Override
  public void addInvalidDelete(APrefixUnopExp exp, BooleanConst stm) {
    for (IAstBinding c : children) {
      c.addInvalidDelete(exp, stm);
    }
  }

  @Override
  public void addPostfixUnopExp(APostfixUnopExp exp, UnaryOperation stm) {
    for (IAstBinding c : children) {
      c.addPostfixUnopExp(exp, stm);
    }
  }

  @Override
  public void addNameExp(ANameExp exp, ReadVariable stm) {
    for (IAstBinding c : children) {
      c.addNameExp(exp, stm);
    }
  }

  @Override
  public void addPropertyExp(IPropertyAccessNode exp, ReadProperty stm) {
    for (IAstBinding c : children) {
      c.addPropertyExp(exp, stm);
    }
  }

  @Override
  public void addNameLvalue(ANameExp exp, WriteVariable stm) {
    for (IAstBinding c : children) {
      c.addNameLvalue(exp, stm);
    }
  }

  @Override
  public void addPropertyLvalue(IPropertyAccessNode exp, WriteProperty stm) {
    for (IAstBinding c : children) {
      c.addPropertyLvalue(exp, stm);
    }
  }

  @Override
  public void addVarDeclLvalue(AVarDecl decl, WriteVariable stm) {
    for (IAstBinding c : children) {
      c.addVarDeclLvalue(decl, stm);
    }
  }

  @Override
  public void addObjectLiteralProperty(ANormalObjectLiteralProperty property, WriteProperty stm) {
    for (IAstBinding c : children) {
      c.addObjectLiteralProperty(property, stm);
    }
  }

  @Override
  public void addReturnVoid(AReturnStmt node, ReturnVoid stm) {
    for (IAstBinding c : children) {
      c.addReturnVoid(node, stm);
    }
  }

  @Override
  public void addReturn(AReturnStmt node, Return stm) {
    for (IAstBinding c : children) {
      c.addReturn(node, stm);
    }
  }

  @Override
  public void addThrow(AThrowStmt node, Throw stm) {
    for (IAstBinding c : children) {
      c.addThrow(node, stm);
    }
  }

  @Override
  public void addCatch(ACatchClause node, Catch ct, WriteVariable write) {
    for (IAstBinding c : children) {
      c.addCatch(node, ct, write);
    }
  }

  @Override
  public void addWith(AWithStmt node, EnterWith enter, LeaveScope leave, Block exceptionalBlock) {
    for (IAstBinding c : children) {
      c.addWith(node, enter, leave, exceptionalBlock);
    }
  }

  @Override
  public void addCondition(PExp exp, Assertion trueAssertion,
      Assertion falseAssertion) {
    for (IAstBinding c : children) {
      c.addCondition(exp, trueAssertion, falseAssertion);
    }
  }

  @Override
  public void addFunction(AFunctionDeclStmt fun, CreateFunction stm) {
    for (IAstBinding c : children) {
      c.addFunction(fun, stm);
    }
  }

  @Override
  public void addCallProperty(AInvokeExp invoke, APropertyExp exp,
      CallProperty statement) {
    for (IAstBinding c : children) {
      c.addCallProperty(invoke, exp, statement);
    }
  }

  @Override
  public void addCallProperty(AInvokeExp invoke, ADynamicPropertyExp exp,
      CallProperty statement) {
    for (IAstBinding c : children) {
      c.addCallProperty(invoke, exp, statement);
    }
  }

  @Override
  public void addCallVariable(AInvokeExp invoke, ANameExp exp,
      CallVariable statement) {
    for (IAstBinding c : children) {
      c.addCallVariable(invoke, exp, statement);
    }
  }

  @Override
  public void addCall(AInvokeExp invoke, Call statement) {
    for (IAstBinding c : children) {
      c.addCall(invoke, statement);
    }
  }

  @Override
  public void addNewExp(ANewExp exp, CallConstructor stm) {
    for (IAstBinding c : children) {
      c.addNewExp(exp, stm);
    }
  }

  @Override
  public void addGetNextProperty(AForInStmt forin, GetNextProperty stm) {
    for (IAstBinding c : children) {
      c.addGetNextProperty(forin, stm);
    }
  }

  @Override
  public void setScope(IScopeBlockNode node, Scope scope) {
    for (IAstBinding c : children) {
      c.setScope(node, scope);
    }
  }

  @Override
  public void addExp(PExp exp, int var, Statement stm) {
    for (IAstBinding c : children) {
      c.addExp(exp, var, stm);
    }
  }

  @Override
  public void addStmt(PStmt stmt, Statement stm) {
    for (IAstBinding c : children) {
      c.addStmt(stmt, stm);
    }
  }
  
  
}
