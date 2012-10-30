package dk.brics.jspointers.test.instrument;

public interface AllocSiteAnswerVisitor<A> {
	A caseArguments(ArgumentsArrayAllocSite site);
	A caseArrayLiteral(ArrayLiteralAllocSite site);
	A caseFunction(FunctionAllocSite site);
	A caseFunctionProto(FunctionProtoAllocSite site);
	A caseNewExp(NewExpAllocSite site);
	A caseObjLiteral(ObjLiteralAllocSite site);
	A caseRegExp(RegExpAllocSite site);
}
