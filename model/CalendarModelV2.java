package model;

import java.util.List;
import java.util.Set;

/**
 * New Model interface that extends CalendarModel with multi-calendar and copying features.
 * Uses interface composition to add new methods without modifying the original interface.
 */
public interface CalendarModelV2 extends CalendarModel {

  /**
   * Create a new calendar with the specified name and timezone.
   *
   * @param name     the unique name for the calendar
   * @param timezone the timezone in IANA format (e.g., "America/New_York")
   * @throws IllegalArgumentException if name is not unique or timezone is invalid
   */
  void createCalendar(String name, String timezone);

  /**
   * Edit a property of an existing calendar.
   *
   * @param name     the name of the calendar to edit
   * @param property the property to change ("name" or "timezone")
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if calendar not found, property invalid, or value invalid
   */
  void editCalendarProperty(String name, String property, String newValue);

  /**
   * Set the active calendar for subsequent operations.
   *
   * @param name the name of the calendar to use
   * @throws IllegalArgumentException if calendar not found
   */
  void useCalendar(String name);

  /**
   * Get the name of the currently active calendar.
   *
   * @return the current calendar name, or null if none is active
   */
  String getCurrentCalendarName();


  /**
   * Copy a specific event to another calendar.
   *
   * @param eventName          the subject of the event to copy
   * @param sourceDateTime     the start date/time of the source event in format "YYYY-MM-DDTHH:mm"
   * @param targetCalendarName the name of the target calendar
   * @param targetDateTime     the desired start date/time in the target calendar
   * @return the ID of the newly created event
   * @throws IllegalArgumentException if event not found, target calendar not found,
   *                                  or time conflict
   */
  int copyEvent(String eventName, String sourceDateTime, String targetCalendarName,
                String targetDateTime);

  /**
   * Copy all events scheduled on a specific date to another calendar.
   *
   * @param sourceDate         the source date in format "YYYY-MM-DD"
   * @param targetCalendarName the name of the target calendar
   * @param targetDate         the target date in format "YYYY-MM-DD"
   * @return list of IDs of newly created events
   * @throws IllegalArgumentException if target calendar not found
   */
  List<Integer> copyEventsOnDate(String sourceDate, String targetCalendarName, String targetDate);

  /**
   * Copy all events in a date range to another calendar.
   *
   * @param startDate          the start of the range in format "YYYY-MM-DD" (inclusive)
   * @param endDate            the end of the range in format "YYYY-MM-DD" (inclusive)
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDate    the target start date in format "YYYY-MM-DD"
   * @return list of IDs of newly created events
   * @throws IllegalArgumentException if target calendar not found
   */
  List<Integer> copyEventsBetween(String startDate, String endDate, String targetCalendarName,
                                  String targetStartDate);


  /**
   * Get summary information about the calendar setup.
   *
   * @return formatted summary string
   */
  String getCalendarSummary();

  /**
   * Check if a timezone string is valid.
   *
   * @param timezone the timezone to validate
   * @return true if valid
   */
  boolean isValidTimezone(String timezone);

  /**
   * Get all available timezone IDs.
   *
   * @return set of available timezone IDs
   */
  Set<String> getAvailableTimezones();
}