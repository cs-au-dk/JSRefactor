package dk.brics.jsrefactoring.family;

import java.util.Collection;

/**
 * Determines which objects are members of a family.
 * @author Asger
 *
 * @param <Obj>
 * @param <Family>
 */
public interface FamilyDefinition<Obj,Family> {
    Collection<? extends Obj> getFamily(Family family);
}
