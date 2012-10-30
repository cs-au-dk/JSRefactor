package dk.brics.jsrefactoring;

import java.util.Set;

import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.family.FamilyDefinition;
import dk.brics.jsrefactoring.nodes.AccessWithName;

public class PropertyNameFamily implements FamilyDefinition<ObjectValue, AccessWithName> {
    
    private Master input;
    private String name;
    
    public PropertyNameFamily(Master input, String name) {
        this.input = input;
        this.name = name;
    }
    
    @Override
    public Set<ObjectValue> getFamily(AccessWithName family) {
        return family.getBase(input, name); // TODO: getBase and getReceivers should be moved to this class
    }
    
}
