package io.github.jinahya.sqlserver.xml.schema.binding.b0fd0e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jinahya.sqlserver.xml.schema.binding.b4347e.Maintenance;
import io.github.jinahya.sqlserver.xml.schema.binding.b4347e.Warranty;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class ProductDescriptionTest {

  private static final String WM = "http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ProductModelWarrAndMain";

  private static List<Element> elements(final Features features) {
    return features.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .toList();
  }

  @Test
  void unmarshal_marshal_unmarshal() throws Exception {
    final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
    final ProductDescription description;
    try (var stream = ProductDescriptionTest.class.getResourceAsStream("/product-description.xml")) {
      description = context.createUnmarshaller()
          .unmarshal(new StreamSource(Objects.requireNonNull(stream)), ProductDescription.class)
          .getValue();
    }
    assertEquals("19", description.getProductModelID());
    final Features features = description.getFeatures().get(0);
    assertTrue(features.getContent().stream().anyMatch(c -> c instanceof String s && s.contains("highlights")));
    final List<Element> elements = elements(features);
    assertEquals(2, elements.size());
    assertEquals(WM, elements.get(0).getNamespaceURI());
    assertEquals("Warranty", elements.get(0).getLocalName());
    assertEquals("Maintenance", elements.get(1).getLocalName());

    // the DOM elements are bound with the ProductModelWarrAndMain module
    final JAXBContext wm = JAXBContext.newInstance(
        io.github.jinahya.sqlserver.xml.schema.binding.b4347e.ObjectFactory.class);
    final Warranty warranty = (Warranty) wm.createUnmarshaller().unmarshal(elements.get(0));
    assertEquals("3 years", warranty.getWarrantyPeriod());
    final Maintenance maintenance = (Maintenance) wm.createUnmarshaller().unmarshal(elements.get(1));
    assertEquals("10 years", maintenance.getNoOfYears());

    final StringWriter writer = new StringWriter();
    final Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(new ObjectFactory().createProductDescription(description), writer);
    final ProductDescription reread = context.createUnmarshaller()
        .unmarshal(new StreamSource(new StringReader(writer.toString())), ProductDescription.class)
        .getValue();
    final List<Element> rereadElements = elements(reread.getFeatures().get(0));
    assertEquals(2, rereadElements.size());
    assertEquals("3 years",
        ((Warranty) wm.createUnmarshaller().unmarshal(rereadElements.get(0))).getWarrantyPeriod());
  }
}
