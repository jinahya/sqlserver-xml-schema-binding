package io.github.jinahya.sqlserver.xml.schema.binding.b872ce;

/**
 * The envelope every {@code EVENT_INSTANCE_<EventType>} type begins with, implemented by all their generated classes.
 *
 * @see EventInstances
 */
public interface EventInstance {

  /**
   * Returns the {@code EventType}, e.g. {@code CREATE_TABLE}.
   *
   * @return the event type.
   */
  String getEventType();

  /**
   * Returns the {@code PostTime}, e.g. {@code 2025-11-14T13:01:06.930}; an {@code xs:string} in the schema.
   *
   * @return the post time.
   */
  String getPostTime();

  /**
   * Returns the {@code SPID}.
   *
   * @return the server process id.
   */
  int getSPID();
}
