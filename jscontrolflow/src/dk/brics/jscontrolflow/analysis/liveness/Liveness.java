package dk.brics.jscontrolflow.analysis.liveness;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jsutil.MultiMap;

public class Liveness {
	
	private MultiMap<Block, Integer> block2after = new MultiMap<Block, Integer>();
	
	public Liveness(Function function) {
		HashSet<Block> unvisited = new HashSet<Block>(function.getBlocks());
		Set<Block> inqueue = new HashSet<Block>();
		LinkedList<Block> queue = new LinkedList<Block>();
		queue.add(function.getExceptionalExit());
		for (Block block : function.getBlocks()) {
			if (block.getSuccessors().isEmpty()) {
				queue.add(block);
				inqueue.add(block);
			}
		}
		// no need to consider exceptional edges, since temporary variables lose their value when
		// an exception gets thrown
		while (!unvisited.isEmpty()) {
			if (queue.isEmpty()) {
				Block next = unvisited.iterator().next();
				queue.add(next);
				inqueue.add(next);
			}
			while (!queue.isEmpty()) {
				Block block = queue.removeFirst();
				unvisited.remove(block);
				inqueue.remove(block);
				Set<Integer> live = block2after.getValues(block);
				for (Statement stm = block.getLast(); stm != null; stm=stm.getPrevious()) {
					live.removeAll(stm.getAssignedVariables());
					live.addAll(stm.getReadVariables());
				}
				for (Block pred : block.getPredecessors()) {
					if (block2after.addAll(pred, live)) {
						if (inqueue.add(pred)) {
							queue.add(pred);
						}
					}
				}
			}
		}
	}
	
	public Set<Integer> getLiveBefore(Block block) {
		Set<Integer> result = new HashSet<Integer>();
		for (Block pred : block.getPredecessors()) {
			result.addAll(getLiveAfter(pred));
		}
		return result;
	}
	
	public Set<Integer> getLiveAfter(Block block) {
		return block2after.getView(block);
	}
	
}
