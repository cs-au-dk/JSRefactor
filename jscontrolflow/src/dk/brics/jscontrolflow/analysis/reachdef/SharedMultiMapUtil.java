package dk.brics.jscontrolflow.analysis.reachdef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility functions for working with <tt>Map&lt;K, Set&lt;V&gt;&gt;</tt> objects that
 * share unmodifiable instances of sets as values.
 */
public class SharedMultiMapUtil {

	public static <K,V> Map<K, Set<V>> union(
			Map<K, Set<V>> arg1,
			Map<K, Set<V>> arg2) {
		if (arg1 == arg2 || arg2 == null) {
	        return arg1;
	    }
	    if (arg1 == null) {
	        return arg2;
	    }
	    Map<K, Set<V>> result = new HashMap<K, Set<V>>();
	    for (Map.Entry<K, Set<V>> en1 : arg1.entrySet()) {
	        Set<V> set2 = arg2.get(en1.getKey());
	        if (set2 == null) {
	            result.put(en1.getKey(), en1.getValue());
	        } else {
	            result.put(en1.getKey(), SharedMultiMapUtil.union(en1.getValue(), set2));
	        }
	    }
	    for (Map.Entry<K, Set<V>> en2 : arg2.entrySet()) {
	        if (arg1.containsKey(en2.getKey())) {
	            continue; // handled in previous pass
	        }
	        result.put(en2.getKey(), en2.getValue());
	    }
	    return result;
	}
	
	public static <T> Set<T> union(Set<T> arg1, Set<T> arg2) {
	    if (arg1 == arg2) {
	        return arg1;
	    }
	    if (arg1 == null || arg1.isEmpty()) {
	        return arg2;
	    }
	    if (arg2 == null || arg2.isEmpty()) {
	        return arg1;
	    }
	    Set<T> result = new HashSet<T>(arg1);
	    result.addAll(arg2);
	    if (result.size() == arg1.size()) {
	        return arg1;
	    }
	    if (result.size() == arg2.size()) {
	        return arg2;
	    }
	    return result;
	}

}
