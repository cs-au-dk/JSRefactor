package dk.brics.jscontrolflow.statements;

import java.util.List;

public abstract class InvokeStatement extends Assignment {
    public InvokeStatement(int resultVar) {
        super(resultVar);
    }

    public abstract List<Integer> getArguments();
}
