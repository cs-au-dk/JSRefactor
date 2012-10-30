package dk.brics.jsparser.node;

import java.util.LinkedList;


/**
 * Interface implemented by nodes that represent functions,
 * currently {@link AFunctionDeclStmt} and {@link AFunctionExp}.
 * 
 * @author max.schaefer@comlab.ox.ac.uk
 *
 */
public interface IFunction extends NodeInterface {
	Start getRoot();
	TFunction getFunction();
	Token getName();
	Token getLparen();
	LinkedList<Token> getParameters();
	Token getRparen();
	Token getLbrace();
	ABody getBody();
	void setBody(ABody body);
	Token getRbrace();
}
