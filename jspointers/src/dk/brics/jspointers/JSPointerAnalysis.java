package dk.brics.jspointers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jscontrolflow.Function;
import dk.brics.jspointers.analysis.JSAnalysis;
import dk.brics.jspointers.analysis.ObjectSensitiveInvocation;
import dk.brics.jspointers.cfg2dataflow.DataflowCreator;
import dk.brics.jspointers.cfg2dataflow.IControlflow2DataflowBinding;
import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.keys.NativeArgKey;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.lattice.values.natives.EvalNative;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jspointers.solver.Solver;

public class JSPointerAnalysis {
    private DataflowGraph dataflow;
    private AnalysisResult<Key, Set<Value>> result;
    private Set<Function> toplevels;
    private Set<Function> harness;

    public JSPointerAnalysis(Set<Function> toplevels, Set<Function> harness, IControlflow2DataflowBinding binding) {
        this.toplevels = toplevels;
        this.harness = harness;

        dataflow = DataflowCreator.convert(toplevels, harness, binding);
        result = Solver.solve(new JSAnalysis(dataflow, new ObjectSensitiveInvocation(1)));
    }

    public DataflowGraph getDataflow() {
        return dataflow;
    }
    public AnalysisResult<Key, Set<Value>> getResult() {
        return result;
    }
    public Set<Value> getResultAt(Key key) {
        Set<Value> values = result.get(key);
        if (values == null) {
            return Collections.emptySet();
        } else {
            return values;
        }
    }
    public <T> Set<T> getResultAt(Key key, Class<T> filterType) {
        Set<Value> values = result.get(key);
        if (values == null) {
            return Collections.emptySet();
        } else {
            Set<T> result = new HashSet<T>();
            for (Value value : values) {
                if (filterType.isInstance(value)) {
                    result.add(filterType.cast(value));
                }
            }
            return result;
        }
    }
    public Set<Function> getHarness() {
        return harness;
    }
    public Set<Function> getToplevels() {
        return toplevels;
    }
    
}
