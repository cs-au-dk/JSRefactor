package dk.brics.jspointers.test.instrument;

import java.util.Set;

import dk.brics.jsparser.node.AInvokeExp;
import dk.brics.jsparser.node.IExpOrStmt;
import dk.brics.jsparser.node.IFunction;
import dk.brics.jsparser.node.IScopeBlockNode;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsparser.node.PExp;
import dk.brics.jsparser.node.Start;
import dk.brics.jsutil.MultiMap;

public interface InstrumentData {
	Set<IFunction> getTargets(AInvokeExp invoke);
	
	/**
	 * For every user-allocated object that <i>exp</i> may evaluate to,
	 * the result must contain the allocation site for that object.
	 * @param exp an expression
	 * @return unmodifiable set
	 */
	Set<AllocSite> getResultAllocationSites(PExp exp);
	
	/**
	 * For every user-allocated object that <i>var</i> in <i>scope</i> may refer
	 * to just after <i>p</i> completed normally, the result must contain the allocation 
	 * site for that object.
	 * @param scope a scope
	 * @param var a variable
	 * @param p an expression or statement 
	 * @return unmodifiable set
	 */
	Set<AllocSite> getVariableAllocationSites(IScopeBlockNode scope, String var, IExpOrStmt p);
	
	Set<AllocSite> getPrototypeOf(AllocSite site);
	MultiMap<String,AllocSite> getPointsTo(AllocSite site);
	
	Set<Start> getAst();
	
	boolean isNative(NodeInterface node);
	
	Set<AllocSite> getArgumentAllocationSites(IFunction function, int argIndex);
	Set<AllocSite> getThisAllocationSites(IFunction function);
	
	
	
}
