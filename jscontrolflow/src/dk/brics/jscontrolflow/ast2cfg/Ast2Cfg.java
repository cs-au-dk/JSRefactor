package dk.brics.jscontrolflow.ast2cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.SourceLocation;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.checks.CheckWellformed;
import dk.brics.jscontrolflow.scope.CatchScope;
import dk.brics.jscontrolflow.scope.Scope;
import dk.brics.jscontrolflow.scope.WithScope;
import dk.brics.jscontrolflow.statements.*;
import dk.brics.jscontrolflow.transforms.SimplifyGraph;
import dk.brics.jsparser.ErrorTolerance;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.analysis.AnswerAdapter;
import dk.brics.jsparser.analysis.QuestionAnswerAdapter;
import dk.brics.jsparser.node.AArrayLiteralExp;
import dk.brics.jsparser.node.AAssignExp;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ABlock;
import dk.brics.jsparser.node.ABlockStmt;
import dk.brics.jsparser.node.ABody;
import dk.brics.jsparser.node.ABooleanConst;
import dk.brics.jsparser.node.ABreakStmt;
import dk.brics.jsparser.node.ACaseSwitchClause;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.ACommaExp;
import dk.brics.jsparser.node.AConditionalExp;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.AContinueStmt;
import dk.brics.jsparser.node.ADebuggerStmt;
import dk.brics.jsparser.node.ADoStmt;
import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AEmptyExp;
import dk.brics.jsparser.node.AEmptyForInit;
import dk.brics.jsparser.node.AEmptyStmt;
import dk.brics.jsparser.node.AExpForInit;
import dk.brics.jsparser.node.AExpStmt;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jsparser.node.AForStmt;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.AIdentifierPropertyName;
import dk.brics.jsparser.node.AIfStmt;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.ALabelledStmt;
import dk.brics.jsparser.node.ALvalueForInLvalue;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.ANumberConst;
import dk.brics.jsparser.node.ANumberPropertyName;
import dk.brics.jsparser.node.AObjectLiteralExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.APostfixUnopExp;
import dk.brics.jsparser.node.APrefixUnopExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.ARegexpExp;
import dk.brics.jsparser.node.AReturnStmt;
import dk.brics.jsparser.node.AStringConst;
import dk.brics.jsparser.node.AStringPropertyName;
import dk.brics.jsparser.node.ASwitchStmt;
import dk.brics.jsparser.node.AThisExp;
import dk.brics.jsparser.node.AThrowStmt;
import dk.brics.jsparser.node.ATrueBool;
import dk.brics.jsparser.node.ATryStmt;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.AVarDeclStmt;
import dk.brics.jsparser.node.AVarForInLvalue;
import dk.brics.jsparser.node.AVarForInit;
import dk.brics.jsparser.node.AWhileStmt;
import dk.brics.jsparser.node.AWithStmt;
import dk.brics.jsparser.node.EPostfixUnop;
import dk.brics.jsparser.node.IPropertyAccessNode;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.PObjectLiteralProperty;
import dk.brics.jsparser.node.PStmt;
import dk.brics.jsparser.node.PSwitchClause;
import dk.brics.jsparser.node.Token;

/**
 * Creates a control-flow graph of a JavaScript program. About the created
 * control-flow graph:
 * <ul>
 * <li>It is {@link CheckWellformed well-formed}.
 * <li>It may contain empty blocks as well as {@link Nop} statements. (The empty
 * blocks can be removed using {@link SimplifyGraph}.)
 * <li>It may contain unreachable blocks, some of which may be empty, where
 * others may contain actual dead code.
 * <li>Finally blocks are cloned for exceptional flow and normal flow and once
 * for every <tt>break</tt>, <tt>continue</tt> and <tt>return</tt> statement in
 * the try body and catch body.
 * <li>Some expressions in the AST may not have any corresponding statements in
 * the flow-graph, due to condition optimization (described below).
 * <li>All variables that are assigned to before they are used. At a {@link Phi}
 * , it is definitely true that one of its arguments is assigned. (This
 * requirement is not mandated by well-formedness).
 * </ul>
 * <p/>
 * <h2>Condition Optimization</h2>
 * Some expressions will be subject to <i>condition optimization</i> in order to
 * make the control-flow graph more suitable for dataflow analysis. The
 * <i>not</i> expression below is an example
 * 
 * <pre>
 * before: if (!E) {A} else {B}
 * after:  if (E) {B} else {A}
 * </pre>
 * 
 * This means certain expressions in the AST will not have their value computed
 * in the control-flow graph. No variable will at any point contain the value of
 * <tt>!E</tt> in the example above, due to the optimization. An expression
 * <i>E</i> will be subject to condition optimization if it 1) occurs as a
 * condition, and 2) it has an appropriate type. These are the places conditions
 * occur in ASTs (the condition being named <i>E</i>):
 * 
 * <pre>
 * if (E)
 * while (E)
 * do .. while (E)
 * for (..; E; ..)
 * E ? x : y
 * E && ..
 * E || ..
 * </pre>
 * 
 * The types of expressions that can be subject to the optimization are:
 * <tt>E1 && E2, E1 || E2, !E, true, false, E ? x : y</tt>
 * <p/>
 * 
 * @author Asger
 */
public class Ast2Cfg {
  /**
   * Converts the given AST into a control-flow graph. All inner functions are
   * included in the conversion.
   * 
   * @param body
   *          AST of the top-level scope.
   * @param binding
   *          see {@link IAstBinding}
   * @param file
   *          file to use when creating {@link SourceLocation}s
   * @return a new function representing the top-level scope.
   */
  public static Function convert(ABody body, IAstBinding binding, File file) {
    Function func = new Function(null, new SourceLocation(file, 0, 0), null);
    new Ast2Cfg(binding, file).buildFunction(func, body,
        Collections.<String> emptyList());
    return func;
  }

  private void buildFunction(Function func, ABody body,
      List<String> parameterNames) {
    func.getParameterNames().addAll(parameterNames);
    func.getDeclaredVariables().addAll(parameterNames);
    int varcounter = nextVar;
    nextVar = 1;
    functions.push(func);
    binding.setScope(body, func);
    scopes.push(func);
    exceptionHandlers.push(func.getExceptionalExit());
    jumpTargets.push(new FunctionBoundsJumpTarget());
    declarationBlocks.add(func.getEntry());
    for (String paramName : func.getParameterNames()) {
      func.getEntry().addLast(
          new DeclareVariable(paramName, DeclareVariable.Kind.PARAMETER));
    }
    Block afterDecls = new Block();
    func.addBlock(afterDecls);
    afterDecls.setExceptionHandler(func.getExceptionalExit());
    func.getEntry().addSuccessor(afterDecls);
    Block bodyExit = body.getBlock().apply(stmtvisitor, afterDecls);
    bodyExit.addLast(new ReturnVoid(true)); // insert implicit return
    declarationBlocks.pop();
    jumpTargets.pop();
    exceptionHandlers.pop();
    scopes.pop();
    functions.pop();
    nextVar = varcounter;

    if (func.getDeclaredVariables().add("arguments")) {
      func.getEntry().addLast(
          new DeclareVariable("arguments", DeclareVariable.Kind.ARGUMENTS));
      func.setHasExplicitArgumentsDeclaration(false);
    } else {
      func.setHasExplicitArgumentsDeclaration(true);
    }
  }

  private Function visitInnerFunction(ABody body, String name, Token token,
      Scope parentScope, List<String> parameterNames) {
    if (functionMap.containsKey(body)) {
      return functionMap.get(body);
    }
    Function f = new Function(name, new SourceLocation(file, token.getLine(),
        token.getPos()), parentScope);
    buildFunction(f, body, parameterNames);
    functions.peek().addInnerFunction(f);
    functionMap.put(body, f);
    return f;
  }

  public Ast2Cfg(IAstBinding binding, File file) {
    this.binding = binding;
    this.file = file;
  }

  private File file;
  private IAstBinding binding;
  private Stack<Function> functions = new Stack<Function>();
  private Stack<Block> declarationBlocks = new Stack<Block>();
  private Stack<Block> exceptionHandlers = new Stack<Block>();
  private Stack<Scope> scopes = new Stack<Scope>();
  private Map<ABody, Function> functionMap = new HashMap<ABody, Function>();
  private Map<ACatchClause, CatchScope> catchMap = new HashMap<ACatchClause, CatchScope>();
  private Map<AWithStmt, WithScope> withMap = new HashMap<AWithStmt, WithScope>();

  /**
   * Enclosing statements that affect jumping statements like break, continue,
   * and return. The innermost statements occur FIRST in the list (not last as
   * one might expect).
   */
  private LinkedList<JumpTarget> jumpTargets = new LinkedList<JumpTarget>();

