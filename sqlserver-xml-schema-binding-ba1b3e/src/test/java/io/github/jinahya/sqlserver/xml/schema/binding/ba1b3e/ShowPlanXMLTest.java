package io.github.jinahya.sqlserver.xml.schema.binding.ba1b3e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShowPlanXMLTest {

  // downloaded at build time into the root schemas/ (not on the classpath); tests run in the module directory
  private static final File SCHEMA = new File("../schemas/sqlserver/2004/07/showplan/sql2022/showplanxml.xsd");

  private static JAXBContext context;

  private static Schema schema;

  @BeforeAll
  static void setUp() throws Exception {
    context = JAXBContext.newInstance(ShowPlanXML.class);
    schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .newSchema(SCHEMA);
  }

  private static ShowPlanXML unmarshal(final StreamSource source) throws Exception {
    final Unmarshaller unmarshaller = context.createUnmarshaller();
    unmarshaller.setSchema(schema);
    return (ShowPlanXML) unmarshaller.unmarshal(source);
  }

  @Test
  void unmarshal_marshal_unmarshal() throws Exception {
    final ShowPlanXML plan;
    try (var stream = ShowPlanXMLTest.class.getResourceAsStream("/showplan-simple.xml")) {
      plan = unmarshal(new StreamSource(Objects.requireNonNull(stream)));
    }
    assertEquals("1.571", plan.getVersion());
    final var statement = plan.getBatchSequence().getBatch().get(0).getStatements().get(0)
        .getExternalDistributedComputationOrStmtSimpleOrStmtCond().get(0);
    final var simple = assertInstanceOf(StmtSimpleType.class, statement);
    assertEquals("SELECT 1", simple.getStatementText());

    final var writer = new StringWriter();
    final Marshaller marshaller = context.createMarshaller();
    marshaller.setSchema(schema);
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(plan, writer);

    final var reread = unmarshal(new StreamSource(new StringReader(writer.toString())));
    final var rereadSimple = (StmtSimpleType) reread.getBatchSequence().getBatch().get(0).getStatements().get(0)
        .getExternalDistributedComputationOrStmtSimpleOrStmtCond().get(0);
    assertEquals(simple.getStatementText(), rereadSimple.getStatementText());
    assertEquals(simple.getStatementSubTreeCost(), rereadSimple.getStatementSubTreeCost());
  }
}
