package dk.brics.jsrefactoring.family;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dk.brics.jsutil.MultiMap;

/**
 * Given some <i>families</i> of <i>objects</i>, computes a set of families F and a set of objects O satisfying:
 * <ul>
 * <li>O is the union of all the sets in F.
 * <li>For each family, either all its members are in O, or none of its members are in O
 * <li>F contains {f<sub>1</sub>,f<sub>2</sub>,...}
 * <li>O contains {o<sub>1</sub>,o<sub>2</sub>,...}
 * </ul>
 * where f<sub>i</sub> and o<sub>i</sub> are some given <i>initial</i> values.
 * <p/>
 * This problem is isomorphic to reachability in a bipartite graph, with Obj and Family being
 * the two vertex classes, and family membership being the edges.
 * 
 * @author Asger
 *
 * @param <Obj> object type
 * @param <Family> family type
 */
public class FamilyClosure<Obj,Family> {
    
    private Set<Family> affectedFamilies = new HashSet<Family>();
    private Set<Obj> affectedObjects = new HashSet<Obj>();
    
    public FamilyClosure(Set<Family> affectedFamilies, Set<Obj> affectedObjects) {
        this.affectedFamilies = affectedFamilies;
        this.affectedObjects = affectedObjects;
    }
    
    public Set<Family> getAffectedFamilies() {
        return affectedFamilies;
    }
    public Set<Obj> getAffectedObjects() {
        return affectedObjects;
    }
    
    /**
     * @see FamilyClosure
     */
    public static <Obj,Family> FamilyClosure<Obj,Family> compute(
                FamilyDefinition<Obj,Family> def,
                Collection<? extends Family> allFamilies,
                Collection<? extends Family> initialFamilies,
                Collection<? extends Obj> initialObjects) {
        
        MultiMap<Obj,Family> obj2family = new MultiMap<Obj,Family>();
        MultiMap<Family,Obj> family2obj = new MultiMap<Family,Obj>();
        
        for (Family family : allFamilies) {
            for (Obj obj : def.getFamily(family)) {
                obj2family.add(obj, family);
                family2obj.add(family, obj);
            }
        }
        
        Set<Family> familyqueue = new HashSet<Family>();
        Set<Obj> objectqueue = new HashSet<Obj>();
        Set<Family> affectedFamilies = new HashSet<Family>();
        Set<Obj> affectedObjects = new HashSet<Obj>();
        
        familyqueue.addAll(initialFamilies);
        objectqueue.addAll(initialObjects);
        
        affectedFamilies.addAll(familyqueue);
        affectedObjects.addAll(objectqueue);
        
        while (!familyqueue.isEmpty() || !objectqueue.isEmpty()) {
            for (Family familty : familyqueue) {
                for (Obj obj : family2obj.getView(familty)) {
                    if (affectedObjects.add(obj)) {
                        objectqueue.add(obj);
                    }
                }
            }
            familyqueue.clear();
            for (Obj obj : objectqueue) {
                for (Family family : obj2family.getView(obj)) {
                    if (affectedFamilies.add(family)) {
                        familyqueue.add(family);
                    }
                }
            }
            objectqueue.clear();
        }
        
        return new FamilyClosure<Obj, Family>(affectedFamilies, affectedObjects);
    }
    
    
}
