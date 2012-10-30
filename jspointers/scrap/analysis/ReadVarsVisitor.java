package dk.brics.jspointers.flowgraph.analysis;

import dk.brics.tajs.flowgraph.Node;
import dk.brics.tajs.flowgraph.NodeVisitor;
import dk.brics.tajs.flowgraph.nodes.AssumeNode;
import dk.brics.tajs.flowgraph.nodes.BinaryOperatorNode;
import dk.brics.tajs.flowgraph.nodes.CallNode;
import dk.brics.tajs.flowgraph.nodes.CatchNode;
import dk.brics.tajs.flowgraph.nodes.ConstantNode;
import dk.brics.tajs.flowgraph.nodes.DeclareEventHandlerNode;
import dk.brics.tajs.flowgraph.nodes.DeclareFunctionNode;
import dk.brics.tajs.flowgraph.nodes.DeclareVariableNode;
import dk.brics.tajs.flowgraph.nodes.DeletePropertyNode;
import dk.brics.tajs.flowgraph.nodes.EnterWithNode;
import dk.brics.tajs.flowgraph.nodes.EventDispatcherNode;
import dk.brics.tajs.flowgraph.nodes.EventEntryNode;
import dk.brics.tajs.flowgraph.nodes.ExceptionalReturnNode;
import dk.brics.tajs.flowgraph.nodes.GetPropertiesNode;
import dk.brics.tajs.flowgraph.nodes.HasNextPropertyNode;
import dk.brics.tajs.flowgraph.nodes.IfNode;
import dk.brics.tajs.flowgraph.nodes.LeaveWithNode;
import dk.brics.tajs.flowgraph.nodes.NewObjectNode;
import dk.brics.tajs.flowgraph.nodes.NextPropertyNode;
import dk.brics.tajs.flowgraph.nodes.NopNode;
import dk.brics.tajs.flowgraph.nodes.ReadPropertyNode;
import dk.brics.tajs.flowgraph.nodes.ReadVariableNode;
import dk.brics.tajs.flowgraph.nodes.ReturnNode;
import dk.brics.tajs.flowgraph.nodes.ThrowNode;
import dk.brics.tajs.flowgraph.nodes.TypeofNode;
import dk.brics.tajs.flowgraph.nodes.UnaryOperatorNode;
import dk.brics.tajs.flowgraph.nodes.WritePropertyNode;
import dk.brics.tajs.flowgraph.nodes.WriteVariableNode;

/**
 * Determines which relevant variables are read for each node. This may differ
 * from what variables the node would actually use if evaluated runtime, due 
 * to the abstractions made by the analysis.
 * <p/>
 * For example, in the statement <tt>x = y * z</tt>, the variables <tt>y,z</tt> will
 * not be considered relevant, because the analysis knows the result is a number regardless
 * of their value.
 */
public class ReadVarsVisitor implements NodeVisitor<Void>, ReadVarsInterface {
    
    private int[] result;
    
    private static int[] NONE = new int[0];
    

	public int[] getReadVars(Node node) {
        result=null;
        node.visitBy(this, null);
        if (result == null)
            throw new RuntimeException("Error in ReadVarsVisitor for node " + node.getClass());
        return result;
    }
    
    public void visit(AssumeNode n, Void a) {
        result = NONE;
    }
    public void visit(BinaryOperatorNode n, Void a) {
    	if (n.getOperator() == BinaryOperatorNode.Op.ADD) {
    		// only + is where we care about the arguments, to distinguish numbers and strings
    		result = new int[] {n.getArg1Var(), n.getArg2Var()};
    	} else {
    		result = NONE; // the operands are irrelevant for the type-system
    	}
    }
    public void visit(CallNode n, Void a) {
        if (n.getBaseVar() != Node.NO_VALUE) {
            result = new int[n.getNumberOfArgs() + 2];
            result[0] = n.getBaseVar();
            result[1] = n.getFunctionVar();
            for (int i=0; i<n.getNumberOfArgs(); i++) {
                result[i+2] = n.getArgVar(i);
            }
        } else {
            result = new int[n.getNumberOfArgs() + 1];
            result[0] = n.getFunctionVar();
            for (int i=0; i<n.getNumberOfArgs(); i++) {
                result[i+1] = n.getArgVar(i);
            }
        }
    }
    
    public void visit(CatchNode n, Void a) {
        result = NONE;
    }
    
    public void visit(ConstantNode n, Void a) {
        result = NONE;
    }

    public void visit(DeletePropertyNode n, Void a) {
        result = NONE; // because we don't model it (FIXME)
    }
    
    public void visit(EnterWithNode n, Void a) {
        result = new int[] {n.getObjectVar()};
    }

    public void visit(ExceptionalReturnNode n, Void a) {
        result = NONE;
    }

    public void visit(DeclareFunctionNode n, Void a) {
        result = NONE;
    }

    public void visit(GetPropertiesNode n, Void a) {
        result = NONE; // because we don't model it (FIXME)
    }

    public void visit(IfNode n, Void a) {
        result = NONE;
    }

    public void visit(LeaveWithNode n, Void a) {
        result = NONE;
    }

    public void visit(NewObjectNode n, Void a) {
        result = NONE;
    }

    public void visit(NextPropertyNode n, Void a) {
        result = NONE;
    }

    public void visit(HasNextPropertyNode n, Void a) {
        result = NONE;
    }

    public void visit(NopNode n, Void a) {
        result = NONE;
    }

    public void visit(ReadPropertyNode n, Void a) {
        if (n.getPropertyStr() != null) {
            result = new int[] {n.getBaseVar()};
        } else {
            result = new int[] {n.getBaseVar(), n.getPropertyVar()};
        }
    }

    public void visit(ReadVariableNode n, Void a) {
        result = NONE;
    }

    public void visit(ReturnNode n, Void a) {
        if (n.getValueVar() != Node.NO_VALUE) {
            result = new int[] {n.getValueVar()};
        } else {
            result = NONE;
        }
    }

    public void visit(ThrowNode n, Void a) {
        result = new int[] {n.getValueVar()};
    }

    public void visit(TypeofNode n, Void a) {
        if (n.getArgVar() != Node.NO_VALUE) {
            result = new int[] {n.getArgVar()};
        } else {
            result = NONE;
        }
    }

    public void visit(UnaryOperatorNode n, Void a) {
        result = NONE; // the operand is irrelevant for the type-system
    }

    public void visit(DeclareVariableNode n, Void a) {
        result = NONE;
    }

    public void visit(WritePropertyNode n, Void a) {
        if (n.getPropertyStr() != null) {
            result = new int[] {n.getBaseVar(), n.getValueVar()};
        } else {
            result = new int[] {n.getBaseVar(), n.getPropertyVar(), n.getValueVar()};
        }
    }

    public void visit(WriteVariableNode n, Void a) {
        result = new int[] {n.getValueVar()};
    }

    public void visit(EventDispatcherNode n, Void a) {
        result = NONE;
    }

    public void visit(DeclareEventHandlerNode n, Void a) {
        result = NONE;
    }
    public void visit(EventEntryNode n, Void a) {
    	result = NONE;
	}
    
    
    
}
