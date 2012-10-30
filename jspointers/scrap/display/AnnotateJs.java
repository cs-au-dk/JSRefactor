package dk.brics.jspointers.display;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import dk.brics.jspointers.dataflow.DataflowGraph;
import dk.brics.jspointers.dataflow.FlowNode;
import dk.brics.jspointers.dataflow.IInvocation;
import dk.brics.jspointers.lattice.contexts.NullContext;
import dk.brics.jspointers.lattice.keys.Key;
import dk.brics.jspointers.lattice.values.ApplyFunctionValue;
import dk.brics.jspointers.lattice.values.CallFunctionValue;
import dk.brics.jspointers.lattice.values.FunctionValue;
import dk.brics.jspointers.lattice.values.NativeFunctionValue;
import dk.brics.jspointers.lattice.values.UserFunctionValue;
import dk.brics.jspointers.lattice.values.Value;
import dk.brics.jspointers.solver.AnalysisResult;
import dk.brics.jsutil.MultiMap;
import dk.brics.jsutil.StringUtil;
import dk.brics.tajs.flowgraph.Function;
import dk.brics.tajs.flowgraph.nodes.CallNode;

public class AnnotateJs {
	public static void annotateJavaScriptFile(String inputJsfile, String outputJsfile, DataflowGraph dataflow, AnalysisResult<Key,Set<Value>> result) throws IOException {
        MultiMap<Integer,IInvocation> linenr2callsites = new MultiMap<Integer, IInvocation>();
        for (FlowNode node : dataflow.getNodes()) {
        	if (node instanceof IInvocation) {
        		IInvocation invoke = (IInvocation)node;
        		CallNode call = dataflow.getCalls().getBackward(invoke);
	            if (call.getSourceLocation().getFileName().equals(inputJsfile)) {
	                linenr2callsites.add(call.getSourceLocation().getLineNumber(), invoke);
	            }
        	}
        }
        MultiMap<Key,Value> map = DisplayUtil.makeContextInsensitive(result);
        result = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        DisplayUtil util = new DisplayUtil(map, dataflow);
        try {
            reader = new BufferedReader(new FileReader(inputJsfile));
            writer = new BufferedWriter(new FileWriter(outputJsfile));
            int linenr = 1;
            for (String line = reader.readLine(); line!=null; line=reader.readLine()) {
                writer.append(line);
                for (IInvocation callsite : linenr2callsites.getView(linenr)) {
                    writer.append(" // CallTargets[");
                    boolean first=true;
                    for (Value val : util.getFunctionArgs(callsite)) {
                    	if (!(val instanceof FunctionValue))
                    		continue;
                    	FunctionValue fval = (FunctionValue)val;
                    	if (!first)
                            writer.append(", ");
                        else
                            first = false;
                    	writer.append(getFunctionName(fval, dataflow));
                		if (fval instanceof CallFunctionValue || fval instanceof ApplyFunctionValue) {
                			// print called function (only one level, not recursively)
                			writer.append("->{");
                			boolean innerFirst=true;
                			for (Value subvalue : map.getView(callsite.getBase().getKey(NullContext.Instance))) {
                				if (!(subvalue instanceof FunctionValue))
                					continue;
                				FunctionValue subf = (FunctionValue)subvalue;
                				if (!innerFirst)
                					writer.append(",");
                				else
                					innerFirst=false;
                				writer.append(getFunctionName(subf, dataflow));
                			}
                			writer.append("}");
                		}
                    }
                    writer.append("]");
                }
                writer.append("\n");
                linenr++;
            }
        } finally {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        }
    }
	private static String getFunctionName(FunctionValue value, DataflowGraph dataflow) {
		if (value instanceof UserFunctionValue) {
			UserFunctionValue uf = (UserFunctionValue)value;
        	Function callee = uf.getFunction();
        	if (dataflow.getHarnessFunctions().contains(callee)) {
        		if (callee.getName() != null)
        			return "native:" + callee.getName();
        		else
        			return "native:" + StringUtil.removePathInfo(callee.getSourceLocation().getFileName()) + ":" + callee.getSourceLocation().getLineNumber();
        	} else {
        		return ""+callee.getSourceLocation().getLineNumber();
        	}
		} else {
			NativeFunctionValue nf = (NativeFunctionValue)value;
			return "native:" + nf.getPrettyName();
		}
	}
}
