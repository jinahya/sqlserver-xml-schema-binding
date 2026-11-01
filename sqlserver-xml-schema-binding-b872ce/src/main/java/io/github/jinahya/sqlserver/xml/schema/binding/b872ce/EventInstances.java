package io.github.jinahya.sqlserver.xml.schema.binding.b872ce;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads and writes {@code EVENT_INSTANCE} documents, such as the {@code xml} returned by {@code EVENTDATA()}.
 *
 * <p>The schema declares {@code EVENT_INSTANCE} without a type, and the documents carry no {@code xsi:type}; the type
 * of their content, {@code EVENT_INSTANCE_<EventType>}, is named by their {@code EventType} child. So
 * {@link #unmarshal(Node)} picks the class from {@code EventType}, and {@link #marshal(EventInstance, Result)} writes
 * {@code EVENT_INSTANCE} without an {@code xsi:type}, as {@code EVENTDATA()} does.
 *
 * <p>A marshaller of a {@code JAXBContext} of the {@link ObjectFactory} knows the untyped {@code EVENT_INSTANCE}
 * element, so it writes an {@code xsi:type}. To marshal with your own settings (e.g.
 * {@link jakarta.xml.bind.Marshaller#JAXB_FRAGMENT}, to omit the XML declaration), use a marshaller of
 * {@link #context(Class)} with {@link #element(EventInstance)}:
 * <pre>{@code
 * var marshaller = EventInstances.context(eventInstance.getClass()).createMarshaller();
 * marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 * marshaller.marshal(EventInstances.element(eventInstance), result);
 * }</pre>
 */
public final class EventInstances {

  /**
   * The name of the document element, in no namespace.
   */
  public static final QName NAME = new QName(XMLConstants.NULL_NS_URI, "EVENT_INSTANCE");

  private static final String TYPE_NAME_PREFIX = "EVENT_INSTANCE_";

  // EventType (e.g. CREATE_TABLE) -> the class bound to the EVENT_INSTANCE_<EventType> type
  private static final Map<String, Class<? extends EventInstance>> TYPES;

  static {
    final var types = new HashMap<String, Class<? extends EventInstance>>();
    for (final var method : ObjectFactory.class.getMethods()) {
      final var type = method.getReturnType();
      final var xmlType = type.getAnnotation(XmlType.class);
      if (xmlType != null && xmlType.name().startsWith(TYPE_NAME_PREFIX)) {
        types.put(xmlType.name().substring(TYPE_NAME_PREFIX.length()), type.asSubclass(EventInstance.class));
      }
    }
    TYPES = Map.copyOf(types);
  }

  // a context of the type only, which doesn't know the (untyped) EVENT_INSTANCE element, so writes no xsi:type
  private static final Map<Class<?>, JAXBContext> CONTEXTS = new ConcurrentHashMap<>();

  /**
   * Returns a context of {@code type} only, whose marshallers write {@link #element(EventInstance)} without an
   * {@code xsi:type}, unlike a context of the {@link ObjectFactory}.
   *
   * @param type a class returned by {@link #typeOf(String)}.
   * @return the context, cached per type.
   * @throws JAXBException if failed to create the context.
   */
  public static JAXBContext context(final Class<? extends EventInstance> type) throws JAXBException {
    Objects.requireNonNull(type, "type is null");
    var context = CONTEXTS.get(type);
    if (context == null) {
      context = JAXBContext.newInstance(type);
      CONTEXTS.putIfAbsent(type, context);
    }
    return context;
  }

  /**
   * Returns the class bound to {@code EVENT_INSTANCE_<eventType>}.
   *
   * @param eventType the event type, e.g. {@code CREATE_TABLE}.
   * @return the class, e.g. {@link EVENTINSTANCECREATETABLE}.
   * @throws IllegalArgumentException if the schema has no type for {@code eventType}.
   */
  public static Class<? extends EventInstance> typeOf(final String eventType) {
    final var type = TYPES.get(Objects.requireNonNull(eventType, "eventType is null"));
    if (type == null) {
      throw new IllegalArgumentException("no type for event type: " + eventType);
    }
    return type;
  }

  /**
   * Returns the {@code EventType} of an {@code EVENT_INSTANCE}.
   *
   * @param eventInstance the {@code EVENT_INSTANCE} element, or a document of it.
   * @return the event type, e.g. {@code CREATE_TABLE}.
   * @throws IllegalArgumentException if {@code eventInstance} has no {@code EventType} child.
   */
  public static String eventTypeOf(final Node eventInstance) {
    final var element = element(eventInstance);
    for (var child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE && "EventType".equals(localName(child))) {
        return child.getTextContent().strip();
      }
    }
    throw new IllegalArgumentException("no EventType in " + localName(element));
  }

  /**
   * Unmarshals an {@code EVENT_INSTANCE} into the class of its {@code EventType}.
   *
   * @param eventInstance the {@code EVENT_INSTANCE} element, or a document of it.
   * @return an instance of {@link #typeOf(String) typeOf(eventTypeOf(eventInstance))}.
   * @throws IllegalArgumentException if the schema has no type for its {@code EventType}.
   * @throws JAXBException if failed to unmarshal.
   */
  public static EventInstance unmarshal(final Node eventInstance) throws JAXBException {
    final var element = element(eventInstance);
    final var type = typeOf(eventTypeOf(element));
    return context(type).createUnmarshaller().unmarshal(element, type).getValue();
  }

  /**
   * Unmarshals an {@code EVENT_INSTANCE} into the class of its {@code EventType}.
   *
   * @param eventInstance a source of the {@code EVENT_INSTANCE} document.
   * @return an instance of the class of its {@code EventType}.
   * @throws IllegalArgumentException if the schema has no type for its {@code EventType}.
   * @throws JAXBException if failed to unmarshal.
   * @throws TransformerException if failed to read {@code eventInstance}.
   * @see #unmarshal(Node)
   */
  public static EventInstance unmarshal(final Source eventInstance) throws JAXBException, TransformerException {
    final var result = new DOMResult();
    TransformerFactory.newInstance().newTransformer().transform(eventInstance, result);
    return unmarshal(result.getNode());
  }

  /**
   * Returns an event instance as an {@code EVENT_INSTANCE} element, for a marshaller of {@link #context(Class)}.
   *
   * @param eventInstance an instance of a class returned by {@link #typeOf(String)}.
   * @param <T> the type of {@code eventInstance}.
   * @return the element.
   */
  @SuppressWarnings("unchecked")
  public static <T extends EventInstance> JAXBElement<T> element(final T eventInstance) {
    final var type = (Class<T>) Objects.requireNonNull(eventInstance, "eventInstance is null").getClass();
    return new JAXBElement<>(NAME, type, eventInstance);
  }

  /**
   * Marshals an event instance as {@code EVENT_INSTANCE}, without an {@code xsi:type}, with the default settings of a
   * marshaller (so with an XML declaration); see the class documentation for your own settings.
   *
   * @param eventInstance an instance of a class returned by {@link #typeOf(String)}.
   * @param result the result to write to.
   * @throws JAXBException if failed to marshal.
   */
  public static void marshal(final EventInstance eventInstance, final Result result) throws JAXBException {
    final var element = element(eventInstance);
    context(element.getDeclaredType()).createMarshaller().marshal(element, result);
  }

  private static Element element(final Node eventInstance) {
    Objects.requireNonNull(eventInstance, "eventInstance is null");
    if (eventInstance instanceof Document document) {
      return document.getDocumentElement();
    }
    if (eventInstance instanceof Element element) {
      return element;
    }
    throw new IllegalArgumentException("neither a document nor an element: " + eventInstance);
  }

  // namespace-unaware nodes have no local name
  private static String localName(final Node node) {
    return node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
  }

  private EventInstances() {
    throw new AssertionError("instantiation is not allowed");
  }
}
