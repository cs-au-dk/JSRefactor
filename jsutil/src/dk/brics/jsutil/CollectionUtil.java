package dk.brics.jsutil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionUtil {

	public static <B,T> Set<T> filter(Set<B> set, Class<T> type) {
	    Set<T> result = new HashSet<T>();
	    for (B value : set) {
	        if (type.isInstance(value)) {
	            result.add(type.cast(value));
	        }
	    }
	    return result;
	}

	public static boolean containsInstanceOf(Collection<?> set, Class<?> clazz) {
		for (Object obj : set) {
			if (clazz.isInstance(obj))
				return true;
		}
		return false;
	}

	public static boolean intersects(Set<?> a, Set<?> b) {
	    if (a.size() < b.size()) {
	        for (Object obj : a) {
	            if (b.contains(obj)) {
	                return true;
	            }
	        }
	    } else {
	        for (Object obj : b) {
	            if (a.contains(obj)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}

	public static <T> Set<T> union(Set<? extends T> a, Set<? extends T> b) {
	    Set<T> result = new HashSet<T>(a);
	    result.addAll(b);
	    return result;
	}
}
