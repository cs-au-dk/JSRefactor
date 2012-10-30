package dk.brics.jspointers.flowgraph.analysis;

import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jscontrolflow.statements.AssignmentQuestionAnswer;
import dk.brics.jscontrolflow.statements.BinaryOperation;
import dk.brics.jscontrolflow.statements.BooleanConst;
import dk.brics.jscontrolflow.statements.Call;
import dk.brics.jscontrolflow.statements.Catch;
import dk.brics.jscontrolflow.statements.ConstructorCall;
import dk.brics.jscontrolflow.statements.CreateFunction;
import dk.brics.jscontrolflow.statements.DeleteDynamicProperty;
import dk.brics.jscontrolflow.statements.DeleteProperty;
import dk.brics.jscontrolflow.statements.GetNextProperty;
import dk.brics.jscontrolflow.statements.NewArray;
import dk.brics.jscontrolflow.statements.NewObject;
import dk.brics.jscontrolflow.statements.NewRegexp;
import dk.brics.jscontrolflow.statements.NullConst;
import dk.brics.jscontrolflow.statements.NumberConst;
import dk.brics.jscontrolflow.statements.Phi;
import dk.brics.jscontrolflow.statements.ReadDynamicProperty;
import dk.brics.jscontrolflow.statements.ReadProperty;
import dk.brics.jscontrolflow.statements.ReadThis;
import dk.brics.jscontrolflow.statements.ReadVariable;
import dk.brics.jscontrolflow.statements.StringConst;
import dk.brics.jscontrolflow.statements.UnaryOperation;
import dk.brics.jscontrolflow.statements.UndefinedConst;

/**
 * Determines whether a given assignment node is of any interest beyond
 * its result. An assignment is called <em>pure</em> if it is irrelevant
 * when its result is irrelevant.
 * <p/>
 * For example, a call node is not pure because it is important even if
 * its result is discarded. A binary + node is pure, because only its
 * result is of interest (because of the way we model coercion). 
 */
public class IsPureAssignmentVisitor implements AssignmentQuestionAnswer<Void, Boolean> {
	
	public static final IsPureAssignmentVisitor Instance = new IsPureAssignmentVisitor();
	
	public static boolean isPure(Assignment asn) {
		return asn.apply(Instance, null);
	}

	@Override
	public Boolean caseBinaryOperation(BinaryOperation stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseBooleanConst(BooleanConst stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseCall(Call stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseCatch(Catch stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseConstructorCall(ConstructorCall stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseCreateFunction(CreateFunction stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseDeleteDynamicProperty(DeleteDynamicProperty stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseDeleteProperty(DeleteProperty stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseGetNextProperty(GetNextProperty stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseNewArray(NewArray stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseNewObject(NewObject stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseNewRegexp(NewRegexp stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseNullConst(NullConst stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseNumberConst(NumberConst stm, Void arg) {
		return true;
	}

	@Override
	public Boolean casePhi(Phi stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseReadDynamicProperty(ReadDynamicProperty stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseReadProperty(ReadProperty stm, Void arg) {
		return false;
	}

	@Override
	public Boolean caseStringConst(StringConst stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseUndefinedConst(UndefinedConst stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseUnaryOperation(UnaryOperation stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseReadThis(ReadThis stm, Void arg) {
		return true;
	}

	@Override
	public Boolean caseReadVariable(ReadVariable stm, Void arg) {
		return true;
	}
	
}
