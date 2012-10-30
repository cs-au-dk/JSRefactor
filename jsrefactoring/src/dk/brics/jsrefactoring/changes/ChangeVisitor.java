package dk.brics.jsrefactoring.changes;


public interface ChangeVisitor<Q,A> {
	A caseInsertExpIntoArglist(InsertExpIntoArglist change, Q arg);
	A caseInsertStmtIntoBlock(InsertStmtIntoBlock change, Q arg);
	A caseRemoveStmtFromBlock(RemoveStmtFromBlock change, Q arg);
	A caseRenamePropertyNameNode(RenamePropertyNameNode change, Q arg);
	A caseReplaceExp(ReplaceExp change, Q arg);
	A caseRemoveVarDecl(RemoveVarDecl change, Q arg);
	A caseSplitVarDeclStmt(SplitVarDeclStmt change, Q arg);
	A caseInsertStmtIntoScript(InsertStmtIntoScript change, Q arg);
}
