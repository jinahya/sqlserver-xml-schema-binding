package io.github.jinahya.sqlserver.xml.schema.binding.b73cae;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

/**
 * Round-trips the Instructions of ProductModelID 7 (HL Touring Frame) of the Production.ProductModel table of the
 * AdventureWorks sample database; samples/root/ holds every non-null Instructions of the table.
 */
class RootTest {

  private static <T> List<T> elements(final List<?> content, final Class<T> type) {
    return content.stream()
        .filter(JAXBElement.class::isInstance)
        .map(e -> ((JAXBElement<?>) e).getValue())
        .filter(type::isInstance)
        .map(type::cast)
        .toList();
  }

  private static Root unmarshal(final JAXBContext context, final StreamSource source) throws Exception {
    return (Root) context.createUnmarshaller().unmarshal(source);
  }

  @Test
  void unmarshal_marshal_unmarshal() throws Exception {
    final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
    final Root root;
    try (var stream = RootTest.class.getResourceAsStream("/samples/root/ProductModel-7.xml")) {
      root = unmarshal(context, new StreamSource(Objects.requireNonNull(stream)));
    }
    assertTrue(root.getContent().stream().anyMatch(c -> c instanceof String s && s.contains("HL Touring Frame")));
    final List<Root.Location> locations = elements(root.getContent(), Root.Location.class);
    assertEquals(6, locations.size());
    final Root.Location first = locations.get(0);
    assertEquals(BigInteger.valueOf(10), first.getLocationID());
    assertEquals(new BigDecimal("0.5"), first.getSetupHours());
    assertEquals(new BigDecimal("3"), first.getMachineHours());
    assertEquals(new BigDecimal("2.5"), first.getLaborHours());
    assertEquals(new BigDecimal("100"), first.getLotSize());
    assertNull(locations.get(2).getMachineHours());
    final List<StepType> steps = locations.stream()
        .flatMap(l -> elements(l.getContent(), StepType.class).stream())
        .toList();
    assertEquals(28, steps.size());
    final List<String> tools = steps.stream()
        .flatMap(s -> s.getContent().stream())
        .filter(c -> c instanceof JAXBElement<?> e && e.getName().getLocalPart().equals("tool"))
        .map(c -> (String) ((JAXBElement<?>) c).getValue())
        .toList();
    assertEquals(6, tools.size());
    assertEquals("T-85A framing tool", tools.get(0));

    final StringWriter writer = new StringWriter();
    final Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(root, writer);
    assertEquals(root, unmarshal(context, new StreamSource(new StringReader(writer.toString()))));
  }
}
