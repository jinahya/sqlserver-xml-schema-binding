package @module.name@;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBIntrospector;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks the generated classes have value {@code equals(Object)} and {@code hashCode()}.
 *
 * <p>Generated from the root {@code src/templates/test/ValueEqualityTest.java}.
 */
class ValueEqualityTest {

  // tests run in the module directory
  private static final Path SAMPLES = Path.of("src", "test", "resources");

  // the no-arg create methods of the ObjectFactory, of the classes of this module
  static Stream<Method> creators() {
    return Arrays.stream(ObjectFactory.class.getDeclaredMethods())
        .filter(m -> m.getParameterCount() == 0 && m.getName().startsWith("create"))
        .filter(m -> m.getReturnType().getPackageName().equals(ObjectFactory.class.getPackageName()));
  }

  @ParameterizedTest
  @MethodSource("creators")
  void equals_hashCode_newInstances(final Method creator) throws Exception {
    final var type = creator.getReturnType();
    assertEquals(type, type.getMethod("equals", Object.class).getDeclaringClass());
    assertEquals(type, type.getMethod("hashCode").getDeclaringClass());
    final var factory = new ObjectFactory();
    final var left = creator.invoke(factory);
    final var right = creator.invoke(factory);
    assertNotSame(left, right);
    assertEquals(left, right);
    assertEquals(left.hashCode(), right.hashCode());
  }

  // every *.xml under src/test/resources, unmarshalled twice
  @TestFactory
  Stream<DynamicTest> equals_hashCode_unmarshalledTwice() throws Exception {
    if (!Files.isDirectory(SAMPLES)) {
      return Stream.empty();
    }
    final var context = JAXBContext.newInstance(ObjectFactory.class);
    final var samples = Files.walk(SAMPLES).filter(p -> p.toString().endsWith(".xml")).sorted().toList();
    return samples.stream().map(sample -> DynamicTest.dynamicTest(SAMPLES.relativize(sample).toString(), () -> {
      final var left = JAXBIntrospector.getValue(context.createUnmarshaller().unmarshal(sample.toFile()));
      final var right = JAXBIntrospector.getValue(context.createUnmarshaller().unmarshal(sample.toFile()));
      // e.g. an untyped document element, unmarshalled as DOM
      assumeTrue(left.getClass().getPackageName().equals(ObjectFactory.class.getPackageName()),
                 () -> "not a class of this module: " + left.getClass());
      assertNotSame(left, right);
      assertEquals(left, right);
      assertEquals(left.hashCode(), right.hashCode());
      assertNotEquals(left.getClass().getDeclaredConstructor().newInstance(), left);
    }));
  }
}
