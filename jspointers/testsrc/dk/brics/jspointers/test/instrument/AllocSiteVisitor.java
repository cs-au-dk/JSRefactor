package dk.brics.jspointers.test.instrument;

public interface AllocSiteVisitor {
	void caseArguments(ArgumentsArrayAllocSite site);
	void caseArrayLiteral(ArrayLiteralAllocSite site);
	void caseFunction(FunctionAllocSite site);
	void caseFunctionProto(FunctionProtoAllocSite site);
	void caseNewExp(NewExpAllocSite site);
	void caseObjLiteral(ObjLiteralAllocSite site);
	void caseRegExp(RegExpAllocSite site);
}
