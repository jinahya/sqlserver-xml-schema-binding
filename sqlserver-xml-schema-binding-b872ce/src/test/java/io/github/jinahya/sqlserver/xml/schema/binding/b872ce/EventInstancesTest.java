package io.github.jinahya.sqlserver.xml.schema.binding.b872ce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EventInstancesTest {

  // downloaded at build time into the root schemas/ (not on the classpath); tests run in the module directory
  private static final File SCHEMA = new File("../schemas/sqlserver/2006/11/eventdata/events.xsd");

  private static Schema schema;

  @BeforeAll
  static void setUp() throws Exception {
    schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(SCHEMA);
  }

  private static Document parse(final InputStream stream) throws Exception {
    final var factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(stream);
  }

  // EVENT_INSTANCE is untyped, so a document is validated against its EVENT_INSTANCE_<EventType> via xsi:type
  private static void validate(final String xml, final String eventType) throws Exception {
    final var typed = xml.replaceFirst(
        "<EVENT_INSTANCE>",
        "<EVENT_INSTANCE xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
        + "\" xsi:type=\"EVENT_INSTANCE_" + eventType + "\">");
    schema.newValidator().validate(new StreamSource(new StringReader(typed)));
  }

  private static String marshal(final Object eventInstance) throws Exception {
    final var writer = new StringWriter();
    EventInstances.marshal(eventInstance, new StreamResult(writer));
    return writer.toString();
  }

  @Test
  void typeOf_everyEventInstanceTypeOfTheSchema() throws Exception {
    final var names = new TreeSet<String>();
    final Document xsd;
    try (var stream = new FileInputStream(SCHEMA)) {
      xsd = parse(stream);
    }
    final var types = xsd.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
    for (int i = 0; i < types.getLength(); i++) {
      final var name = ((Element) types.item(i)).getAttribute("name");
      if (name.startsWith("EVENT_INSTANCE_")) {
        names.add(name);
      }
    }
    assertFalse(names.isEmpty());
    for (final var name : names) {
      final var type = EventInstances.typeOf(name.substring("EVENT_INSTANCE_".length()));
      assertEquals(name, type.getAnnotation(XmlType.class).name());
    }
  }

  @Test
  void typeOf_unknown() {
    assertThrows(IllegalArgumentException.class, () -> EventInstances.typeOf("NO_SUCH_EVENT"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"CREATE_TABLE", "ALTER_TABLE", "CREATE_EXTENDED_PROPERTY"})
  void unmarshal_marshal_unmarshal(final String eventType) throws Exception {
    final Document document;
    try (var stream = EventInstancesTest.class.getResourceAsStream("/eventdata/" + eventType + ".xml")) {
      document = parse(Objects.requireNonNull(stream));
    }
    assertEquals(eventType, EventInstances.eventTypeOf(document));

    final var eventInstance = EventInstances.unmarshal(document);
    assertInstanceOf(EventInstances.typeOf(eventType), eventInstance);

    // written as EVENTDATA() does: EVENT_INSTANCE in no namespace, without xsi:type
    final var xml = marshal(eventInstance);
    assertTrue(xml.contains("<EVENT_INSTANCE>"), xml);
    assertFalse(xml.contains("xsi:type"), xml);
    assertFalse(xml.contains("xmlns"), xml);
    validate(xml, eventType);

    final var reread = EventInstances.unmarshal(new StreamSource(new StringReader(xml)));
    assertInstanceOf(EventInstances.typeOf(eventType), reread);
    assertEquals(xml, marshal(reread));
  }

  @Test
  void unmarshal_createTable() throws Exception {
    final Object eventInstance;
    try (var stream = EventInstancesTest.class.getResourceAsStream("/eventdata/CREATE_TABLE.xml")) {
      eventInstance = EventInstances.unmarshal(new StreamSource(Objects.requireNonNull(stream)));
    }
    final var createTable = assertInstanceOf(EVENTINSTANCECREATETABLE.class, eventInstance);
    assertEquals("CREATE_TABLE", createTable.getEventType());
    assertEquals("Sample", createTable.getObjectName());
    assertEquals("TABLE", createTable.getObjectType());
    assertTrue(createTable.getTSQLCommand().getCommandText().startsWith("CREATE TABLE [dbo].[Sample]"));
  }
}
