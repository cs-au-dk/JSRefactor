package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.List;

public class CoerceToObject extends FlowNode {
	private InputPoint argument = new InputPoint(this);
	private OutputPoint result = new OutputPoint(this);
	
	public InputPoint getArgument() {
		return argument;
	}
	public OutputPoint getResult() {
		return result;
	}
	
	@Override
	public void apply(FlowNodeVisitor visitor) {
		visitor.caseCoerceToObject(this);
	}
	
	@Override
	public List<InputPoint> getInputPoints() {
		return Arrays.asList(argument);
	}
	
	@Override
	public List<OutputPoint> getOutputPoints() {
		return Arrays.asList(result);
	}
	
	@Override
	public boolean isPurelyLocal() {
		return true;
	}
	
}
