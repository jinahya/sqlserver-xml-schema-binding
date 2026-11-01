package @module.name@;

import java.util.Objects;
import org.jvnet.jaxb.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb.locator.ObjectLocator;
import org.w3c.dom.Node;

/**
 * The hash code strategy of the generated {@code hashCode()} methods, consistent with {@link ValueEqualsStrategy}:
 * {@link JAXBHashCodeStrategy}, plus DOM nodes hashed from what {@link Node#isEqualNode(Node)} compares.
 *
 * <p>Generated from the root {@code src/templates/main/ValueHashCodeStrategy.java}.
 *
 * @see ValueEqualsStrategy
 */
public class ValueHashCodeStrategy extends JAXBHashCodeStrategy {

  // isEqualNode compares these, the attributes (in any order) and the children (in order)
  static int hash(final Node node) {
    var hash = Objects.hash(node.getNodeType(), node.getNodeName(), node.getLocalName(), node.getNamespaceURI(),
                            node.getPrefix(), node.getNodeValue());
    final var attributes = node.getAttributes();
    if (attributes != null) {
      var sum = 0;
      for (int i = 0; i < attributes.getLength(); i++) {
        sum += hash(attributes.item(i));
      }
      hash = 31 * hash + sum;
    }
    for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      hash = 31 * hash + hash(child);
    }
    return hash;
  }

  @Override
  protected int hashCodeInternal(final ObjectLocator locator, final int hashCode, final Object value) {
    if (value instanceof Node node) {
      return super.hashCodeInternal(locator, hashCode, hash(node));
    }
    return super.hashCodeInternal(locator, hashCode, value);
  }
}
