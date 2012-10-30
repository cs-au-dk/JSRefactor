package dk.brics.jspointers.test.instrument;

public class AbstractAllocSiteVisitor implements AllocSiteVisitor {

    @Override
    public void caseArguments(ArgumentsArrayAllocSite site) {
    }

    @Override
    public void caseArrayLiteral(ArrayLiteralAllocSite site) {
    }

    @Override
    public void caseFunction(FunctionAllocSite site) {
    }

    @Override
    public void caseFunctionProto(FunctionProtoAllocSite site) {
    }

    @Override
    public void caseNewExp(NewExpAllocSite site) {
    }

    @Override
    public void caseObjLiteral(ObjLiteralAllocSite site) {
    }

    @Override
    public void caseRegExp(RegExpAllocSite site) {
    }
    
}
