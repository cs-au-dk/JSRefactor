package dk.brics.jsrefactoring.evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import dk.brics.jsparser.AstUtil;
import dk.brics.jsparser.TokenPair;
import dk.brics.jsparser.node.Node;
import dk.brics.jsparser.node.NodeInterface;
import dk.brics.jsrefactoring.AccessFinder;
import dk.brics.jsrefactoring.CommandLineUtil;
import dk.brics.jsrefactoring.Diagnostic;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.NodeFinder;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.renameprty.RenameProperty;

/**
 * Attempts to rename every property in the given JavaScript file, and
 * reports statistics about how many successes and different types of
 * failures occurred.
 */
public class RenameEveryProperty {
	private static <K> void increment(Map<K,Integer> map, K key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key)+1);
		} else {
			map.put(key, 1);
		}
	}
	private static <K> int count(Map<K,Integer> map, K key) {
		if (map.containsKey(key))
			return map.get(key);
		else
			return 0;
	}
	private static <K,V> void append(Map<K,List<V>> map, K key, V value) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
		} else {
			List<V> list = new ArrayList<V>();
			list.add(value);
			map.put(key, list);
		}
	}
	private static <K,V> List<V> getlist(Map<K,List<V>> map, K key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			return Collections.emptyList();
		}
	}
	public static void main(String[] args) {
		boolean details = false;
		for (int i=1; i<args.length; i++) {
			String arg = args[i];
			if (arg.equals("-details")) {
				details = true;
			} else {
				System.err.println("Unrecognized argument: " + arg);
			}
		}
		
		String detailPropertyName = null;
		if (details) {
			detailPropertyName = CommandLineUtil.promptString("What property would you like details about?");
			if (detailPropertyName.isEmpty()) {
				detailPropertyName = null;
			}
		}
		
		System.out.println("Analyzing " + args[0]);
		Master master = new Master(new File(args[0]));
		
		System.out.print("Renaming properties");
		
		int numSuccesses = 0;
		int numTrials = 0;
		int numUnreachable = 0;
		Set<String> namesWithErrors = new HashSet<String>();
		Map<String,Integer> nameFrequency = new HashMap<String,Integer>();
		Map<String,Integer> unreachableFrequency = new HashMap<String,Integer>();
		Set<AccessWithName> visitedSuccess = new HashSet<AccessWithName>();
		Set<AccessWithName> visitedFailure = new HashSet<AccessWithName>();
		Map<String,List<Integer>> components = new HashMap<String,List<Integer>>();
		Map<String,List<Integer>> failComponents = new HashMap<String,List<Integer>>();
		Map<String,Integer> nameSuccesses = new HashMap<String,Integer>();
		List<AccessWithName> propertyRelatedAccesses = AccessFinder.getPropertyRelatedAccesses(new NodeFinder(master));
		StringBuilder detailMsg = new StringBuilder();
		for (AccessWithName access : propertyRelatedAccesses) {
			if (master.isNativeCode(access.getNode()))
				continue;
			String name = access.getName();
			
			if (access.getDirectReceivers(master).isEmpty())  {
				increment(unreachableFrequency, name);
				numUnreachable++;
				
				if (detailPropertyName != null && detailPropertyName.equals(name)) {
					detailMsg.append("-----------------------------\n");
					detailMsg.append("UNREACHABLE ");
					appendInfo(detailMsg, access.getNode());
				}
				continue;
			}
			
			boolean success;
			if (visitedSuccess.contains(access)) {
				success = true;
			} else if (visitedFailure.contains(access)) {
				success = false;
			} else {
				RenameProperty rename = new RenameProperty(master, access, "_$_unused_property_name_$_");
				success = rename.getDiagnostics().size() == 0;
				System.out.print(".");
				
				if (success) {
					visitedSuccess.addAll(rename.getAffectedNames());
					append(components, name, rename.getAffectedNames().size());
				} else {
					visitedFailure.addAll(rename.getAffectedNames());
					namesWithErrors.add(name);
					append(failComponents, name, rename.getAffectedNames().size());
					visitedFailure.addAll(rename.getAffectedNames());
				}
				
				if (detailPropertyName != null && detailPropertyName.equals(name)) {
					detailMsg.append("-----------------------------\n");
					for (AccessWithName acc : rename.getAffectedNames()) {
						appendInfo(detailMsg, acc.getNode());
					}
					for (Diagnostic diag : rename.getDiagnostics()) {
						detailMsg.append(diag.getMessage()).append("\n");
					}
				}
			}
			
			if (success) {
				numSuccesses++;
				increment(nameSuccesses, name);
				
			}
			numTrials++;
			increment(nameFrequency, name);
			
		}
		
		System.out.println();
		System.out.printf("%-20s   %13s / %-7s %-20s %-20s %-20s\n", "Property name", "success","total","components","fail components","unreachable");
		for (String name : nameFrequency.keySet()) {
			int numsucc = count(nameSuccesses, name);
			int freq = count(nameFrequency, name);
			boolean noteworthy = numsucc != 0 && numsucc != freq;
			String notestr = noteworthy? "!" : " ";
			System.out.printf("%-30s %s %3d / %3d     %-20s %-20s %3d\n", name, notestr, count(nameSuccesses, name), count(nameFrequency, name), getlist(components, name), getlist(failComponents, name), count(unreachableFrequency, name));
		}
		System.out.println();
		System.out.printf("%-30s   %3d / %3d     %24d\n", "Total", numSuccesses, numTrials, numUnreachable);
		
		if (!nameFrequency.keySet().contains(unreachableFrequency.keySet())) {
			System.out.println();
			Set<String> unreachableNames = new HashSet<String>(unreachableFrequency.keySet());
			unreachableNames.removeAll(nameFrequency.keySet());
			System.out.println("Names always unreachable: " + unreachableNames);
		}
		
		Map<Integer,Integer> numComp2numAccesses = new TreeMap<Integer,Integer>(); // TreeMap for sorting
		int withMoreThanOneComp = 0;
		int numCompsBeyondFirst = 0;
		int mixedSuccessAndError = 0;
		int totalComponents = 0;
		for (Map.Entry<String,List<Integer>> en : components.entrySet()) {
			int numComps = en.getValue().size();
			if (namesWithErrors.contains(en.getKey())) {
				mixedSuccessAndError++;
			}
			increment(numComp2numAccesses, numComps);
			if (numComps > 1) {
				withMoreThanOneComp++;
				numCompsBeyondFirst += (numComps - 1);
			}
			totalComponents += en.getValue().size();
		}
		System.out.println("\nCOMPONENT DETAILS");
		for (Map.Entry<Integer,Integer> en : numComp2numAccesses.entrySet()) {
			System.out.printf("Property names with %d components: %4d\n", en.getKey(), en.getValue());
		}
		
		System.out.println("\nCOMPONENT SUMMARY");
		System.out.printf("1 component:                %4d\n", count(numComp2numAccesses, 1));
		System.out.printf(">1 component:               %4d\n", withMoreThanOneComp);
		System.out.printf("Total number of components: %4d\n", totalComponents);
		System.out.printf("Names with mixed success:   %4d\n", mixedSuccessAndError);
		
		if (details) {
			System.out.println(detailMsg.toString());
		}
	}
	private static void appendInfo(StringBuilder detailMsg, NodeInterface node) {
		TokenPair tokens = AstUtil.getFirstAndLastToken(node);
		detailMsg.append(String.format("    %4d %s\n", tokens.first.getLine(), AstUtil.toSourceString((Node)node)));
	}
}
