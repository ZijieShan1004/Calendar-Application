package model;

import java.time.ZoneId;
import java.util.List;

/**
 * Interface defining the core functionality of a calendar that can manage events
 * with timezone support. Supports single events, recurring event series, and
 * various query and modification operations.
 */
public interface CalendarInterface {

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Sets the name of this calendar.
   *
   * @param name the new name for the calendar
   */
  void setName(String name);

  /**
   * Gets the timezone of this calendar.
   *
   * @return the ZoneId representing the calendar's timezone
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone for the calendar
   */
  void setTimezone(ZoneId timezone);

  /**
   * Creates a single event with specified start and end times.
   * Validates for time conflicts, duplicate events, and proper time ordering.
   * If endDate or endTime are null/empty, they default to startDate and startTime respectively.
   *
   * @param subject     the event subject/title
   * @param description optional event description (can be null)
   * @param startDate   the start date in "yyyy-MM-dd" format
   * @param startTime   the start time in "HH:mm" format
   * @param endDate     the end date in "yyyy-MM-dd" format (null defaults to startDate)
   * @param endTime     the end time in "HH:mm" format (null defaults to startTime)
   * @param location    optional event location (can be null)
   * @param status      the event visibility status (PUBLIC/PRIVATE)
   * @return the unique ID of the created event
   * @throws IllegalArgumentException if times are invalid, event conflicts, or is duplicate
   */
  int createSingleEvent(String subject, String description, String startDate,
                        String startTime, String endDate, String endTime,
                        String location, Status status);

  /**
   * Creates an all-day event from 8:00 AM to 5:00 PM on the specified date.
   * Validates for duplicate events but not time conflicts (all-day events can coexist).
   *
   * @param subject     the event subject/title
   * @param description optional event description (can be null)
   * @param date        the event date in "yyyy-MM-dd" format
   * @param location    optional event location (can be null)
   * @param status      the event visibility status (PUBLIC/PRIVATE)
   * @return the unique ID of the created event
   * @throws IllegalArgumentException if the date format is invalid or event is duplicate
   */
  int createAllDayEvent(String subject, String description, String date,
                        String location, Status status);

  /**
   * Creates a series of recurring timed events on specified weekdays.
   * Each event in the series has the same duration and occurs on the specified days.
   * The series continues for either a specified number of occurrences or until a date.
   *
   * @param subject     the event subject/title for all events in the series
   * @param description optional event description (can be null)
   * @param startDate   the start date for the first event in "yyyy-MM-dd" format
   * @param startTime   the start time for all events in "HH:mm" format
   * @param endDate     the end date for the first event in "yyyy-MM-dd" format
   * @param endTime     the end time for all events in "HH:mm" format
   * @param weekdays    string of weekday abbreviations (M,T,W,R,F,S,U)
   * @param recurrence  number of occurrences (>0), or 0 if using untilDate
   * @param untilDate   end date for series in "yyyy-MM-dd" format, or null if using recurrence
   * @param location    optional event location (can be null)
   * @param status      the event visibility status (PUBLIC/PRIVATE)
   * @return the unique series ID for all events in the series
   * @throws IllegalArgumentException if parameters are invalid, weekdays empty, or events conflict
   */
  int createTimedEventSeries(String subject, String description, String startDate,
                             String startTime, String endDate, String endTime,
                             String weekdays, int recurrence, String untilDate,
                             String location, Status status);

  /**
   * Creates a series of recurring all-day events (8:00 AM to 5:00 PM) on specified weekdays.
   * The series continues for either a specified number of occurrences or until a date.
   *
   * @param subject     the event subject/title for all events in the series
   * @param description optional event description (can be null)
   * @param date        the date for the first event in "yyyy-MM-dd" format
   * @param weekdays    string of weekday abbreviations (M,T,W,R,F,S,U)
   * @param recurrence  number of occurrences (>0), or 0 if using untilDate
   * @param untilDate   end date for series in "yyyy-MM-dd" format, or null if using recurrence
   * @param location    optional event location (can be null)
   * @param status      the event visibility status (PUBLIC/PRIVATE)
   * @return the unique series ID for all events in the series
   * @throws IllegalArgumentException if parameters are invalid or weekdays empty
   */
  int createAllDayEventSeries(String subject, String description, String date,
                              String weekdays, int recurrence, String untilDate,
                              String location, Status status);

