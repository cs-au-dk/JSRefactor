package dk.brics.jsrefactoring.nodes;

public abstract class AbstractAccessVisitor<Q, A> implements AccessVisitor<Q, A> {
	public A defaultAccess(Access node, Q arg) {
		return null;
	}
	
	public A defaultAccessWithName(AccessWithName node, Q arg) {
		return defaultAccess(node, arg);
	}
	
	public A defaultAccessVariable(AccessVariable node, Q arg) {
		return defaultAccessWithName(node, arg);
	}
	
	public A defaultAccessWithoutName(AccessWithoutName node, Q arg) {
		return defaultAccess(node, arg);
	}

	@Override
	public A casePropertyInitializer(PropertyInitializerAccess node, Q arg) {
		return defaultAccessWithName(node, arg);
	}

	@Override
	public A casePropertyExp(PropertyExpAccess node, Q arg) {
		return defaultAccessWithName(node, arg);
	}

	@Override
	public A caseConstantInExp(ConstInExpAccess node, Q arg) {
		return defaultAccessWithName(node, arg);
	}

	@Override
	public A caseNameExp(NameExpAccess node, Q arg) {
		return defaultAccessVariable(node, arg);
	}

	@Override
	public A caseVarDecl(VarDeclAccess node, Q arg) {
		return defaultAccessVariable(node, arg);
	}

	@Override
	public A caseParameter(ParameterAccess node, Q arg) {
		return defaultAccessVariable(node, arg);
	}

	@Override
	public A caseCatch(CatchAccess node, Q arg) {
		return defaultAccessVariable(node, arg);
	}
	
	@Override
	public A caseFunctionDecl(FunctionDeclAccess node, Q arg) {
		return defaultAccessVariable(node, arg);
	}
	public A caseFunctionExp(FunctionExpAccess node, Q arg) {
	  return defaultAccessVariable(node, arg);
	}

	@Override
	public A caseDynamicProperty(DynamicPropertyExpAccess node, Q arg) {
		return defaultAccessWithoutName(node, arg);
	}
	
}
