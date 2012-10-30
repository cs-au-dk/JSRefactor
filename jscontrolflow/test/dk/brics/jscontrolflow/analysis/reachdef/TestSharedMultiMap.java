package dk.brics.jscontrolflow.analysis.reachdef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static dk.brics.jscontrolflow.analysis.reachdef.SharedMultiMapUtil.union;
import static org.junit.Assert.*;

public class TestSharedMultiMap {
	
	Map<String,Set<String>> map1 = new HashMap<String, Set<String>>();
	Map<String,Set<String>> map2 = new HashMap<String, Set<String>>();
	Map<String,Set<String>> map3 = new HashMap<String, Set<String>>();
	
	private Set<String> set(String ... strings) {
		return new HashSet<String>(Arrays.asList(strings));
	}
	
	@Test
	public void union1() {
		map1.put("keyA", set("A","B","C"));
		map2.put("keyA", set("B","D"));
		
		map3 = union(map1, map2);
		
		assertEquals(map3.get("keyA"), set("A","B","C","D"));
	}
	@Test
	public void union2() {
		map1.put("keyA", set("A","B","C"));
		map2.put("keyB", set("B","D"));
		
		map3 = union(map1, map2);

		assertEquals(map3.get("keyA"), set("A","B","C"));
		assertEquals(map3.get("keyB"), set("B","D"));
	}
	@Test
	public void union3() {
		map1.put("keyA", set("A","B","C"));
		map1.put("keyC", set("X"));
		map2.put("keyB", set("B","D"));
		map2.put("keyC", set());
		
		map3 = union(map1, map2);

		assertEquals(map3.get("keyA"), set("A","B","C"));
		assertEquals(map3.get("keyB"), set("B","D"));
		assertEquals(map3.get("keyC"), set("X"));
	}
}
