package dk.brics.jsparser.node;

/**
 * An expression that can occur as a left-hand side. Note that this type of
 * expression does not <i>have</i> to occur as a left-hand side.
 * The expressions are: {@link ANameExp}, {@link APropertyExp}, {@link ADynamicPropertyExp}.
 */
public interface ILeftHandSideNode extends NodeInterface {
    Start getRoot();
}
