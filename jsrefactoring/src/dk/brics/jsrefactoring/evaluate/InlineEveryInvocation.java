package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.AParenthesisExp;
import dk.brics.jsparser.node.EExp;
import dk.brics.jsparser.node.PExp;
import dk.brics.jspointers.lattice.values.natives.FunctionCallNative;
import dk.brics.jsrefactoring.Diagnostic;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.inlining.InlineToOneShotClosure;

/**
 * Attempts to inline every invocation expression to a one-shot closure.
 */
public class InlineEveryInvocation {
    public static void main(String[] args) {
        System.out.println("Analyzing " + args[0]);
        Master master = new Master(new File(args[0]));
        
        List<InlineToOneShotClosure> successes = new ArrayList<InlineToOneShotClosure>();
        List<InlineToOneShotClosure> failures = new ArrayList<InlineToOneShotClosure>();
        List<InlineToOneShotClosure> boguses = new ArrayList<InlineToOneShotClosure>();
        List<AInvokeExp> callInvokes = new ArrayList<AInvokeExp>();
        for (AInvokeExp invoke : master.getAllNodesOfType(AInvokeExp.class)) {
            if (master.isNativeCode(invoke))
                continue;
            
            PExp funcexp = invoke.getFunctionExp();
            while (funcexp instanceof AParenthesisExp) {
                funcexp = ((AParenthesisExp)funcexp).getExp();
            }
            if (funcexp.kindPExp() != EExp.NAME && funcexp.kindPExp() != EExp.PROPERTY)
                continue;
            
            InlineToOneShotClosure refactoring = new InlineToOneShotClosure(master, invoke);
            
            if (refactoring.isBogusInvoke()) {
                boguses.add(refactoring);
            } else if (refactoring.getDiagnostics().size() == 0) {
                successes.add(refactoring);
            } else {
                failures.add(refactoring);
                
                if (master.getCalledFunctions(invoke).equals(Collections.singleton(FunctionCallNative.Instance))) {
                    callInvokes.add(invoke);
                }
            }
        }
        
        int numWithOKClosures = 0;
        int numFuncDeclProofs = 0;
        int numScopeConsistencyProofs = 0;
        int numWithUnsafeClosures = 0;
        int numWithInaccessibleClosures = 0;
        System.out.println("SUCCESSES");
        for (InlineToOneShotClosure refactoring : successes) {
            System.out.printf("  %4d %s\n", refactoring.getInvoke().getLparen().getLine(), AstUtil.toSourceString(refactoring.getInvoke()));
            if (refactoring.getSafeClosureVars().size() > 0) {
                numWithOKClosures++;
                System.out.printf("       > OK closure vars: %s\n", refactoring.getSafeClosureVars());
                System.out.printf("       > Closure type: %s\n", refactoring.getCommonClosureType());
                switch (refactoring.getCommonClosureType()) {
                case FunctionDeclaration:
                    numFuncDeclProofs++;
                    break;
                case ScopeConsistency:
                    numScopeConsistencyProofs++;
                    break;
                }
            }
        }
        System.out.println("FAILURES");
        for (InlineToOneShotClosure refactoring : failures) {
            System.out.printf("  %4d %s\n", refactoring.getInvoke().getLparen().getLine(), AstUtil.toSourceString(refactoring.getInvoke()));
            for (Diagnostic diag : refactoring.getDiagnostics()) {
                System.out.printf("       > %s\n", diag.getMessage());
            }
            if (refactoring.getUnsafeClosureVars().size() > 0 && refactoring.getInaccessibleClosureVars().size() == 0 && !refactoring.hasMultipleTargets() && !refactoring.hasNativeTarget()) {
                numWithUnsafeClosures++;
            }
            if (refactoring.getInaccessibleClosureVars().size() > 0 && !refactoring.hasMultipleTargets() && !refactoring.hasNativeTarget()) {
                numWithInaccessibleClosures++;
            }
        }
        System.out.println("BOGUSES");
        for (InlineToOneShotClosure refactoring : boguses) {
            System.out.printf("  %4d %s\n", refactoring.getInvoke().getLparen().getLine(), AstUtil.toSourceString(refactoring.getInvoke()));
        }
        
        System.out.printf("\nClosure stats: %3d succeeds with safe closure vars (%d func decl, %d scope consistency)\n", numWithOKClosures, numFuncDeclProofs, numScopeConsistencyProofs);
        System.out.printf("               %3d fails with unsafe closure vars\n", numWithUnsafeClosures);
        System.out.printf("               %3d fails with inaccessible closure vars\n", numWithInaccessibleClosures);
        
        System.out.printf("\nCall invokes:  %3d\n", callInvokes.size());
        
        System.out.printf("\nTOTAL: %d / %d invocations (%d unreachable).\n", successes.size(), successes.size() + failures.size(), boguses.size());
    }
}
