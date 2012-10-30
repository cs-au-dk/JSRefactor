package dk.brics.jsrefactoring;

import java.util.ArrayList;
import java.util.List;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.Literals;
import dk.brics.jsparser.node.ABinopExp;
import dk.brics.jsparser.node.ACatchClause;
import dk.brics.jsparser.node.AConstExp;
import dk.brics.jsparser.node.AFunctionDeclStmt;
import dk.brics.jsparser.node.AFunctionExp;
import dk.brics.jsparser.node.ANameExp;
import dk.brics.jsparser.node.ANormalObjectLiteralProperty;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.AStringConst;
import dk.brics.jsparser.node.AVarDecl;
import dk.brics.jsparser.node.EBinop;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Token;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.CatchAccess;
import dk.brics.jsrefactoring.nodes.ConstInExpAccess;
import dk.brics.jsrefactoring.nodes.FunctionDeclAccess;
import dk.brics.jsrefactoring.nodes.FunctionExpAccess;
import dk.brics.jsrefactoring.nodes.NameExpAccess;
import dk.brics.jsrefactoring.nodes.ParameterAccess;
import dk.brics.jsrefactoring.nodes.PropertyExpAccess;
import dk.brics.jsrefactoring.nodes.PropertyInitializerAccess;
import dk.brics.jsrefactoring.nodes.VarDeclAccess;

public class AccessFinder {
    /**
     * Returns all property name nodes with the given name in the program.
     */
    public static List<AccessWithName> getNamedAccesses(NodeFinder nodes, String name) {
        List<AccessWithName> list = new ArrayList<AccessWithName>();
        for (APropertyExp exp : nodes.getAllNodesOfType(APropertyExp.class)) {
            String expName = Literals.getName(exp);
            if (expName.equals(name)) {
                list.add(new PropertyExpAccess(exp));
            }
        }
        for (ANormalObjectLiteralProperty prty : nodes.getAllNodesOfType(ANormalObjectLiteralProperty.class)) {
            String prtyName = AstUtil.getPropertyName(prty.getName());
            if (prtyName.equals(name)) {
                list.add(new PropertyInitializerAccess(prty));
            }
        }
        for (ABinopExp exp : nodes.getAllNodesOfType(ABinopExp.class)) {
            if (exp.getOp().kindPBinop() != EBinop.IN)
                continue;
            PExp left = exp.getLeft();
            if (!(left instanceof AConstExp))
                continue; // non-constant node is not a property name node
            AConstExp leftc = (AConstExp)left;
            if (!(leftc.getConst() instanceof AStringConst))
                continue; // int and boolean are safe
            AStringConst sc = (AStringConst)leftc.getConst();
            String expName = Literals.parseStringLiteral(sc.getStringLiteral().getText());
            if (expName.equals(name)) {
                list.add(new ConstInExpAccess(sc, exp));
            }
        }
        for (ANameExp exp : nodes.getAllNodesOfType(ANameExp.class)) {
            String expName = Literals.getName(exp);
            if (expName.equals(name)) {
                list.add(new NameExpAccess(exp));
            }
        }
        for (AVarDecl decl : nodes.getAllNodesOfType(AVarDecl.class)) {
            String varName = Literals.getName(decl);
            if (varName.equals(name)) {
                list.add(new VarDeclAccess(decl));
            }
        }
        for (IFunction func : nodes.getAllNodesOfType(IFunction.class)) {
            int index = 0;
            for (Token param : func.getParameters()) {
                String paramName = Literals.parseIdentifier(param.getText());
                if (paramName.equals(name)) {
                    list.add(new ParameterAccess(func, index));
                }
                index++;
            }
          	if (func.getName() != null && Literals.parseIdentifier(func.getName().getText()).equals(name)) {
          	  if (func instanceof AFunctionDeclStmt) {
          	    FunctionDeclAccess functionDeclAccess = new FunctionDeclAccess((AFunctionDeclStmt)func);
                list.add(functionDeclAccess);
          	  } else {
                FunctionExpAccess functionExpAccess = new FunctionExpAccess((AFunctionExp)func);
                list.add(functionExpAccess);
          	  }
          	}
        }
        for (ACatchClause node : nodes.getAllNodesOfType(ACatchClause.class)) {
        	if (Literals.parseIdentifier(node.getName().getText()).equals(name)) {
        		list.add(new CatchAccess(node));
        	}
        }
        return list;
    }
    
    /**
     * Returns all {@link PropertyExpAccess}, {@link PropertyInitializerAccess} and {@link ConstInExpAccess}.
     */
    public static List<AccessWithName> getPropertyRelatedAccesses(NodeFinder nodes) {
        List<AccessWithName> list = new ArrayList<AccessWithName>();
        for (APropertyExp exp : nodes.getAllNodesOfType(APropertyExp.class)) {
            list.add(new PropertyExpAccess(exp));
        }
        for (ANormalObjectLiteralProperty prty : nodes.getAllNodesOfType(ANormalObjectLiteralProperty.class)) {
            list.add(new PropertyInitializerAccess(prty));
        }
        for (ABinopExp exp : nodes.getAllNodesOfType(ABinopExp.class)) {
            if (exp.getOp().kindPBinop() != EBinop.IN)
                continue;
            PExp left = exp.getLeft();
            if (!(left instanceof AConstExp))
                continue; // non-constant node is not a property name node
            AConstExp leftc = (AConstExp)left;
            if (!(leftc.getConst() instanceof AStringConst))
                continue; // int and boolean are safe
            AStringConst sc = (AStringConst)leftc.getConst();
            list.add(new ConstInExpAccess(sc, exp));
        }
        return list;
    }
}
