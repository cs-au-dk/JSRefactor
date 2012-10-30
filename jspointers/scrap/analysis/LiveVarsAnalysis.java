package dk.brics.jspointers.flowgraph.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.flowgraph.nodes.AssignmentNode;
import dk.brics.tajs.flowgraph.nodes.CatchNode;
import dk.brics.tajs.flowgraph.nodes.ReadVariableNode;
import dk.brics.tajs.flowgraph.nodes.WriteVariableNode;
import dk.brics.tajs.optimizer2.Decorator;
import dk.brics.tajs.optimizer2.analysis.FlowAnalysis;

public class LiveVarsAnalysis extends FlowAnalysis<Set<LocalVariable>> {
	
	private Decorator decorator;
	private ReadVarsInterface readvars;
	private Set<String> privateVariables;
	
	public LiveVarsAnalysis(Decorator decorator, ReadVarsInterface readvars, Set<String> privateVariables) {
		super(new SetLattice<LocalVariable>());
		this.decorator = decorator;
		this.readvars = readvars;
		this.privateVariables = privateVariables;
	}
	
	@Override
	public Set<Node> getJoinSet(Node node) {
		return decorator.getAllSuccessorNodes(node);
	}

	@Override
	public Set<Node> getDependencySet(Node node) {
		return decorator.getAllPredecessorNodes(node);
	}
	
	@Override
	public Set<LocalVariable> defaultLatticePoint(Node n, Set<LocalVariable> after) {
		Set<LocalVariable> s = new HashSet<LocalVariable>(after);
		boolean ignorable = false;
		// decide if the statement can be ignored due to irrelevant result node
		if (n instanceof AssignmentNode) {
		    AssignmentNode asn = (AssignmentNode)n;
		    LocalTemporaryVariable var = new LocalTemporaryVariable(asn.getResultVar());
		    if (new IsPureAssignmentVisitor().isPure(asn) && !after.contains(var)) {
                ignorable = true;
            } 
		} else if (n instanceof WriteVariableNode) {
		    WriteVariableNode write = (WriteVariableNode)n;
		    if (privateVariables.contains(write.getVarName())) {
		        // important: writes to non-private variables can NOT be ignored
		        if (!after.contains(new LocalProgramVariable(write.getVarName()))) {
		            ignorable = true;
		        }
		    }
		}
		// nothing to change if ignorable
		if (ignorable)
		    return Collections.unmodifiableSet(s);
		// update temporary variables
		if (n instanceof AssignmentNode) {
			AssignmentNode asn = (AssignmentNode)n;
			LocalTemporaryVariable var = new LocalTemporaryVariable(asn.getResultVar());
			s.remove(var);
		}
		else if (n instanceof CatchNode) {
			CatchNode ct = (CatchNode)n;
			if (ct.getTempVar() != Node.NO_VALUE) {
				s.remove(new LocalTemporaryVariable(ct.getTempVar()));
			} else {
				s.remove(new LocalTemporaryVariable(ct.getScopeObjVar()));
			}
		}
        int[] vars = readvars.getReadVars(n);
        for (int var : vars) {
            s.add(new LocalTemporaryVariable(var));
        }
		// update (private) program variables
		if (n instanceof ReadVariableNode) {
		    ReadVariableNode read = (ReadVariableNode)n;
		    if (privateVariables.contains(read.getVarName())) {
	            s.add(new LocalProgramVariable(read.getVarName()));
		    }
		}
		else if (n instanceof WriteVariableNode) {
		    WriteVariableNode write = (WriteVariableNode)n;
		    if (privateVariables.contains(write.getVarName())) {
		        s.remove(new LocalProgramVariable(write.getVarName()));
		    }
		}
		return Collections.unmodifiableSet(s);
	}
	
}
