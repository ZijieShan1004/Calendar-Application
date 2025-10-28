package model;

import java.util.List;

/**
 * Interface for the calendar's core logic (Model in MVC).
 */
public interface CalendarModel {

  /**
   * Create a single event with explicit start/end time.
   *
   * @param subject     Event subject
   * @param description Optional description
   * @param startDate   "YYYY-MM-DD"
   * @param startTime   "HH:mm"
   * @param endDate     "YYYY-MM-DD" or null for default
   * @param endTime     "HH:mm" or null for default
   * @param location    Optional location string
   * @param status      PUBLIC or PRIVATE
   * @return generated eventId
   * @throws IllegalArgumentException if invalid params or conflict
   */
  int createSingleEvent(
          String subject,
          String description,
          String startDate,
          String startTime,
          String endDate,
          String endTime,
          String location,
          Status status
  );

  /**
   * Create a single "all-day" event from 08:00 to 17:00.
   *
   * @param subject     Event subject
   * @param description Optional description
   * @param date        "YYYY-MM-DD"
   * @param location    Optional location
   * @param status      PUBLIC or PRIVATE
   * @return generated eventId
   */
  int createAllDayEvent(
          String subject,
          String description,
          String date,
          String location,
          Status status
  );

  /**
   * Create a series of events with explicit start/end times.
   * Each event in the series must fall on one of the given weekdays,
   * repeat for either N occurrences (if recurrence > 0) or until 'untilDate' (inclusive).
   * If recurrence > 0, ignore untilDate; if untilDate != null, ignore recurrence.
   *
   * @param subject     Event subject
   * @param description Optional description
   * @param startDate   "YYYY-MM-DD"
   * @param startTime   "HH:mm"
   * @param endDate     "YYYY-MM-DD"
   * @param endTime     "HH:mm"
   * @param weekdays    e.g. "MWF"
   * @param recurrence  number of occurrences (>0) or 0 if using untilDate
   * @param untilDate   "YYYY-MM-DD" (inclusive) or null if using recurrence
   * @param location    Optional location
   * @param status      PUBLIC or PRIVATE
   * @return generated seriesId
   */
  int createTimedEventSeries(
          String subject,
          String description,
          String startDate,
          String startTime,
          String endDate,
          String endTime,
          String weekdays,
          int recurrence,
          String untilDate,
          String location,
          Status status
  );

  /**
   * Create a series of "all-day" events (08:00–17:00) on given weekdays.
   *
   * @param subject     Event subject
   * @param description Optional description
   * @param date        "YYYY-MM-DD" for first occurrence
   * @param weekdays    e.g. "MW"
   * @param recurrence  number of occurrences (>0) or 0 if using untilDate
   * @param untilDate   "YYYY-MM-DD" inclusive or null if using recurrence
   * @param location    Optional location
   * @param status      PUBLIC or PRIVATE
   * @return generated seriesId
   */
  int createAllDayEventSeries(
          String subject,
          String description,
          String date,
          String weekdays,
          int recurrence,
          String untilDate,
          String location,
          Status status
  );

  /**
   * Edit a single event's property (identified by subject + startDateTime).
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @param property      one of: subject, start, end, description, location, status
   * @param newValue      new value string (格式视 property 而定)
   * @return true if success
   * @throws IllegalArgumentException if not found, invalid property, or ambiguous
   */
  boolean editSingleEvent(
          String subject,
          String startDateTime,
          String property,
          String newValue
  );

  /**
   * Edit events FROM the given occurrence onward in its series.
   * If the identified event is not part of any series, same as editSingleEvent.
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @param property      to edit
   * @param newValue      new value
   * @return number of events modified
   * @throws IllegalArgumentException if not found or ambiguous or invalid property
   */
  int editEventsFrom(
          String subject,
          String startDateTime,
          String property,
          String newValue
  );

  /**
   * Edit all events in the series (past + future).
   * If not part of series, same as editSingleEvent.
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @param property      to edit
   * @param newValue      new value
   * @return number of events modified
   */
  int editEntireSeries(
          String subject,
          String startDateTime,
          String property,
          String newValue
  );

  /**
   * Delete a single event (identified by subject + startDateTime).
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @return true if deleted
   * @throws IllegalArgumentException if not found or ambiguous
   */
  boolean deleteSingleEvent(String subject, String startDateTime);

  /**
   * Delete events FROM the given occurrence onward in its series.
   * If the target is not part of a series, same as deleteSingleEvent.
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @return number of events deleted
   */
  int deleteEventsFrom(String subject, String startDateTime);

  /**
   * Delete all events in the series (past + future).
   * If not part of series, same as deleteSingleEvent.
   *
   * @param subject       subject of the target event
   * @param startDateTime "YYYY-MM-DDThh:mm"
   * @return number of events deleted
   */
  int deleteEntireSeries(String subject, String startDateTime);

  /**
   * Get all events on a given date.
   *
   * @param date "YYYY-MM-DD"
   * @return list of matching events (possibly empty)
   */
  List<Event> getEventsOnDate(String date);

  /**
   * Get all events between two dateTime bounds (inclusive).
   *
   * @param start "YYYY-MM-DDThh:mm"
   * @param end   "YYYY-MM-DDThh:mm"
   * @return list of matching events
   */
  List<Event> getEventsInRange(String start, String end);

  /**
   * Check if user is busy at the given dateTime.
   *
   * @param dateTime "YYYY-MM-DDThh:mm"
   * @return true if any event covers that time
   */
  boolean isBusy(String dateTime);
}
