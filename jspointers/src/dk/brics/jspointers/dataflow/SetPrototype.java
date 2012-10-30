package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetPrototype extends FlowNode {
	private InputPoint base = new InputPoint(this);
	private InputPoint value = new InputPoint(this);
	
	public InputPoint getBase() {
		return base;
	}
	public InputPoint getValue() {
		return value;
	}
	
	@Override
	public void apply(FlowNodeVisitor visitor) {
		visitor.caseSetPrototype(this);
	}
	@Override
	public List<InputPoint> getInputPoints() {
		return Arrays.asList(base, value);
	}
	@Override
	public List<OutputPoint> getOutputPoints() {
		return Collections.emptyList();
	}
	@Override
	public boolean isPurelyLocal() {
		return false;
	}
}
