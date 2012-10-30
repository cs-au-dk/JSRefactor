package dk.brics.jspointers.solver;

import java.util.HashMap;

/**
 * Stores the fixed point found by a monotone dataflow analysis.
 * 
 * @author Asger
 *
 * @param <N>
 * @param <T>
 */
public class AnalysisResult<N,T> extends HashMap<N,T> {
    private static final long serialVersionUID = -4496647857262612039L;

    private T bottom;

    public AnalysisResult(T bottom) {
        this.bottom = bottom;
    }

    public T getValue(N key) {
        T t = get(key);
        if (t == null) {
            return bottom;
        } else {
            return t;
        }
    }
}
