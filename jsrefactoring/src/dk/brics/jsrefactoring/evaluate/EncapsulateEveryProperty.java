package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import dk.brics.jscontrolflow.Function;
import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.node.ANewExp;
import dk.brics.jsparser.node.APropertyExp;
import dk.brics.jsparser.node.EExp;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jsrefactoring.InputFile;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.encapsulateprty.EncapsulateProperty;

public class EncapsulateEveryProperty {	
	private void encapsulate(File benchmark) {
		int numRef = 0, numFail = 0;
		System.out.println(benchmark);
		Master input = new Master(benchmark);
		
		NodeFinder finder = new NodeFinder(input, APropertyExp.class, ANewExp.class);
		for(APropertyExp exp : finder.getAllNodesOfType(APropertyExp.class)) {
			if(input.isNativeCode(exp))
				continue;
			if(exp.getBase().kindPExp() != EExp.THIS || !AstUtil.isLValue(exp))
				continue;
			IFunction fun = exp.getAncestor(IFunction.class);
			if(fun == null)
				continue;
			if(!invokedAsConstructor(fun, input, finder))
				continue;
			File sourceFile = input.getSourceFile(exp);
			int sourceLine = input.getTranslatedLineNumber(exp);
			System.out.print("Refactoring " + AstUtil.toSourceString(exp) + " in file " + sourceFile + " at line " + sourceLine + "...");
			EncapsulateProperty refactoring = new EncapsulateProperty(input, exp);
			refactoring.getChanges();
			++numRef;
			//Assert.assertEquals(dump(new Master(benchmark)), dump(input));
			if(refactoring.getDiagnostics().isEmpty()) {
				System.out.println(" success.");
			} else {
				++numFail;
				System.out.println(" error: " + refactoring.getDiagnostics().get(0).getMessage() + ".");
			}
		}
		//System.out.println(benchmark.getName() + ": " + numRef + " attempts, " + (numRef-numFail) + " successes");
	}
	
	private boolean invokedAsConstructor(IFunction fun, Master input, NodeFinder finder) {
		Set<Function> funs = input.getFunctions(fun);
		for(ANewExp exp : finder.getAllNodesOfType(ANewExp.class))
			for(FunctionValue target : input.getCalledFunctions(exp))
				if(target instanceof UserFunctionValue && funs.contains(((UserFunctionValue)target).getFunction()))
					return true;
		return false;
	}
	
	public String dump(Master input) {
		StringBuffer buf = new StringBuffer();
		for(InputFile f : input.getUserFiles())
			buf.append(AstUtil.toSourceString(f.getAst()));
		String s = buf.toString();
		return s;
	}
	
	@Test public void test() {
		encapsulate(Benchmarks.getBenchmark("msie", "mrpotatogun.html"));
	}
	
	public static void main(String[] args) {
		EncapsulateEveryProperty e = new EncapsulateEveryProperty();
		for(File benchmark : Benchmarks.getBenchmarks())
			e.encapsulate(benchmark);
	}
}
