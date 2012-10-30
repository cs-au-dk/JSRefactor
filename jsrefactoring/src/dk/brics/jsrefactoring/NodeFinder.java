package dk.brics.jsrefactoring;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.jsparser.analysis.DepthFirstAdapter;
import dk.brics.jsparser.node.Node;

/**
 * Utility class for finding all nodes of a given type; caches results for improved performance.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NodeFinder {
	private List<InputFile> allFiles;
	private Map<Class, Set<Node>> cache = new LinkedHashMap<Class, Set<Node>>();
	
	public NodeFinder(Master master, Class... types) {
		this.allFiles = master.getAllInputFiles();
		findNodes(types);
	}
	
	private void findNodes(final Class... types) {
		for(Class type : types)
			if(!cache.containsKey(type))
				cache.put(type, new LinkedHashSet<Node>());
        for (InputFile file : allFiles) {
            file.getAst().apply(new DepthFirstAdapter() {
                @Override
                public void defaultIn(Node node) {
                	for(Class type : types) {
                		if(type.isInstance(node)) {
                			cache.get(type).add(node);
                		}
                	}
                }
            });
        }
    }
	
	/**
	 * <p>
	 * Returns every AST node of the given type (or a subtype thereof),
	 * including the AST of harness files. Use {@link Master#isNativeCode(Node)}
	 * to determine if an AST node is from a harness file.
	 * </p>

	 * @param <T> the type of node to get
	 * @param type class or interface object for the type to get
	 * @return a newly created set
	 */
	public <T> Set<T> getAllNodesOfType(Class<T> type) {
		if(!cache.containsKey(type))
			findNodes(type);
		return (Set<T>)Collections.unmodifiableSet(cache.get(type));
	}
}
