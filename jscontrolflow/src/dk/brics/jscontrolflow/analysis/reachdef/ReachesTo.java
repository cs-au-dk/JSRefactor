package dk.brics.jscontrolflow.analysis.reachdef;

import java.util.Set;

import dk.brics.jscontrolflow.Block;
import dk.brics.jscontrolflow.Function;
import dk.brics.jscontrolflow.Statement;
import dk.brics.jscontrolflow.statements.Assignment;
import dk.brics.jsutil.MultiMap;

/**
 * Builds an inverse map of the reaching definitions, to allow queries on form:
 * <i>What statements may read the variable I am assigning to here?</i>.
 */
public class ReachesTo {
    private MultiMap<VariableDefinition, Statement> map = new MultiMap<VariableDefinition, Statement>();

    public ReachesTo(ReachingDefinitions reachingDefs) {
        Function function = reachingDefs.getFunction();
        for (Block block : function.getBlocks()) {
            for (Statement stm : block.getStatements()) {
                for (int var : stm.getReadVariables()) {
                    for (VariableDefinition def : reachingDefs.getReachingDefinitions(stm, var)) {
                        map.add(def, stm);
                    }
                }
            }
        }
    }

    public Set<Statement> getReachesTo(VariableDefinition def) {
        return map.getView(def);
    }
    public Set<Statement> getReachesTo(Assignment assign) {
        return map.getView(new StatementVariableDefinition(assign));
    }
}
