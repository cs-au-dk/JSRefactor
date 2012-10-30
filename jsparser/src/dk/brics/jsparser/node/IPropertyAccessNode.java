package dk.brics.jsparser.node;

/**
 * Superinterface of {@link APropertyExp} and {@link ADynamicPropertyExp}.
 * @author asf
 *
 */
public interface IPropertyAccessNode extends ILeftHandSideNode {
    PExp getBase();
    void setBase(PExp exp);
}