  /**
   * Edits a single event's property. If the event is part of a series, only that
   * specific occurrence is modified and it's removed from the series.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event in "yyyy-MM-ddTHH:mm" format
   * @param property      the property to edit (subject, start, end, description, location, status)
   * @param newValue      the new value for the property
   * @return true if the event was successfully edited
   * @throws IllegalArgumentException if event not found, property invalid, or new value invalid
   */
  boolean editSingleEvent(String subject, String startDateTime, String property, String newValue);

  /**
   * Edits all events in a series starting from the specified occurrence onward.
   * If the event is not part of a series, behaves like editSingleEvent.
   *
   * @param subject       the subject of the target event in the series
   * @param startDateTime the start date/time of the target occurrence in "yyyy-MM-ddTHH:mm" format
   * @param property      the property to edit (subject, start, end, description, location, status)
   * @param newValue      the new value for the property
   * @return the number of events that were modified
   * @throws IllegalArgumentException if event not found, property invalid, or new value invalid
   */
  int editEventsFrom(String subject, String startDateTime, String property, String newValue);

  /**
   * Edits all events in a series (past, present, and future occurrences).
   * If the event is not part of a series, behaves like editSingleEvent.
   *
   * @param subject       the subject of any event in the series
   * @param startDateTime the start date/time of any occurrence in "yyyy-MM-ddTHH:mm" format
   * @param property      the property to edit (subject, start, end, description, location, status)
   * @param newValue      the new value for the property
   * @return the number of events that were modified
   * @throws IllegalArgumentException if event not found, property invalid, or new value invalid
   */
  int editEntireSeries(String subject, String startDateTime, String property, String newValue);

  /**
   * Deletes a single event. If the event is part of a series, only that
   * specific occurrence is removed.
   *
   * @param subject       the subject of the event to delete
   * @param startDateTime the start date/time of the event in "yyyy-MM-ddTHH:mm" format
   * @return true if the event was successfully deleted
   * @throws IllegalArgumentException if event not found or multiple matches found
   */
  boolean deleteSingleEvent(String subject, String startDateTime);

  /**
   * Deletes all events in a series starting from the specified occurrence onward.
   * If the event is not part of a series, behaves like deleteSingleEvent.
   *
   * @param subject       the subject of the target event in the series
   * @param startDateTime the start date/time of the target occurrence in "yyyy-MM-ddTHH:mm" format
   * @return the number of events that were deleted
   * @throws IllegalArgumentException if event not found
   */
  int deleteEventsFrom(String subject, String startDateTime);

  /**
   * Deletes all events in a series (past, present, and future occurrences).
   * If the event is not part of a series, behaves like deleteSingleEvent.
   *
   * @param subject       the subject of any event in the series
   * @param startDateTime the start date/time of any occurrence in "yyyy-MM-ddTHH:mm" format
   * @return the number of events that were deleted
   * @throws IllegalArgumentException if event not found
   */
  int deleteEntireSeries(String subject, String startDateTime);

  /**
   * Retrieves all events that occur on the specified date.
   * An event occurs on a date if any part of it falls on that date.
   *
   * @param date the date to query in "yyyy-MM-dd" format
   * @return a list of events occurring on the specified date, sorted by start time
   * @throws IllegalArgumentException if the date format is invalid
   */
  List<Event> getEventsOnDate(String date);

  /**
   * Retrieves all events that overlap with the specified time range.
   * An event overlaps if any part of it falls within the given range.
   *
   * @param startTime the start of the range in "yyyy-MM-ddTHH:mm" format
   * @param endTime   the end of the range in "yyyy-MM-ddTHH:mm" format
   * @return a list of events overlapping with the specified range, sorted by start time
   * @throws IllegalArgumentException if the datetime format is invalid
   */
  List<Event> getEventsInRange(String startTime, String endTime);

  /**
   * Checks if there are any events scheduled at the specified date/time.
   * Returns true if the specified time falls within any existing event.
   *
   * @param dateTime the date/time to check in "yyyy-MM-ddTHH:mm" format
   * @return true if there is an event at the specified time, false otherwise
   * @throws IllegalArgumentException if the datetime format is invalid
   */
  boolean isBusy(String dateTime);

  /**
   * Gets the total number of events in this calendar.
   *
   * @return the number of events currently stored in the calendar
   */
  int getEventCount();

  /**
   * Retrieves all events in this calendar.
   *
   * @return a list containing all events in the calendar (defensive copy)
   */
  List<Event> getAllEvents();
}