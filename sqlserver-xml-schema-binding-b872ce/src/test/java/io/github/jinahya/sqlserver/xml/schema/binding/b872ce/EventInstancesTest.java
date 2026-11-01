package io.github.jinahya.sqlserver.xml.schema.binding.b872ce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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

  private static String marshal(final EventInstance eventInstance) throws Exception {
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

  @Test
  void typeOf_everyTypeImplementsEventInstance() throws Exception {
    for (final var method : ObjectFactory.class.getMethods()) {
      final var xmlType = method.getReturnType().getAnnotation(XmlType.class);
      if (xmlType != null && xmlType.name().startsWith("EVENT_INSTANCE_")) {
        assertTrue(EventInstance.class.isAssignableFrom(method.getReturnType()), xmlType.name());
      }
    }
  }

  // the event types of AdventureWorks' dbo.DatabaseLog, one (anonymized) row each
  @ParameterizedTest
  @ValueSource(strings = {
      "ALTER_AUTHORIZATION_DATABASE", "ALTER_TABLE", "ALTER_XML_SCHEMA_COLLECTION", "CREATE_EXTENDED_PROPERTY",
      "CREATE_FULLTEXT_CATALOG", "CREATE_FULLTEXT_INDEX", "CREATE_FUNCTION", "CREATE_INDEX", "CREATE_PROCEDURE",
      "CREATE_SCHEMA", "CREATE_TABLE", "CREATE_TRIGGER", "CREATE_TYPE", "CREATE_VIEW", "CREATE_XML_INDEX",
      "CREATE_XML_SCHEMA_COLLECTION", "UPDATE_STATISTICS"})
  void unmarshal_marshal_unmarshal(final String eventType) throws Exception {
    final Document document;
    try (var stream = EventInstancesTest.class.getResourceAsStream("/eventdata/" + eventType + ".xml")) {
      document = parse(Objects.requireNonNull(stream));
    }
    assertEquals(eventType, EventInstances.eventTypeOf(document));

    final var eventInstance = EventInstances.unmarshal(document);
    assertInstanceOf(EventInstances.typeOf(eventType), eventInstance);
    assertEquals(eventType, eventInstance.getEventType());
    assertNotNull(eventInstance.getPostTime());
    assertTrue(eventInstance.getSPID() > 0);

    // written as EVENTDATA() does: EVENT_INSTANCE in no namespace, without xsi:type
    final var xml = marshal(eventInstance);
    assertTrue(xml.contains("<EVENT_INSTANCE>"), xml);
    // no xsi:type and no xmlns on the root, where JAXB declares namespaces; checked on the parsed document, as some
    // CommandText (e.g. of XML schema collections) contains "xmlns" as text
    final var root = parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    assertNull(root.getNamespaceURI(), xml);
    assertEquals(0, root.getAttributes().getLength(), xml);
    validate(xml, eventType);

    final var reread = EventInstances.unmarshal(new StreamSource(new StringReader(xml)));
    assertInstanceOf(EventInstances.typeOf(eventType), reread);
    assertEquals(xml, marshal(reread));
  }

  @Test
  void marshal_withOwnMarshaller_fragment() throws Exception {
    final EventInstance eventInstance;
    try (var stream = EventInstancesTest.class.getResourceAsStream("/eventdata/CREATE_TABLE.xml")) {
      eventInstance = EventInstances.unmarshal(new StreamSource(Objects.requireNonNull(stream)));
    }
    assertTrue(marshal(eventInstance).startsWith("<?xml"));

    final var marshaller = EventInstances.context(eventInstance.getClass()).createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    final var writer = new StringWriter();
    marshaller.marshal(EventInstances.element(eventInstance), writer);
    final var xml = writer.toString();
    assertTrue(xml.startsWith("<EVENT_INSTANCE>"), xml);
    assertFalse(xml.contains("xsi:type"), xml);
  }

  // a context of the ObjectFactory knows the untyped EVENT_INSTANCE element, so writes an xsi:type
  @Test
  void marshal_objectFactoryContext_writesXsiType() throws Exception {
    final EventInstance eventInstance;
    try (var stream = EventInstancesTest.class.getResourceAsStream("/eventdata/CREATE_TABLE.xml")) {
      eventInstance = EventInstances.unmarshal(new StreamSource(Objects.requireNonNull(stream)));
    }
    final var writer = new StringWriter();
    JAXBContext.newInstance(ObjectFactory.class).createMarshaller()
        .marshal(EventInstances.element(eventInstance), writer);
    assertTrue(writer.toString().contains("xsi:type"), writer.toString());
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
