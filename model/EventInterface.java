package model;

import java.time.LocalDateTime;

/**
 * An interface for any Calendar Event and its methods.
 */
public interface EventInterface {
  /**
   * Get the event’s subject.
   *
   * @return subject string
   */
  String getSubject();

  /**
   * Get the event’s start date/time.
   *
   * @return start LocalDateTime
   */
  LocalDateTime getStartDateTime();

  /**
   * Get the event’s end date/time.
   *
   * @return end LocalDateTime
   */
  LocalDateTime getEndDateTime();

  /**
   * Get the event’s location.
   *
   * @return location string (empty if none)
   */
  String getLocation();
}