  private <T extends Scope> T getTopmostScopeOfType(Class<T> type) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      Scope scope = scopes.get(i);
      if (type.isInstance(scope)) {
        return type.cast(scope);
      }
    }
    throw new RuntimeException("Must be in scope of type "
        + type.getSimpleName());
  }

  /**
   * Creates a new block with the given blocks as predecessors. The block's
   * exception handlers is set to the top of the exceptionHandlers stack.
   * 
   * @param predecessors
   *          zero or more blocks - may contain duplicates but not nulls
   * @return newly created block
   */
  private Block newBlock(Block... predecessors) {
    Function func = functions.peek();
    Block block = new Block();
    func.addBlock(block);
    block.setExceptionHandler(exceptionHandlers.peek());
    for (Block pred : predecessors) {
      pred.addSuccessor(block);
    }
    return block;
  }

  private static String identifier(Token token) {
    return Literals.parseIdentifier(token.getText());
  }

  private static final class BlockPair {
    Block before;
    Block after;

    public BlockPair(Block before, Block after) {
      this.before = before;
      this.after = after;
    }
  }

  private abstract static class JumpTarget {
  }

  private static class FinallyJumpTarget extends JumpTarget {
    List<BlockPair> pendingSources = new LinkedList<BlockPair>();
  }

  private static class LoopJumpTarget extends JumpTarget {
    Set<String> labelSet;
    Block continueTarget, breakTarget;

    public LoopJumpTarget(Set<String> labelSet, Block continueTarget,
        Block breakTarget) {
      this.labelSet = labelSet;
      this.continueTarget = continueTarget;
      this.breakTarget = breakTarget;
    }
  }

  private static class SwitchJumpTarget extends JumpTarget {
    Set<String> labelSet;
    Block breakTarget;

    public SwitchJumpTarget(Set<String> labelSet, Block breakTarget) {
      this.labelSet = labelSet;
      this.breakTarget = breakTarget;
    }
  }

  private static class BreakJumpTarget extends JumpTarget {
    String label;
    Block breakTarget;

    public BreakJumpTarget(String label, Block breakTarget) {
      this.label = label;
      this.breakTarget = breakTarget;
    }
  }

  private static class WithOrCatchBoundsJumpTarget extends JumpTarget {
  }

  private static class FunctionBoundsJumpTarget extends JumpTarget {
  }

  private void pushJumpTarget(JumpTarget target) {
    jumpTargets.addFirst(target);
  }

  private void popJumpTarget() {
    jumpTargets.removeFirst();
  }

  private static List<String> getParameterNames(List<Token> tokens) {
    List<String> names = new ArrayList<String>();
    for (Token tok : tokens) {
      names.add(identifier(tok));
    }
    return names;
  }

  private int nextVar = 1;

  private int newvar() {
    return nextVar++;
  }

  private void addDeclaration(DeclareVariable decl) {
    // TODO: make nondeterministic choice between same-name declarations (ECMA
    // mandates a choice, but browsers don't follow it)
    declarationBlocks.peek().addLast(decl);
  }

  private void addDeclaration(DeclareVariable decl, Statement... stmts) {
    declarationBlocks.peek().addLast(decl);
    for (Statement stm : stmts) {
      declarationBlocks.peek().addLast(stm);
    }
  }

  private StmtVisitor stmtvisitor = new StmtVisitor();

  private class StmtVisitor extends QuestionAnswerAdapter<Block, Block> {
    private Set<String> getLabelSet(PStmt stmt) {
      Node node = stmt;
      Set<String> result = new HashSet<String>();
      while (node.parent() instanceof ALabelledStmt) {
        ALabelledStmt lb = (ALabelledStmt) node.parent();
        result.add(identifier(lb.getLabel()));
        node = lb.parent();
      }
      return result;
    }

    private Block finishStmt(PStmt stmt, Block block) {
      if (block.isEmpty()) {
        block.addFirst(new Nop());
      }
      binding.addStmt(stmt, block.getLast());
      return block;
    }

    @Override
    public Block caseAIfStmt(AIfStmt node, Block block) {
      ConditionResult cond = node.getCondition().apply(conditionvisitor, block);
      Block trueExit = node.getThenBody().apply(this, cond.whenTrue);
      if (node.getElse() != null) {
        Block falseExit = node.getElseBody().apply(this, cond.whenFalse);
        return finishStmt(node, newBlock(trueExit, falseExit));
      } else {
        return finishStmt(node, newBlock(trueExit, cond.whenFalse));
      }
    }

    @Override
    public Block caseABlock(ABlock node, Block block) {
      for (PStmt stmt : node.getStatements()) {
        block = stmt.apply(this, block);
      }
      return block;
    }

    @Override
    public Block caseABlockStmt(ABlockStmt node, Block block) {
      return finishStmt(node, node.getBlock().apply(this, block));
    }

    @Override
    public Block caseAWhileStmt(AWhileStmt node, Block block) {
      Block conditionEntry = newBlock(block);
      Block loopExit = newBlock();
      ConditionResult cond = node.getCondition().apply(conditionvisitor,
          conditionEntry);
      cond.whenFalse.addSuccessor(loopExit);
      pushJumpTarget(new LoopJumpTarget(getLabelSet(node), conditionEntry,
          loopExit));
      Block bodyExit = node.getBody().apply(this, cond.whenTrue);
      popJumpTarget();
      bodyExit.addSuccessor(conditionEntry);
      return finishStmt(node, loopExit);
    }

    @Override
    public Block caseADoStmt(ADoStmt node, Block block) {
      Block bodyEntry = newBlock(block);
      Block conditionEntry = newBlock();
      Block loopExit = newBlock();
      pushJumpTarget(new LoopJumpTarget(getLabelSet(node), conditionEntry,
          loopExit));
      Block bodyExit = node.getBody().apply(this, bodyEntry);
      popJumpTarget();
      bodyExit.addSuccessor(conditionEntry);
      ConditionResult cond = node.getCondition().apply(conditionvisitor,
          conditionEntry);
      cond.whenTrue.addSuccessor(bodyEntry);
      cond.whenFalse.addSuccessor(loopExit);
      return finishStmt(node, loopExit);
    }

    @Override
    public Block caseAForStmt(AForStmt node, Block block) {
      Block initExit = node.getInit().apply(this, block);
      Block loopExit = newBlock();
      Block updateEntry = newBlock();
      Block conditionEntry;
      Block bodyEntry;
      if (node.getCondition() == null) {
        bodyEntry = conditionEntry = newBlock(initExit);
      } else {
        conditionEntry = newBlock(initExit);
        bodyEntry = newBlock();
        ConditionResult cond = node.getCondition().apply(conditionvisitor,
            conditionEntry);
        bodyEntry = cond.whenTrue;
        cond.whenFalse.addSuccessor(loopExit);
      }
      pushJumpTarget(new LoopJumpTarget(getLabelSet(node), updateEntry,
          loopExit));
      Block bodyExit = node.getBody().apply(this, bodyEntry);
      popJumpTarget();
      bodyExit.addSuccessor(updateEntry);
      Block updateExit;
      if (node.getUpdate() == null) {
        updateExit = updateEntry;
      } else {
        updateExit = node.getUpdate().apply(expvisitor, updateEntry).block;
      }
      updateExit.addSuccessor(conditionEntry);
      return finishStmt(node, loopExit);
    }

    @Override
    public Block caseAVarForInit(AVarForInit node, Block block) {
      for (AVarDecl decl : node.getVarDecl()) {
        block = decl.apply(this, block);
      }
      return block;
    }

    @Override
    public Block caseAExpForInit(AExpForInit node, Block block) {
      return node.getExp().apply(expvisitor, block).block;
    }

    @Override
    public Block caseAEmptyForInit(AEmptyForInit node, Block block) {
      return block;
    }

    @Override
    public Block caseAForInStmt(AForInStmt node, Block block) {
      LvalueResult lvalue = node.getLvalue().apply(lvaluevisitor, block);
      ExpResult obj = node.getExp().apply(expvisitor, lvalue.block);
      Block conditionEntry = newBlock(obj.block); // condition block will be
                                                  // empty
      Block conditionExit = conditionEntry;
      Block bodyEntry = newBlock(conditionExit);
      int prtyVar = newvar();
      GetNextProperty nextPrtyStm = new GetNextProperty(prtyVar, obj.resultVar);
      bodyEntry.addLast(nextPrtyStm);
      bodyEntry.addLast(lvalue.makeAssignment(prtyVar));
      Block loopExit = newBlock(obj.block);
      pushJumpTarget(new LoopJumpTarget(getLabelSet(node), conditionEntry,
          loopExit));
      Block bodyExit = node.getBody().apply(this, bodyEntry);
      popJumpTarget();
      bodyExit.addSuccessor(conditionEntry);
      conditionExit.addSuccessor(loopExit);
      binding.addGetNextProperty(node, nextPrtyStm);
      return finishStmt(node, loopExit);
    }

    @Override
    public Block caseABreakStmt(ABreakStmt node, Block block) {
      String label = node.getLabel() == null ? null : identifier(node
          .getLabel());
      Block b = block;
      boolean found = false;
      for (JumpTarget target : jumpTargets) {
        if (target instanceof FinallyJumpTarget) {
          FinallyJumpTarget f = (FinallyJumpTarget) target;
          Block exit = newBlock();
          f.pendingSources.add(new BlockPair(b, exit)); // execute finally
                                                        // block, then go to
                                                        // exit block
          b = exit;
        } else if (target instanceof LoopJumpTarget) {
          LoopJumpTarget loop = (LoopJumpTarget) target;
          if (label == null || loop.labelSet.contains(label)) {
            b.addSuccessor(loop.breakTarget);
            found = true;
            break;
          }
        } else if (target instanceof SwitchJumpTarget) {
          SwitchJumpTarget sw = (SwitchJumpTarget) target;
          if (label == null || sw.labelSet.contains(label)) {
            b.addSuccessor(sw.breakTarget);
            found = true;
            break;
          }
        } else if (target instanceof FunctionBoundsJumpTarget) {
          break; // function boundary
        } else if (target instanceof WithOrCatchBoundsJumpTarget) {
          b = newBlock(b);
          b.addFirst(new LeaveScope());
        } else if (target instanceof BreakJumpTarget) {
          BreakJumpTarget br = (BreakJumpTarget) target;
          if (label != null && label.equals(br.label)) {
            b.addSuccessor(br.breakTarget);
            found = true;
            break;
          }
        }
      }
      if (!found) {
        if (label != null) {
          throw new RuntimeException("Label not found: " + label);
        } else {
          throw new RuntimeException(
              "Break statement must be inside a switch or iteration statement");
        }
      }
      return newBlock();
    }

    @Override
    public Block caseAContinueStmt(AContinueStmt node, Block block) {
      String label = node.getLabel() == null ? null : identifier(node
          .getLabel());
      Block b = block;
      boolean found = false;
      for (JumpTarget target : jumpTargets) {
        if (target instanceof FinallyJumpTarget) {
          FinallyJumpTarget f = (FinallyJumpTarget) target;
          Block exit = newBlock();
          f.pendingSources.add(new BlockPair(b, exit));
          b = exit;
        } else if (target instanceof LoopJumpTarget) {
          LoopJumpTarget loop = (LoopJumpTarget) target;
          if (label == null || loop.labelSet.contains(label)) {
            b.addSuccessor(loop.continueTarget);
            found = true;
            break;
          }
        } else if (target instanceof FunctionBoundsJumpTarget) {
          break; // function boundary
        } else if (target instanceof WithOrCatchBoundsJumpTarget) {
          b = newBlock(b);
          b.addFirst(new LeaveScope());
        }
      }
      if (!found) {
        if (label != null) {
          throw new RuntimeException("Label not found: " + label);
        } else {
          throw new RuntimeException(
              "Continue statement must be inside an iteration statement");
        }
      }
      return newBlock();
    }

    @Override
    public Block caseADebuggerStmt(ADebuggerStmt node, Block block) {
      return finishStmt(node, block);
    }

    @Override
    public Block caseAEmptyStmt(AEmptyStmt node, Block block) {
      return finishStmt(node, block);
    }

    @Override
    public Block caseALabelledStmt(ALabelledStmt node, Block block) {
      switch (node.getStmt().kindPStmt()) {
      case FOR:
      case FOR_IN:
      case DO:
      case WHILE:
      case SWITCH:
        return finishStmt(node, node.getStmt().apply(this, block));
      default:
        Block exit = newBlock();
        pushJumpTarget(new BreakJumpTarget(identifier(node.getLabel()), exit));
        Block b = node.getStmt().apply(this, block);
        popJumpTarget();
        b.addSuccessor(exit);
        return finishStmt(node, exit);
      }
    }

    @Override
    public Block caseAFunctionDeclStmt(AFunctionDeclStmt node, Block block) {
      String name = identifier(node.getName());
      functions.peek().getDeclaredVariables().add(name);
      int funcvar = newvar();
      // function is instantiated at the top of the body
      // this also means that its enclosing scope is the function scope, and not
      // any enclosing with/catch scopes
      // (but it does inherit the with/catch scopes enclosing any of its
      // enclosing anonymous functions)
      Scope scope = getTopmostScopeOfType(Function.class);
      DeclareVariable decl = new DeclareVariable(name,
          DeclareVariable.Kind.FUNCTION);
      CreateFunction fun = new CreateFunction(funcvar, visitInnerFunction(
          node.getBody(), name, node.getFunction(), scope,
          getParameterNames(node.getParameters())), scope);
      WriteVariable write = new WriteVariable(name, funcvar, scope);
      addDeclaration(decl, fun, write);
      binding.addFunction(node, fun);
      // XXX: Need a way to remember where the function declaration originally
      // was. This
      // might be useful for bug-patterns, since the scoping rules might
      // surprise the programmer
      return finishStmt(node, block);
    }

    @Override
    public Block caseAExpStmt(AExpStmt node, Block block) {
      return finishStmt(node, node.getExp().apply(expvisitor, block).block);
    }

    @Override
    public Block caseAVarDeclStmt(AVarDeclStmt node, Block block) {
      Block b = block;
      for (AVarDecl decl : node.getDecls()) {
        b = decl.apply(this, b);
      }
      return finishStmt(node, b);
    }

    @Override
    public Block caseAVarDecl(AVarDecl node, Block block) {
      String name = identifier(node.getName());
      functions.peek().getDeclaredVariables().add(name);
      addDeclaration(new DeclareVariable(name, DeclareVariable.Kind.VAR));
      Block b = block;
      if (node.getInit() != null) {
        ExpResult init = node.getInit().apply(expvisitor, b);
        b = init.block;
        WriteVariable stm = new WriteVariable(name, init.resultVar,
            functions.peek());
        b.addLast(stm);
        binding.addVarDeclLvalue(node, stm);
      }
      return b;
    }

    @Override
    public Block caseAReturnStmt(AReturnStmt node, Block block) {
      if (node.getValue() == null) {
        Block b = returnJump(block);
        b = newBlock(b);
        ReturnVoid stm = new ReturnVoid(false);
        b.addLast(stm);
        binding.addReturnVoid(node, stm);
        return newBlock();
      } else {
        ExpResult e = node.getValue().apply(expvisitor, block);
        Block b = e.block;
        b = returnJump(b);
        b = newBlock(b); // cannot append to f.exit
        Return stm = new Return(e.resultVar);
        b.addLast(stm);
        binding.addReturn(node, stm);
        return newBlock();
      }
    }

    private Block returnJump(Block b) {
      for (JumpTarget target : jumpTargets) {
        if (target instanceof FinallyJumpTarget) {
          FinallyJumpTarget f = (FinallyJumpTarget) target;
          Block exit = newBlock();
          f.pendingSources.add(new BlockPair(b, exit));
          b = exit;
        } else if (target instanceof FunctionBoundsJumpTarget) {
          break;
        } else if (target instanceof WithOrCatchBoundsJumpTarget) {
          b = newBlock(b);
          b.addFirst(new LeaveScope());
        }
      }
      return b;
    }

    @Override
    public Block caseAThrowStmt(AThrowStmt node, Block block) {
      ExpResult e = node.getValue().apply(expvisitor, block);
      Throw stm = new Throw(e.resultVar);
      e.block.addLast(stm);
      binding.addThrow(node, stm);
      return newBlock();
    }

    @Override
    public Block caseATryStmt(ATryStmt node, Block block) {
      // we handle the three cases separately
      // the code becomes too tricky if we handle them together
      if (node.getCatchClause() != null && node.getFinallyClause() == null) {
        // catch, no finally
        Block catchEntry = newBlock();
        exceptionHandlers.push(catchEntry);
        Block bodyExit = node.getBody().apply(this, newBlock(block));
        exceptionHandlers.pop();
        Block catchExit = node.getCatchClause().apply(this, catchEntry);
        return finishStmt(node, newBlock(bodyExit, catchExit));
      } else if (node.getCatchClause() == null
          && node.getFinallyClause() != null) {
        // no catch, finally
        int finallyException = newvar();
        Block finallyExceptionalEntry = newBlock();
        finallyExceptionalEntry.addFirst(new Catch(finallyException));
        Block finallyNormalEntry = newBlock();
        Block finallyAbruptExit = newBlock(); // abrupt exit from finally due to
                                              // return/break/continue in try
                                              // block
        FinallyJumpTarget target = new FinallyJumpTarget();
        exceptionHandlers.push(finallyExceptionalEntry);
        Block bodyExit = node.getBody().apply(this, newBlock(block));
        exceptionHandlers.pop();
        bodyExit.addSuccessor(finallyNormalEntry);
        Block finallyNormalExit = node.getFinallyClause().getBody()
            .apply(this, finallyNormalEntry);
        finallyNormalExit.addSuccessor(finallyAbruptExit);
        Block finallyExceptionalExit = node.getFinallyClause().getBody()
            .apply(this, finallyExceptionalEntry);
        finallyExceptionalExit.addLast(new Throw(finallyException));
        // handle break,continue,return statements in the try body
        for (BlockPair bp : target.pendingSources) {
          Block b = node.getFinallyClause().getBody().apply(this, bp.before);
          b.addSuccessor(bp.after);
        }
        return finishStmt(node, newBlock(finallyNormalExit));
      } else if (node.getCatchClause() != null
          && node.getFinallyClause() != null) {
        // catch and finally
        int finallyException = newvar();
        Block finallyExceptionalEntry = newBlock();
        finallyExceptionalEntry.addFirst(new Catch(finallyException));
        Block finallyNormalEntry = newBlock();
        Block finallyAbruptExit = newBlock(); // abrupt exit from finally due to
                                              // return/break/continue in try
                                              // block
        Block catchEntry = newBlock();
        catchEntry.setExceptionHandler(finallyExceptionalEntry);
        FinallyJumpTarget target = new FinallyJumpTarget();
        pushJumpTarget(target);
        exceptionHandlers.push(catchEntry);
        Block bodyExit = node.getBody().apply(this, newBlock(block));
        exceptionHandlers.pop();
        exceptionHandlers.push(finallyExceptionalEntry);
        pushJumpTarget(new WithOrCatchBoundsJumpTarget());
        Block catchExit = node.getCatchClause().apply(this, catchEntry);
        popJumpTarget();
        exceptionHandlers.pop();
        popJumpTarget();
        bodyExit.addSuccessor(finallyNormalEntry);
        catchExit.addSuccessor(finallyNormalEntry);
        Block finallyNormalExit = node.getFinallyClause().getBody()
            .apply(this, finallyNormalEntry);
        finallyNormalExit.addSuccessor(finallyAbruptExit);
        Block finallyExceptionalExit = node.getFinallyClause().getBody()
            .apply(this, finallyExceptionalEntry);
        finallyExceptionalExit.addLast(new Throw(finallyException));
        for (BlockPair bp : target.pendingSources) {
          Block b = node.getFinallyClause().getBody().apply(this, bp.before);
          b.addSuccessor(bp.after);
        }
        return finishStmt(node, newBlock(finallyNormalExit));
      } else {
        // parser does not allow try without catch or finally
        throw new RuntimeException("Try statement without catch or finally");
      }
    }

    private Block createScopeExceptionBlock() {
      Block exceptionalBlock = newBlock();
      int exceptionVar = newvar();
      exceptionalBlock.addLast(new Catch(exceptionVar));
      exceptionalBlock.addLast(new LeaveScope());
      exceptionalBlock.addLast(new Throw(exceptionVar));
      return exceptionalBlock;
    }

    @Override
    public Block caseACatchClause(ACatchClause node, Block block) {
      String name = identifier(node.getName());
      int exception = newvar();
      Catch ct = new Catch(exception);
      block.addLast(ct);
      CatchScope scope = catchMap.get(node);
      if (scope == null) {
        scope = new CatchScope(scopes.peek(), name);
        catchMap.put(node, scope);
        binding.setScope(node, scope);
      }
      block.addLast(new EnterCatch(scope, name));
      Block exceptionalBlock = createScopeExceptionBlock();
      exceptionHandlers.push(exceptionalBlock);
      scopes.push(scope);
      pushJumpTarget(new WithOrCatchBoundsJumpTarget());
      Block b = newBlock(block);
      WriteVariable write = new WriteVariable(name, exception, scope);
      b.addLast(write);
      b = node.getBody().apply(this, b);
      popJumpTarget();
      scopes.pop();
      exceptionHandlers.pop();
      binding.addCatch(node, ct, write);
      b.addLast(new LeaveScope());
      return b;
    }

    @Override
    public Block caseASwitchStmt(ASwitchStmt node, Block block) {
      ExpResult e = node.getExp().apply(expvisitor, block);
      Block executionBranch = newBlock();
      Block jumpingBranch = e.block;
      Block switchExit = newBlock();
      pushJumpTarget(new SwitchJumpTarget(getLabelSet(node), switchExit));
      for (PSwitchClause clause : node.getClauses()) {
        if (clause instanceof ACaseSwitchClause) {
          ACaseSwitchClause caseclause = (ACaseSwitchClause) clause;
          ExpResult expr = caseclause.getExp().apply(expvisitor, jumpingBranch);
          int compareVar = newvar();
          expr.block.addLast(new BinaryOperation(compareVar,
              BinaryOperation.Operator.STRICT_EQUAL, e.resultVar,
              expr.resultVar));
          Block equalBranch = newBlock(expr.block);
          equalBranch.addFirst(new Assertion(compareVar, true));
          Block execEntry = newBlock(executionBranch, equalBranch);
          jumpingBranch = newBlock(expr.block);
          jumpingBranch.addFirst(new Assertion(compareVar, false));
          executionBranch = caseclause.getBlock().apply(this, execEntry);
        } else {
          // default clause
          Block b = newBlock(jumpingBranch, executionBranch);
          b = clause.getBlock().apply(this, b);
          executionBranch = b;
          jumpingBranch = newBlock();
        }
      }
      popJumpTarget();
      executionBranch.addSuccessor(switchExit);
      jumpingBranch.addSuccessor(switchExit);
      return finishStmt(node, switchExit);
    }

    @Override
    public Block caseAWithStmt(AWithStmt node, Block block) {
      Block exceptionalBlock = createScopeExceptionBlock();
      ExpResult e = node.getExp().apply(expvisitor, block);
      EnterWith enter = new EnterWith(null, e.resultVar);
      WithScope scope = withMap.get(node);
      if (scope == null) {
        scope = new WithScope(scopes.peek(), enter);
        withMap.put(node, scope);
        binding.setScope(node, scope);
      }
      enter.setInnerScope(scope); // XXX fix this circular hack
      Block bodyEntry = e.block;
      bodyEntry.addLast(enter);
      scopes.push(scope);
      pushJumpTarget(new WithOrCatchBoundsJumpTarget());
      exceptionHandlers.push(exceptionalBlock);
      Block bodyExit = node.getBody().apply(this, newBlock(bodyEntry));
      exceptionHandlers.pop();
      popJumpTarget();
      scopes.pop();
      Block withExit = newBlock(bodyExit);
      LeaveScope leave = new LeaveScope();
      withExit.addLast(leave);
      binding.addWith(node, enter, leave, exceptionalBlock);
      return finishStmt(node, withExit);
    }

    @Override
    public Block defaultNode(Node node, Block block) {
      throw new RuntimeException("Unexpected node in stmt visitor: "
          + node.getClass());
    }
  }

  private static class ExpResult {
    Block block;
    int resultVar;

    public ExpResult(Block block, int resultVar) {
      this.block = block;
      this.resultVar = resultVar;
    }
  }

  private ExpVisitor expvisitor = new ExpVisitor();

  private class ExpVisitor extends QuestionAnswerAdapter<Block, ExpResult> {
    @Override
    public ExpResult defaultNode(Node node, Block question) {
      throw new RuntimeException(
          "Unexpected node applied to expression visitor: " + node.getClass());
    }

    @Override
    public ExpResult caseAAssignExp(AAssignExp node, Block block) {
      LvalueResult lvalue = node.getLeft().apply(lvaluevisitor, block);
      ExpResult right = node.getRight().apply(this, lvalue.block);
      Block b = right.block;
      WriteStatement write;
      switch (node.getOp().kindPAssignOp()) {
      case NORMAL:
        write = lvalue.makeAssignment(right.resultVar);
        b.addLast(write);
        binding.addAssignExp(node, write);
        binding.addExp(node, right.resultVar, write);
        return new ExpResult(b, right.resultVar);
      default:
        BinaryOperation.Operator op = BinaryOperation.fromAssignOp(node.getOp()
            .kindPAssignOp());
        int oldValueVar = newvar();
        b.addLast(lvalue.makeRead(oldValueVar));
        int newValueVar = newvar();
        b.addLast(new BinaryOperation(newValueVar, op, oldValueVar,
            right.resultVar));
        write = lvalue.makeAssignment(newValueVar);
        b.addLast(write);
        binding.addAssignExp(node, write);
        binding.addExp(node, newValueVar, write);
        return new ExpResult(b, newValueVar);
      }
    }

    @Override
    public ExpResult caseACommaExp(ACommaExp node, Block block) {
      ExpResult first = node.getFirstExp().apply(this, block);
      ExpResult second = node.getSecondExp().apply(this, first.block);
      if (second.block.isEmpty()) {
        second.block.addFirst(new Nop());
      }
      binding.addExp(node, second.resultVar, second.block.getLast());
      return second;
    }

    @Override
    public ExpResult caseABinopExp(ABinopExp node, Block block) {
      switch (node.getOp().kindPBinop()) {
      case LOGICAL_AND: {
        // E1 && E2
        ConditionValueResult cond = node.getLeft().apply(falsevisitor, block);
        ExpResult right = node.getRight().apply(this, cond.whenTrue);
        int result = newvar();
        Block b = newBlock(cond.whenFalse, right.block);
        Phi phi = new Phi(result, cond.resultVar, right.resultVar);
        b.addFirst(phi);
        binding.addLogicalExp(node, phi);
        binding.addExp(node, result, phi);
        return new ExpResult(b, result);
      }
      case LOGICAL_OR: {
        // E1 || E2
        ConditionValueResult cond = node.getLeft().apply(truevisitor, block);
        ExpResult right = node.getRight().apply(this, cond.whenFalse);
        int result = newvar();
        Block b = newBlock(cond.whenTrue, right.block);
        Phi phi = new Phi(result, cond.resultVar, right.resultVar);
        b.addFirst(phi);
        binding.addLogicalExp(node, phi);
        binding.addExp(node, result, phi);
        return new ExpResult(b, result);
      }
      default:
        ExpResult left = node.getLeft().apply(this, block);
        ExpResult right = node.getRight().apply(this, left.block);
        int resultVar = newvar();
        BinaryOperation stm = new BinaryOperation(resultVar,
            BinaryOperation.fromBinop(node.getOp().kindPBinop()),
            left.resultVar, right.resultVar);
        Block b = right.block;
        b.addLast(stm);
        binding.addBinopExp(node, stm);
        binding.addExp(node, resultVar, stm);
        return new ExpResult(b, resultVar);
      }
    }

    @Override
    public ExpResult caseAEmptyExp(AEmptyExp node, Block block) {
      // occurs only in array literals
      throw new RuntimeException("Empty expression must not occur here");
    }

    @Override
    public ExpResult caseAArrayLiteralExp(AArrayLiteralExp node, Block block) {
      Block b = block;
      List<Integer> expVars = new ArrayList<Integer>();
      // evaluate all subexpressions before creating the array object
      for (PExp item : node.getValues()) {
        if (item instanceof AEmptyExp) {
          expVars.add(-1);
        } else {
          ExpResult r = item.apply(this, b);
          expVars.add(r.resultVar);
          b = r.block;
        }
      }
      int arrayVar = newvar();
      NewArray stm = new NewArray(arrayVar, node.getValues().size());
      b.addLast(stm);
      for (int i = 0; i < expVars.size(); i++) {
        if (expVars.get(i) == -1) {
          continue;
        }
        int tmp = newvar();
        b.addLast(new NumberConst(tmp, i));
        b.addLast(new WriteProperty(arrayVar, tmp, expVars.get(i)));
      }
      binding.addArrayExp(node, stm);
      binding.addExp(node, arrayVar, b.getLast());
      return new ExpResult(b, arrayVar);
    }

    @Override
    public ExpResult caseAConditionalExp(AConditionalExp node, Block block) {
      ConditionResult cond = node.getCondition().apply(conditionvisitor, block);
      ExpResult trueEval = node.getTrueExp().apply(this, cond.whenTrue);
      ExpResult falseEval = node.getFalseExp().apply(this, cond.whenFalse);
      Block exit = newBlock(trueEval.block, falseEval.block);
      int result = newvar();
      Phi phi = new Phi(result, trueEval.resultVar, falseEval.resultVar);
      exit.addLast(phi);
      binding.addConditionalExp(node, phi);
      binding.addExp(node, result, phi);
      return new ExpResult(exit, result);
    }

    @Override
    public ExpResult caseAConstExp(AConstExp node, Block block) {
      int resultVar = newvar();
      final Assignment asn;
      switch (node.getConst().kindPConst()) {
      case BOOLEAN: {
        ABooleanConst ct = (ABooleanConst) node.getConst();
        block.addLast(asn = new BooleanConst(resultVar,
            ct.getBool() instanceof ATrueBool));
        break;
      }
      case NULL:
        block.addLast(asn = new NullConst(resultVar));
        break;
      case NUMBER: {
        ANumberConst ct = (ANumberConst) node.getConst();
        block.addLast(asn = new NumberConst(resultVar, Literals
            .parseNumberLiteral(ct.getNumberLiteral().getText())));
        break;
      }
      case STRING: {
        AStringConst ct = (AStringConst) node.getConst();
        block.addLast(asn = new StringConst(resultVar, Literals
            .parseStringLiteral(ct.getStringLiteral().getText(),
                ErrorTolerance.COMPENSATE)));
        break;
      }
      default:
        throw new RuntimeException("Unknown constant kind: "
            + node.getConst().getClass());
      }
      binding.addConstExp(node, asn);
      binding.addExp(node, resultVar, asn);
      return new ExpResult(block, resultVar);
    }

    @Override
    public ExpResult caseAThisExp(AThisExp node, Block block) {
      int result = newvar();
      ReadThis stm = new ReadThis(result);
      block.addLast(stm);
      binding.addThisExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(block, result);
    }

    @Override
    public ExpResult caseARegexpExp(ARegexpExp node, Block block) {
      int result = newvar();
      NewRegexp stm = new NewRegexp(result, node.getRegexpLiteral().getText());
      block.addLast(stm);
      binding.addRegexpExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(block, result);
    }

    @Override
    public ExpResult caseAFunctionExp(AFunctionExp node, Block block) {
      int result = newvar();
      // note the distinction between function expressions and function
      // declarations:
      // function declarations get the scope they are used in (including
      // with/catch) but function declarations
      // are moved to the top of the enclosing function body, and get the
      // appropriate scope from there
      String name = node.getName() == null ? null : identifier(node.getName());
      Function inner = visitInnerFunction(node.getBody(), name,
          node.getFunction(), scopes.peek(),
          getParameterNames(node.getParameters()));
      if (name != null) {
        inner.getEntry().addFirst(
            new DeclareVariable(name, DeclareVariable.Kind.SELF));
        inner.getDeclaredVariables().add(name);
      }
      CreateFunction stm = new CreateFunction(result, inner, scopes.peek());
      block.addLast(stm);
      binding.addFunctionExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(block, result);
    }

    @Override
    public ExpResult caseAObjectLiteralExp(AObjectLiteralExp node, Block block) {
      int obj = newvar();
      Block b = block;
      NewObject stm = new NewObject(obj);
      b.addLast(stm);
      for (PObjectLiteralProperty prty : node.getProperties()) {
        switch (prty.kindPObjectLiteralProperty()) {
        case NORMAL:
          ANormalObjectLiteralProperty nprty = (ANormalObjectLiteralProperty) prty;
          ExpResult value = nprty.getValue().apply(this, b);
          b = value.block;
          final int propertyVar = newvar();
          Statement propertyStm = nprty.getName().apply(
              new AnswerAdapter<Statement>() {
                @Override
                public Statement caseAIdentifierPropertyName(
                    AIdentifierPropertyName node) {
                  return new StringConst(propertyVar,
                      identifier(node.getName()));
                }

                @Override
                public Statement caseAStringPropertyName(
                    AStringPropertyName node) {
                  return new StringConst(propertyVar, Literals
                      .parseStringLiteral(node.getStringLiteral().getText(),
                          ErrorTolerance.COMPENSATE));
                }

                @Override
                public Statement caseANumberPropertyName(
                    ANumberPropertyName node) {
                  return new NumberConst(propertyVar, Literals
                      .parseNumberLiteral(node.getNumberLiteral().getText()));
                }
              });
          b.addLast(propertyStm);
          WriteProperty write = new WriteProperty(obj, propertyVar,
              value.resultVar);
          b.addLast(write);
          binding.addObjectLiteralProperty(nprty, write);
          break;
        case GET:
        case SET:
          // TODO property accessors
          throw new RuntimeException("Property accessors are not supported");
        }
      }
      binding.addObjectLiteralExp(node, stm);
      binding.addExp(node, obj, b.getLast());
      return new ExpResult(b, obj);
    }

    @Override
    public ExpResult caseAParenthesisExp(AParenthesisExp node, Block block) {
      ExpResult r = node.getExp().apply(this, block);
      if (r.block.isEmpty()) {
        r.block.addFirst(new Nop());
      }
      binding.addExp(node, r.resultVar, r.block.getLast());
      return r;
    }

    @Override
    public ExpResult caseANewExp(ANewExp node, Block block) {
      int result = newvar();
      ExpResult func = node.getFunctionExp().apply(this, block);
      CallConstructor stm = new CallConstructor(result, func.resultVar);
      Block b = func.block;
      for (PExp arg : node.getArguments()) {
        ExpResult r = arg.apply(this, b);
        stm.getArguments().add(r.resultVar);
        b = r.block;
      }
      b.addLast(stm);
      binding.addNewExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(b, result);
    }

    @Override
    public ExpResult caseAInvokeExp(final AInvokeExp invoke, final Block block) {
      final int result = newvar();
      abstract class InvokeTmp {
        InvokeStatement statement;
        Block block;

        public InvokeTmp(InvokeStatement statement, Block block) {
          this.statement = statement;
          this.block = block;
        }

        public abstract void addBinding();
      }
      InvokeTmp func = invoke.getFunctionExp().apply(
          new AnswerAdapter<InvokeTmp>() {
            // evaluate function expression and create the appropriate call
            // statement,
            // but do not add the call statement to a block
            @Override
            public InvokeTmp caseAParenthesisExp(AParenthesisExp node) {
              return node.getExp().apply(this);
            }

            @Override
            public InvokeTmp caseANameExp(final ANameExp node) {
              final CallVariable stm = new CallVariable(result, identifier(node
                  .getName()), scopes.peek());
              // special case: no call to binding.addExp
              return new InvokeTmp(stm, block) {
                public void addBinding() {
                  binding.addCallVariable(invoke, node, stm);
                }
              };
            }

            @Override
            public InvokeTmp caseAPropertyExp(final APropertyExp node) {
              ExpResult base = node.getBase().apply(expvisitor, block);
              StringConst cst = new StringConst(newvar(), identifier(node
                  .getName()));
              base.block.addLast(cst);
              final CallProperty stm = new CallProperty(result, base.resultVar,
                  cst.getResultVar());
              // special case: no call to binding.addExp
              return new InvokeTmp(stm, base.block) {
                public void addBinding() {
                  binding.addCallProperty(invoke, node, stm);
                }
              };
            }

            @Override
            public InvokeTmp caseADynamicPropertyExp(
                final ADynamicPropertyExp node) {
              ExpResult base = node.getBase().apply(expvisitor, block);
              ExpResult prty = node.getPropertyExp().apply(expvisitor,
                  base.block);
              final CallProperty stm = new CallProperty(result, base.resultVar,
                  prty.resultVar);
              // special case: no call to binding.addExp
              return new InvokeTmp(stm, prty.block) {
                public void addBinding() {
                  binding.addCallProperty(invoke, node, stm);
                }
              };
            }

            @Override
            public InvokeTmp defaultPExp(final PExp node) {
              ExpResult func = node.apply(expvisitor, block);
              final Call stm = new Call(result, func.resultVar);
              // special case: no call to binding.addExp
              return new InvokeTmp(stm, func.block) {
                public void addBinding() {
                  binding.addCall(invoke, stm);
                }
              };
            }
          });
      Block b = func.block;
      for (PExp arg : invoke.getArguments()) {
        ExpResult r = arg.apply(this, b);
        func.statement.getArguments().add(r.resultVar);
        b = r.block;
      }
      b.addLast(func.statement);
      func.addBinding(); // must only add binding AFTER statement has been added
                         // to the block
      binding.addExp(invoke, result, func.statement);
      return new ExpResult(b, result);
    }

    @Override
    public ExpResult caseAPrefixUnopExp(final APrefixUnopExp node,
        final Block block) {
      UnaryOperation stm;
      switch (node.getOp().kindPPrefixUnop()) {
      case INCREMENT:
      case DECREMENT: {
        LvalueResult lvalue = node.getExp().apply(lvaluevisitor, block);
        int beforeIncrementVar = newvar();
        int afterIncrementVar = newvar();
        Block b = lvalue.block;
        b.addLast(lvalue.makeRead(beforeIncrementVar));
        b.addLast(stm = new UnaryOperation(afterIncrementVar, UnaryOperation
            .fromPrefixUnop(node.getOp().kindPPrefixUnop()), beforeIncrementVar));
        b.addLast(lvalue.makeAssignment(afterIncrementVar));
        binding.addPrefixUnopExp(node, stm);
        binding.addExp(node, afterIncrementVar, b.getLast());
        return new ExpResult(b, afterIncrementVar);
      }
      case PLUS:
      case MINUS:
      case COMPLEMENT:
      case NOT:
      case TYPEOF:
      case VOID: {
        ExpResult exp = node.getExp().apply(this, block);
        int result = newvar();
        Block b = exp.block;
        b.addLast(stm = new UnaryOperation(result, UnaryOperation
            .fromPrefixUnop(node.getOp().kindPPrefixUnop()), exp.resultVar));
        binding.addPrefixUnopExp(node, stm);
        binding.addExp(node, result, b.getLast());
        return new ExpResult(b, result);
      }
      case DELETE: {
        final int result = newvar();
        Block b = node.getExp().apply(new AnswerAdapter<Block>() {
          @Override
          public Block caseAParenthesisExp(AParenthesisExp exp) {
            // special case: no call to binding.addExp
            return exp.getExp().apply(this);
          }

          @Override
          public Block caseAPropertyExp(APropertyExp exp) {
            ExpResult base = exp.getBase().apply(expvisitor, block);
            Block b = base.block;
            int prtyvar = newvar();
            StringConst ct = new StringConst(prtyvar, identifier(exp.getName()));
            b.addLast(ct);
            DeleteProperty stm = new DeleteProperty(result, base.resultVar,
                prtyvar);
            b.addLast(stm);
            binding.addDeleteProperty(node, exp, stm);
            // special case: no call to binding.addExp
            return b;
          }

          @Override
          public Block caseADynamicPropertyExp(ADynamicPropertyExp exp) {
            ExpResult base = exp.getBase().apply(expvisitor, block);
            ExpResult prty = exp.getPropertyExp().apply(expvisitor, base.block);
            DeleteProperty stm = new DeleteProperty(result, base.resultVar,
                prty.resultVar);
            Block b = prty.block;
            b.addLast(stm);
            binding.addDeleteDynamicProperty(node, exp, stm);
            // special case: no call to binding.addExp
            return b;
          }

          @Override
          public Block defaultPExp(PExp exp) {
            // type error in strict mode
            // returns true in non-strict mode
            BooleanConst stm = new BooleanConst(result, true);
            block.addLast(stm);
            binding.addInvalidDelete(node, stm);
            // special case: no call to binding.addExp
            return block;
          }
        });
        binding.addExp(node, result, b.getLast());
        return new ExpResult(b, result);
      }
      default:
        throw new RuntimeException("Unknown prefix unop: " + node.getOp());
      }
    }

    @Override
    public ExpResult caseAPostfixUnopExp(APostfixUnopExp node, Block block) {
      switch (node.getOp().kindPPostfixUnop()) {
      case DECREMENT:
      case INCREMENT:
        LvalueResult lvalue = node.getExp().apply(lvaluevisitor, block);
        int beforeIncrementVar = newvar();
        int afterIncrementVar = newvar();
        Block b = lvalue.block;
        b.addLast(lvalue.makeRead(beforeIncrementVar));
        UnaryOperation.Operator op = node.getOp().kindPPostfixUnop() == EPostfixUnop.INCREMENT ? UnaryOperation.Operator.INCREMENT
            : UnaryOperation.Operator.DECREMENT;
        UnaryOperation stm = new UnaryOperation(afterIncrementVar, op,
            beforeIncrementVar);
        b.addLast(stm);
        b.addLast(lvalue.makeAssignment(afterIncrementVar));
        binding.addPostfixUnopExp(node, stm);
        binding.addExp(node, beforeIncrementVar, b.getLast());
        return new ExpResult(b, beforeIncrementVar);
      default:
        throw new RuntimeException("Unknown postfix unop: " + node.getOp());
      }
    }

    @Override
    public ExpResult caseANameExp(ANameExp node, Block block) {
      int result = newvar();
      ReadVariable stm = new ReadVariable(result, identifier(node.getName()),
          scopes.peek());
      Block b = block;
      b.addLast(stm);
      binding.addNameExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(b, result);
    }

    @Override
    public ExpResult caseAPropertyExp(APropertyExp node, Block block) {
      ExpResult base = node.getBase().apply(this, block);
      Block b = base.block;
      int propertyVar = newvar();
      StringConst propertyStm = new StringConst(propertyVar,
          identifier(node.getName()));
      b.addLast(propertyStm);
      int result = newvar();
      ReadProperty stm = new ReadProperty(result, base.resultVar, propertyVar);
      b.addLast(stm);
      binding.addPropertyExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(b, result);
    }

    @Override
    public ExpResult caseADynamicPropertyExp(ADynamicPropertyExp node,
        Block block) {
      ExpResult base = node.getBase().apply(this, block);
      ExpResult prty = node.getPropertyExp().apply(this, base.block);
      int result = newvar();
      ReadProperty stm = new ReadProperty(result, base.resultVar,
          prty.resultVar);
      Block b = prty.block;
      b.addLast(stm);
      binding.addPropertyExp(node, stm);
      binding.addExp(node, result, stm);
      return new ExpResult(b, result);
    }
  }

  private static abstract class LvalueResult {
    Block block;

    public abstract WriteStatement makeAssignment(int valueVar);

    public abstract Statement makeRead(int resultVar);

    public LvalueResult(Block block) {
      this.block = block;
    }
  }

  private class VarLvalue extends LvalueResult {
    String varName;
    ANameExp exp;
    Scope scope;

    public VarLvalue(Block block, String varName, ANameExp exp, Scope scope) {
      super(block);
      this.varName = varName;
      this.exp = exp;
      this.scope = scope;
    }

    @Override
    public WriteVariable makeAssignment(int valueVar) {
      WriteVariable stm = new WriteVariable(varName, valueVar, scope);
      binding.addNameLvalue(exp, stm);
      return stm;
    }

    @Override
    public Statement makeRead(int resultVar) {
      ReadVariable stm = new ReadVariable(resultVar, varName, scope);
      binding.addNameExp(exp, stm);
      return stm;
    }
  }

  private class VarDeclLvalue extends LvalueResult {
    String varName;
    AVarDecl decl;
    Scope scope;

    public VarDeclLvalue(Block block, String varName, AVarDecl decl, Scope scope) {
      super(block);
      this.varName = varName;
      this.decl = decl;
      this.scope = scope;
    }

    @Override
    public WriteVariable makeAssignment(int valueVar) {
      WriteVariable stm = new WriteVariable(varName, valueVar, scope);
      binding.addVarDeclLvalue(decl, stm);
      return stm;
    }

    @Override
    public Statement makeRead(int resultVar) {
      throw new RuntimeException("Cannot read from variable declaration");
      // ReadVariable stm = new ReadVariable(resultVar, newvar(), varName);
      // binding.addNameExp(exp, stm);
      // return stm;
    }
  }

  private class DynamicPropertyLvalue extends LvalueResult {
    int baseVar;
    int propertyVar;
    IPropertyAccessNode exp;

    public DynamicPropertyLvalue(Block block, int baseVar, int propertyVar,
        IPropertyAccessNode exp) {
      super(block);
      this.baseVar = baseVar;
      this.propertyVar = propertyVar;
      this.exp = exp;
    }

    @Override
    public WriteProperty makeAssignment(int valueVar) {
      WriteProperty stm = new WriteProperty(baseVar, propertyVar, valueVar);
      binding.addPropertyLvalue(exp, stm);
      return stm;
    }

    @Override
    public Statement makeRead(int resultVar) {
      ReadProperty stm = new ReadProperty(resultVar, baseVar, propertyVar);
      binding.addPropertyExp(exp, stm);
      return stm;
    }
  }

  private LvalueVisitor lvaluevisitor = new LvalueVisitor();

  private class LvalueVisitor extends
      QuestionAnswerAdapter<Block, LvalueResult> {
    @Override
    public LvalueResult caseANameExp(ANameExp node, Block block) {
      return new VarLvalue(block, identifier(node.getName()), node,
          scopes.peek());
    }

    @Override
    public LvalueResult caseAPropertyExp(APropertyExp node, Block block) {
      ExpResult base = node.getBase().apply(expvisitor, block);
      int propertyVar = newvar();
      StringConst propertyStm = new StringConst(propertyVar,
          identifier(node.getName()));
      base.block.addLast(propertyStm);
      return new DynamicPropertyLvalue(base.block, base.resultVar, propertyVar,
          node);
    }

    @Override
    public LvalueResult caseADynamicPropertyExp(ADynamicPropertyExp node,
        Block block) {
      ExpResult base = node.getBase().apply(expvisitor, block);
      ExpResult prty = node.getPropertyExp().apply(expvisitor, base.block);
      return new DynamicPropertyLvalue(prty.block, base.resultVar,
          prty.resultVar, node);
    }

    @Override
    public LvalueResult caseAParenthesisExp(AParenthesisExp node, Block block) {
      return node.getExp().apply(this, block);
    }

    @Override
    public LvalueResult defaultPExp(PExp node, Block block) {
      throw new IllegalArgumentException("Not a valid left-hand value: " + node);
    }

    @Override
    public LvalueResult caseALvalueForInLvalue(ALvalueForInLvalue node,
        Block block) {
      return node.getExp().apply(this, block);
    }

    @Override
    public LvalueResult caseAVarForInLvalue(AVarForInLvalue node, Block block) {
      String name = identifier(node.getVarDecl().getName());
      functions.peek().getDeclaredVariables().add(name);
      addDeclaration(new DeclareVariable(name, DeclareVariable.Kind.VAR));
      if (node.getVarDecl().getInit() != null) {
        // The parser does not accept an initializer here.
        // TODO: Refactor AST to make this type-safe?
        throw new RuntimeException("For-in lvalues cannot have initializers");
      }
      return new VarDeclLvalue(block, name, node.getVarDecl(), functions.peek());
    }

    @Override
    public LvalueResult defaultNode(Node node, Block block) {
      throw new IllegalArgumentException(
          "LvalueVisitor applied to non-expression: " + node.getClass());
    }
  }

  // --- Condition visitor ---
  // Applied to an expression E
  // Standing at the end of 'start' block, evaluate E and go to 'whenTrue' if
  // ToBoolean(E) is true, else go to 'whenFalse'
  // This gives a better graph than we would get by applying ExpVisitor and then
  // add two outgoing edges
  private static final class ConditionResult {
    Block whenTrue, whenFalse;

    public ConditionResult(Block whenTrue, Block whenFalse) {
      this.whenTrue = whenTrue;
      this.whenFalse = whenFalse;
    }
  }

  private ConditionVisitor conditionvisitor = new ConditionVisitor();

  private class ConditionVisitor extends
      QuestionAnswerAdapter<Block, ConditionResult> {
    // Note: The expressions we handle here only fail if their subexpressions
    // fail,
    // and only have the side-effects of their subexpressions. It is not sound
    // to include other expressions.
    // In particular, ToBoolean(v) NEVER throws an exception or has
    // side-effects!
    @Override
    public ConditionResult caseABinopExp(ABinopExp node, Block block) {
      switch (node.getOp().kindPBinop()) {
      case LOGICAL_AND: {
        ConditionResult left = node.getLeft().apply(this, block);
        ConditionResult right = node.getRight().apply(this, left.whenTrue);
        return new ConditionResult(right.whenTrue, newBlock(left.whenFalse,
            right.whenFalse));
      }
      case LOGICAL_OR: {
        ConditionResult left = node.getLeft().apply(this, block);
        ConditionResult right = node.getRight().apply(this, left.whenFalse);
        return new ConditionResult(newBlock(left.whenTrue, right.whenTrue),
            right.whenFalse);
      }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionResult caseAPrefixUnopExp(APrefixUnopExp node, Block block) {
      switch (node.getOp().kindPPrefixUnop()) {
      case NOT:
        // swap destinations
        ConditionResult r = node.getExp().apply(this, block);
        return new ConditionResult(r.whenFalse, r.whenTrue);
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionResult caseAConstExp(AConstExp node, Block block) {
      switch (node.getConst().kindPConst()) {
      case BOOLEAN:
        ABooleanConst bc = (ABooleanConst) node.getConst();
        if (bc.getBool() instanceof ATrueBool) {
          return new ConditionResult(block, newBlock());
        } else {
          return new ConditionResult(newBlock(), block);
        }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionResult caseAConditionalExp(AConditionalExp node, Block block) {
      // This case is quite uncommon. It will occur in nested conditionals,
      // like:
      // if (A ? B : C) { .. } else { .. }
      // (x ? y : z) ? A : B
      ConditionResult cond = node.getCondition().apply(this, block);
      ConditionResult trueBranch = node.getTrueExp().apply(this, cond.whenTrue);
      ConditionResult falseBranch = node.getFalseExp().apply(this,
          cond.whenFalse);
      return new ConditionResult(newBlock(trueBranch.whenTrue,
          falseBranch.whenTrue), newBlock(trueBranch.whenFalse,
          falseBranch.whenFalse));
    }

    @Override
    public ConditionResult caseAParenthesisExp(AParenthesisExp node, Block block) {
      return node.getExp().apply(this, block);
    }

    @Override
    public ConditionResult defaultPExp(PExp node, Block block) {
      ExpResult e = node.apply(expvisitor, block);
      Block whenTrue = newBlock(e.block);
      Assertion trueAssertion = new Assertion(e.resultVar, true);
      whenTrue.addFirst(trueAssertion);
      Block whenFalse = newBlock(e.block);
      Assertion falseAssertion = new Assertion(e.resultVar, false);
      whenFalse.addFirst(falseAssertion);
      binding.addCondition(node, trueAssertion, falseAssertion);
      return new ConditionResult(whenTrue, whenFalse);
    }
  }

  private static final class ConditionValueResult {
    Block whenTrue, whenFalse;
    /**
     * Value of the expression, before ToBoolean.
     */
    int resultVar;

    public ConditionValueResult(Block whenTrue, Block whenFalse, int resultVar) {
      this.whenTrue = whenTrue;
      this.whenFalse = whenFalse;
      this.resultVar = resultVar;
    }
  }

  ConditionValueTrueVisitor truevisitor = new ConditionValueTrueVisitor();

  private class ConditionValueTrueVisitor extends
      QuestionAnswerAdapter<Block, ConditionValueResult> {
    @Override
    public ConditionValueResult caseABinopExp(ABinopExp node, Block block) {
      switch (node.getOp().kindPBinop()) {
      case LOGICAL_AND: {
        // E1 && E2
        ConditionResult left = node.getLeft().apply(conditionvisitor, block);
        ConditionValueResult right = node.getRight().apply(this, left.whenTrue);
        return new ConditionValueResult(right.whenTrue, newBlock(
            left.whenFalse, right.whenFalse), right.resultVar);
      }
      case LOGICAL_OR: {
        // Example: !(a && b=c) || b (b definitely assigned in right branch)
        // E1 || E2
        ConditionValueResult left = node.getLeft().apply(this, block);
        ConditionValueResult right = node.getRight()
            .apply(this, left.whenFalse);
        int result = newvar();
        Block b = newBlock(left.whenTrue, right.whenTrue);
        b.addFirst(new Phi(result, left.resultVar, right.resultVar));
        return new ConditionValueResult(b, right.whenFalse, result);
      }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAPrefixUnopExp(APrefixUnopExp node,
        Block block) {
      switch (node.getOp().kindPPrefixUnop()) {
      case NOT:
        // swap destinations, and put a true const on the false branch (which
        // becomes the true branch)
        ConditionResult r = node.getExp().apply(conditionvisitor, block);
        int result = newvar();
        BooleanConst ct = new BooleanConst(result, true);
        r.whenFalse.addLast(ct);
        return new ConditionValueResult(r.whenFalse, r.whenTrue, result);
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAConstExp(AConstExp node, Block block) {
      switch (node.getConst().kindPConst()) {
      case BOOLEAN:
        ABooleanConst bc = (ABooleanConst) node.getConst();
        int result = newvar();
        BooleanConst ct = new BooleanConst(result, true);
        if (bc.getBool() instanceof ATrueBool) {
          block.addLast(ct);
          return new ConditionValueResult(block, newBlock(), result);
        } else {
          Block b = newBlock();
          b.addLast(ct);
          return new ConditionValueResult(b, block, result);
        }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAConditionalExp(AConditionalExp node,
        Block block) {
      ConditionResult cond = node.getCondition().apply(conditionvisitor, block);
      ConditionValueResult trueBranch = node.getTrueExp().apply(this,
          cond.whenTrue);
      ConditionValueResult falseBranch = node.getFalseExp().apply(this,
          cond.whenFalse);
      Block b = newBlock(trueBranch.whenTrue, falseBranch.whenTrue);
      int result = newvar();
      b.addLast(new Phi(result, trueBranch.resultVar, falseBranch.resultVar));
      return new ConditionValueResult(b, newBlock(trueBranch.whenFalse,
          falseBranch.whenFalse), result);
    }

    @Override
    public ConditionValueResult caseAParenthesisExp(AParenthesisExp node,
        Block block) {
      return node.getExp().apply(this, block);
    }

    @Override
    public ConditionValueResult defaultPExp(PExp node, Block block) {
      ExpResult e = node.apply(expvisitor, block);
      Block whenTrue = newBlock(e.block);
      Assertion trueAssertion = new Assertion(e.resultVar, true);
      whenTrue.addFirst(trueAssertion);
      Block whenFalse = newBlock(e.block);
      Assertion falseAssertion = new Assertion(e.resultVar, false);
      whenFalse.addFirst(falseAssertion);
      binding.addCondition(node, trueAssertion, falseAssertion);
      return new ConditionValueResult(whenTrue, whenFalse, e.resultVar);
    }
  }

  ConditionValueFalseVisitor falsevisitor = new ConditionValueFalseVisitor();

  private class ConditionValueFalseVisitor extends
      QuestionAnswerAdapter<Block, ConditionValueResult> {
    @Override
    public ConditionValueResult caseABinopExp(ABinopExp node, Block block) {
      switch (node.getOp().kindPBinop()) {
      case LOGICAL_AND: {
        // E1 && E2
        ConditionValueResult left = node.getLeft().apply(this, block);
        ConditionValueResult right = node.getRight().apply(this, left.whenTrue);
        Block b = newBlock(left.whenFalse, right.whenFalse);
        int result = newvar();
        b.addFirst(new Phi(result, left.resultVar, right.resultVar));
        return new ConditionValueResult(right.whenTrue, b, result);
      }
      case LOGICAL_OR: {
        // E1 || E2
        ConditionResult left = node.getLeft().apply(conditionvisitor, block);
        ConditionValueResult right = node.getRight()
            .apply(this, left.whenFalse);
        return new ConditionValueResult(
            newBlock(left.whenTrue, right.whenTrue), right.whenFalse,
            right.resultVar);
      }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAPrefixUnopExp(APrefixUnopExp node,
        Block block) {
      switch (node.getOp().kindPPrefixUnop()) {
      case NOT:
        // swap destinations, and put a true const on the false branch (which
        // becomes the true branch)
        ConditionResult r = node.getExp().apply(conditionvisitor, block);
        int result = newvar();
        BooleanConst ct = new BooleanConst(result, false);
        r.whenTrue.addLast(ct);
        return new ConditionValueResult(r.whenFalse, r.whenTrue, result);
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAConstExp(AConstExp node, Block block) {
      switch (node.getConst().kindPConst()) {
      case BOOLEAN:
        ABooleanConst bc = (ABooleanConst) node.getConst();
        int result = newvar();
        BooleanConst ct = new BooleanConst(result, false);
        if (bc.getBool() instanceof ATrueBool) {
          Block b = newBlock();
          b.addLast(ct);
          return new ConditionValueResult(block, b, result);
        } else {
          block.addLast(ct);
          return new ConditionValueResult(newBlock(), block, result);
        }
      default:
        return defaultPExp(node, block);
      }
    }

    @Override
    public ConditionValueResult caseAConditionalExp(AConditionalExp node,
        Block block) {
      ConditionResult cond = node.getCondition().apply(conditionvisitor, block);
      ConditionValueResult trueBranch = node.getTrueExp().apply(this,
          cond.whenTrue);
      ConditionValueResult falseBranch = node.getFalseExp().apply(this,
          cond.whenFalse);
      Block b = newBlock(trueBranch.whenFalse, falseBranch.whenFalse);
      int result = newvar();
      b.addLast(new Phi(result, trueBranch.resultVar, falseBranch.resultVar));
      return new ConditionValueResult(newBlock(trueBranch.whenTrue,
          falseBranch.whenTrue), b, result);
    }

    @Override
    public ConditionValueResult caseAParenthesisExp(AParenthesisExp node,
        Block block) {
      return node.getExp().apply(this, block);
    }

    @Override
    public ConditionValueResult defaultPExp(PExp node, Block block) {
      ExpResult e = node.apply(expvisitor, block);
      Block whenTrue = newBlock(e.block);
      Assertion trueAssertion = new Assertion(e.resultVar, true);
      whenTrue.addFirst(trueAssertion);
      Block whenFalse = newBlock(e.block);
      Assertion falseAssertion = new Assertion(e.resultVar, false);
      whenFalse.addFirst(falseAssertion);
      binding.addCondition(node, trueAssertion, falseAssertion);
      return new ConditionValueResult(whenTrue, whenFalse, e.resultVar);
    }
  }
}
