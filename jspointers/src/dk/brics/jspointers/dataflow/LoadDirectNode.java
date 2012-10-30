package dk.brics.jspointers.dataflow;

import java.util.Arrays;
import java.util.List;

/**
 * Load a property without using the prototype chain.
 */
public class LoadDirectNode extends FlowNode {
	private InputPoint base = new InputPoint(this);
	private OutputPoint result = new OutputPoint(this);
	private String property;
	
	public LoadDirectNode(String property) {
		this.property = property;
	}

	public OutputPoint getResult() {
		return result;
	}
	public InputPoint getBase() {
		return base;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public void apply(FlowNodeVisitor visitor) {
		visitor.caseLoadDirect(this);
	}

	@Override
	public List<InputPoint> getInputPoints() {
		return Arrays.asList(base);
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
