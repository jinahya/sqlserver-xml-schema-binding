package @module.name@;

import org.jvnet.jaxb.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb.locator.ObjectLocator;
import org.w3c.dom.Node;

/**
 * The equals strategy of the generated {@code equals(Object)} methods: {@link JAXBEqualsStrategy} (which compares
 * {@code JAXBElement}s by name, declared type and value, and lists element-wise), plus DOM nodes (wildcard content no
 * class binds) compared with {@link Node#isEqualNode(Node)}.
 *
 * <p>Generated from the root {@code src/templates/main/ValueEqualsStrategy.java}.
 *
 * @see ValueHashCodeStrategy
 */
public class ValueEqualsStrategy extends JAXBEqualsStrategy {

  @Override
  protected boolean equalsInternal(final ObjectLocator leftLocator, final ObjectLocator rightLocator,
                                   final Object lhs, final Object rhs) {
    if (lhs instanceof Node left && rhs instanceof Node right) {
      return left.isEqualNode(right);
    }
    return super.equalsInternal(leftLocator, rightLocator, lhs, rhs);
  }
}
