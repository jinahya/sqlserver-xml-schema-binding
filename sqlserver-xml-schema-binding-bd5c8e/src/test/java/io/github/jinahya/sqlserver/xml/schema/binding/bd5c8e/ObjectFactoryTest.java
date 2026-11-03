package io.github.jinahya.sqlserver.xml.schema.binding.bd5c8e;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;

class ObjectFactoryTest {

  @Test
  void newInstance() throws Exception {
    assertNotNull(JAXBContext.newInstance(ObjectFactory.class));
  }
}
