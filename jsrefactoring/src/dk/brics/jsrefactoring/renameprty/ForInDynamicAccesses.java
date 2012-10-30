package dk.brics.jsrefactoring.renameprty;

import java.util.HashSet;
import java.util.Set;

import dk.brics.jsparser.node.ADynamicPropertyExp;
import dk.brics.jsparser.node.AForInStmt;
import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsutil.MultiMap;

public class ForInDynamicAccesses {
    private Set<ADynamicPropertyExp> safeExps = new HashSet<ADynamicPropertyExp>();
    private Set<AForInStmt> safeForIns = new HashSet<AForInStmt>();
    private MultiMap<ADynamicPropertyExp,ObjectValue> exp2forinObjs = new MultiMap<ADynamicPropertyExp, ObjectValue>();
    
    
    public void setSafeExps(Set<ADynamicPropertyExp> safeExps) {
        this.safeExps = safeExps;
    }
    public Set<ADynamicPropertyExp> getSafeExps() {
        return safeExps;
    }
    public void setSafeForIns(Set<AForInStmt> safeForIns) {
        this.safeForIns = safeForIns;
    }
    public Set<AForInStmt> getSafeForIns() {
        return safeForIns;
    }
    public void setExp2forinObjs(MultiMap<ADynamicPropertyExp,ObjectValue> exp2forinObjs) {
        this.exp2forinObjs = exp2forinObjs;
    }
    public MultiMap<ADynamicPropertyExp,ObjectValue> getExp2forinObjs() {
        return exp2forinObjs;
    }
}