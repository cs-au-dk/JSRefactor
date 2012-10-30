package dk.brics.jsrefactoring.renameprty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.jspointers.lattice.values.ObjectValue;
import dk.brics.jsrefactoring.Master;
import dk.brics.jsrefactoring.family.FamilyDefinition;
import dk.brics.jsrefactoring.hosts.Host;
import dk.brics.jsrefactoring.hosts.ObjectHost;
import dk.brics.jsrefactoring.hosts.ScopeHost;
import dk.brics.jsrefactoring.nodes.DynamicPropertyExpAccess;
import dk.brics.jsrefactoring.nodes.AccessWithName;
import dk.brics.jsrefactoring.nodes.Access;
import dk.brics.jsrefactoring.nodes.AccessVariable;
import dk.brics.jsutil.CollectionUtil;

public class RenamingFamily implements FamilyDefinition<Host, Access> {
	
	private Master input;
	private String name;
	private ForInDynamicAccesses forinAccess;
	
	public RenamingFamily(Master input, String name,
            ForInDynamicAccesses forinAccess) {
        this.input = input;
        this.name = name;
        this.forinAccess = forinAccess;
    }

    @Override
	public Collection<? extends Host> getFamily(Access family) {
        if (family instanceof AccessWithName) {
            AccessWithName node = (AccessWithName)family;
            Set<ObjectHost> objects = ObjectHost.wrap(node.getBase(input, name));
            if (node instanceof AccessVariable) {
                AccessVariable vnode = (AccessVariable)node;
                List<ScopeHost> scopes = vnode.getSearchedScopes(input);
				return CollectionUtil.union(objects, Collections.singleton(scopes.get(scopes.size()-1)));
            } else {
                return objects;
            }
        } else {
            DynamicPropertyExpAccess node = (DynamicPropertyExpAccess)family;
            Set<ObjectValue> result = new HashSet<ObjectValue>();
            result.addAll(node.getBase(input, name));
            for (ObjectValue obj : forinAccess.getExp2forinObjs().getView(node.getExp())) {
                if (!input.isPropertyDefinitelyAbsent(obj, name)) {
                    result.add(obj);
                }
            }
            return ObjectHost.wrap(result);
        }
	}
}
