package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for copying events between calendars with timezone conversion support.
 * Handles single event copying, batch copying by date, and range copying operations.
 */
public class EventCopyer {
  private final TimezoneConverter timezoneConverter;
  private final DateTimeFormatter dateFormatter;

  /**
   * Constructs an EventCopyer with the specified timezone converter.
   *
   * @param timezoneConverter the timezone converter to use for cross-timezone copying
   */
  public EventCopyer(TimezoneConverter timezoneConverter) {
    this.timezoneConverter = timezoneConverter;
    this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  /**
   * Copies a single event from one calendar to another at the specified target time.
   * The event duration is preserved, and timezone conversion is applied if necessary.
   *
   * @param sourceEvent     the event to copy
   * @param sourceCalendar  the calendar containing the source event
   * @param targetCalendar  the calendar to copy the event to
   * @param targetStartTime the desired start time for the copied event
   * @return the ID of the newly created event in the target calendar
   * @throws IllegalArgumentException if the event duration is invalid or if there's a conflict
   */
  public int copyEvent(Event sourceEvent, Calendar sourceCalendar,
                       Calendar targetCalendar, LocalDateTime targetStartTime) {

    Duration eventDuration = Duration.between(sourceEvent.getStartDateTime(),
            sourceEvent.getEndDateTime());
    LocalDateTime targetEndTime = targetStartTime.plus(eventDuration);

    if (targetEndTime.isBefore(targetStartTime)) {
      throw new IllegalArgumentException("Invalid event duration");
    }

    try {
      return targetCalendar.createSingleEvent(
              sourceEvent.getSubject(),
              sourceEvent.getDescription(),
              targetStartTime.toLocalDate().format(dateFormatter),
              targetStartTime.toLocalTime().toString(),
              targetEndTime.toLocalDate().format(dateFormatter),
              targetEndTime.toLocalTime().toString(),
              sourceEvent.getLocation(),
              sourceEvent.getStatus()
      );
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to copy event '" +
              sourceEvent.getSubject() + "': " + e.getMessage());
    }
  }

  /**
   * Copies all events from a list to a target calendar on the specified target date.
   * Events maintain their original time-of-day but are moved to the target date.
   * Timezone conversion is applied if the calendars have different timezones.
   * Events that cannot be copied due to conflicts are skipped and reported to stderr.
   *
   * @param sourceEvents   the list of events to copy
   * @param sourceCalendar the calendar containing the source events
   * @param targetCalendar the calendar to copy events to
   * @param targetDate     the date on which to schedule the copied events
   * @return a list of IDs for successfully copied events
   */
  public List<Integer> copyEventsOnDate(List<Event> sourceEvents,
                                        Calendar sourceCalendar,
                                        Calendar targetCalendar,
                                        LocalDate targetDate) {
    List<Integer> copiedEventIds = new ArrayList<>();
    List<String> skippedEvents = new ArrayList<>();

    for (Event sourceEvent : sourceEvents) {
      try {
        LocalDateTime sourceTime = sourceEvent.getStartDateTime();
        LocalDateTime targetStartTime;

        if (sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
          targetStartTime = LocalDateTime.of(targetDate, sourceTime.toLocalTime());
        } else {
          LocalDateTime convertedTime = timezoneConverter.convertTime(sourceTime,
                  sourceCalendar.getTimezone(), targetCalendar.getTimezone());
          targetStartTime = LocalDateTime.of(targetDate, convertedTime.toLocalTime());
        }

        int eventId = copyEvent(sourceEvent, sourceCalendar, targetCalendar, targetStartTime);
        copiedEventIds.add(eventId);

      } catch (IllegalArgumentException e) {
        skippedEvents.add(sourceEvent.getSubject() + ": " + e.getMessage());
      }
    }

    if (!skippedEvents.isEmpty()) {
      System.err.println("Skipped " + skippedEvents.size() + " events due to conflicts:");
      skippedEvents.forEach(msg -> System.err.println("  - " + msg));
    }

    return copiedEventIds;
  }

