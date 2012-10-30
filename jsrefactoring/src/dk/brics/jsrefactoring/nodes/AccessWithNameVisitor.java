package dk.brics.jsrefactoring.nodes;

public interface AccessWithNameVisitor<Q,A> {
	A casePropertyInitializer(PropertyInitializerAccess node, Q arg);
	A casePropertyExp(PropertyExpAccess node, Q arg);
	A caseConstantInExp(ConstInExpAccess node, Q arg);
	A caseNameExp(NameExpAccess node, Q arg);
	A caseVarDecl(VarDeclAccess node, Q arg);
	A caseParameter(ParameterAccess node, Q arg);
	A caseCatch(CatchAccess node, Q arg);
	A caseFunctionDecl(FunctionDeclAccess node, Q arg);
	A caseFunctionExp(FunctionExpAccess node, Q arg);
}