  /**
   * Copies events from a source date range to a target calendar starting at the specified date.
   * The relative positioning of events within the date range is preserved.
   * For example, if copying a 3-day range, an event on day 2 of the source range
   * will be placed on day 2 of the target range.
   * Timezone conversion is applied if the calendars have different timezones.
   * Events that cannot be copied due to conflicts are skipped and reported to stderr.
   *
   * @param sourceEvents    the list of events to copy from the source range
   * @param sourceCalendar  the calendar containing the source events
   * @param targetCalendar  the calendar to copy events to
   * @param sourceStartDate the start date of the source range
   * @param targetStartDate the start date of the target range
   * @return a list of IDs for successfully copied events
   */
  public List<Integer> copyEventsInRange(List<Event> sourceEvents,
                                         Calendar sourceCalendar,
                                         Calendar targetCalendar,
                                         LocalDate sourceStartDate,
                                         LocalDate targetStartDate) {
    List<Integer> copiedEventIds = new ArrayList<>();
    List<String> skippedEvents = new ArrayList<>();

    for (Event sourceEvent : sourceEvents) {
      try {
        LocalDate eventDate = sourceEvent.getStartDateTime().toLocalDate();
        long dayOffset = eventDate.toEpochDay() - sourceStartDate.toEpochDay();
        LocalDate targetEventDate = targetStartDate.plusDays(dayOffset);

        LocalDateTime sourceDateTime = sourceEvent.getStartDateTime();
        LocalDateTime targetStartTime;

        if (sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
          targetStartTime = LocalDateTime.of(targetEventDate, sourceDateTime.toLocalTime());
        } else {
          LocalDateTime convertedTime = timezoneConverter.convertTime(sourceDateTime,
                  sourceCalendar.getTimezone(), targetCalendar.getTimezone());
          targetStartTime = LocalDateTime.of(targetEventDate, convertedTime.toLocalTime());
        }

        int eventId = copyEvent(sourceEvent, sourceCalendar, targetCalendar, targetStartTime);
        copiedEventIds.add(eventId);

      } catch (IllegalArgumentException e) {
        skippedEvents.add(sourceEvent.getSubject() + " on " +
                sourceEvent.getStartDateTime().toLocalDate() + ": " + e.getMessage());
      }
    }

    if (!skippedEvents.isEmpty()) {
      System.err.println("Skipped " + skippedEvents.size() + " events due to conflicts:");
      skippedEvents.forEach(msg -> System.err.println("  - " + msg));
    }

    return copiedEventIds;
  }

  /**
   * Copies a series of related events (typically from a recurring series) to a target calendar.
   * Events are sorted by their occurrence index to maintain proper chronological order.
   * The relative timing between events in the series is preserved.
   *
   * @param sourceEvents    the list of events in the series to copy
   * @param sourceCalendar  the calendar containing the source event series
   * @param targetCalendar  the calendar to copy the event series to
   * @param targetStartDate the start date for the copied series
   * @return a list of IDs for successfully copied events, or empty list if no events provided
   */
  public List<Integer> copyEventSeries(List<Event> sourceEvents,
                                       Calendar sourceCalendar,
                                       Calendar targetCalendar,
                                       LocalDate targetStartDate) {

    List<Event> sortedEvents = sourceEvents.stream()
            .sorted((e1, e2) -> Integer.compare(e1.getOccurrenceIndex(), e2.getOccurrenceIndex()))
            .collect(Collectors.toList());

    if (sortedEvents.isEmpty()) {
      return new ArrayList<>();
    }

    LocalDate originalStartDate = sortedEvents.get(0).getStartDateTime().toLocalDate();
    return copyEventsInRange(sortedEvents, sourceCalendar, targetCalendar,
            originalStartDate, targetStartDate);
  }

  /**
   * Generates a summary string describing a copy operation between calendars.
   * Includes information about source and target calendars, number of events,
   * and whether timezone conversion will be applied.
   *
   * @param sourceEvents   the list of events to be copied
   * @param sourceCalendar the source calendar
   * @param targetCalendar the target calendar
   * @return a formatted summary string, or "No events to copy" if the event list is empty
   */
  public String getCopySummary(List<Event> sourceEvents, Calendar sourceCalendar,
                               Calendar targetCalendar) {
    if (sourceEvents.isEmpty()) {
      return "No events to copy";
    }

    StringBuilder summary = new StringBuilder();
    summary.append("Copy Summary:\n");
    summary.append("- Source: ").append(sourceCalendar.getName())
            .append(" (").append(sourceCalendar.getTimezone()).append(")\n");
    summary.append("- Target: ").append(targetCalendar.getName())
            .append(" (").append(targetCalendar.getTimezone()).append(")\n");
    summary.append("- Events to copy: ").append(sourceEvents.size()).append("\n");

    if (!sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
      summary.append("- Timezone conversion will be applied\n");
    }

    return summary.toString();
  }
}