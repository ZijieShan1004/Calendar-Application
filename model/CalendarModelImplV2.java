package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


/**
 * Enhanced implementation of CalendarModel using composition pattern internally.
 * Maintains exact same public interface as original CalendarModelImpl.
 */
public class CalendarModelImplV2 implements CalendarModelV2 {
  private final CalendarManager calendarManager;
  private final EventCopyer copyer;
  private final TimezoneConverter timezoneConverter;
  private final DateTimeFormatter DATE_FORMAT;
  private final DateTimeFormatter DATETIME_FORMAT;

  /**
   * The Constructor for the new Model implementation of a calendar. Allows this Model
   * to perform the new Calendar and timezone management.
   */
  public CalendarModelImplV2() {
    this.calendarManager = new CalendarManager();
    this.timezoneConverter = new TimezoneConverter();
    this.copyer = new EventCopyer(timezoneConverter);
    this.DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    createDefaultCalendar();
  }

  private void createDefaultCalendar() {
    try {
      calendarManager.createCalendar("Default", ZoneId.systemDefault());
      calendarManager.useCalendar("Default");
    } catch (Exception e) {
      // Fallback - should not happen with system default timezone
    }
  }

  @Override
  public int createSingleEvent(String subject, String description, String startDate,
                               String startTime, String endDate, String endTime,
                               String location, Status status) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.createSingleEvent(subject, description, startDate, startTime,
            endDate, endTime, location, status);
  }

  @Override
  public int createAllDayEvent(String subject, String description, String date,
                               String location, Status status) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.createAllDayEvent(subject, description, date, location, status);
  }

  @Override
  public int createTimedEventSeries(String subject, String description, String startDate,
                                    String startTime, String endDate, String endTime,
                                    String weekdays, int recurrence, String untilDate,
                                    String location, Status status) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.createTimedEventSeries(subject, description, startDate, startTime,
            endDate, endTime, weekdays, recurrence,
            untilDate, location, status);
  }

  @Override
  public int createAllDayEventSeries(String subject, String description, String date,
                                     String weekdays, int recurrence, String untilDate,
                                     String location, Status status) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.createAllDayEventSeries(subject, description, date, weekdays,
            recurrence, untilDate, location, status);
  }

  @Override
  public boolean editSingleEvent(String subject, String startDateTime, String property,
                                 String newValue) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.editSingleEvent(subject, startDateTime, property, newValue);
  }

  @Override
  public int editEventsFrom(String subject, String startDateTime, String property,
                            String newValue) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.editEventsFrom(subject, startDateTime, property, newValue);
  }

  @Override
  public int editEntireSeries(String subject, String startDateTime, String property,
                              String newValue) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.editEntireSeries(subject, startDateTime, property, newValue);
  }

  @Override
  public boolean deleteSingleEvent(String subject, String startDateTime) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.deleteSingleEvent(subject, startDateTime);
  }

  @Override
  public int deleteEventsFrom(String subject, String startDateTime) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.deleteEventsFrom(subject, startDateTime);
  }

  @Override
  public int deleteEntireSeries(String subject, String startDateTime) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.deleteEntireSeries(subject, startDateTime);
  }

  @Override
  public List<Event> getEventsOnDate(String date) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.getEventsOnDate(date);
  }

  @Override
  public List<Event> getEventsInRange(String start, String end) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.getEventsInRange(start, end);
  }

  @Override
  public boolean isBusy(String dateTime) {
    Calendar activeCalendar = calendarManager.getActiveCalendar();
    return activeCalendar.isBusy(dateTime);
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      calendarManager.createCalendar(name, zoneId);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone +
              ". Use IANA timezone format (e.g., 'America/New_York')");
    }
  }

  @Override
  public void editCalendarProperty(String name, String property, String newValue) {
    calendarManager.editCalendarProperty(name, property, newValue);
  }

  @Override
  public void useCalendar(String name) {
    calendarManager.useCalendar(name);
  }

  @Override
  public String getCurrentCalendarName() {
    return calendarManager.getActiveCalendarName();
  }

  @Override
  public int copyEvent(String eventName, String sourceDateTime,
                       String targetCalendarName, String targetDateTime) {
    Calendar sourceCalendar = calendarManager.getActiveCalendar();
    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    LocalDateTime sourceStart = parseDateTime(sourceDateTime);
    List<Event> sourceEvents = sourceCalendar.getEventsOnDate(
            sourceStart.toLocalDate().format(DATE_FORMAT));

    Event sourceEvent = sourceEvents.stream()
            .filter(event -> event.getSubject().equals(eventName) &&
                    event.getStartDateTime().equals(sourceStart))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                    "Event not found: " + eventName + " at " + sourceDateTime));

    LocalDateTime targetStart = parseDateTime(targetDateTime);
    return copyer.copyEvent(sourceEvent, sourceCalendar, targetCalendar, targetStart);
  }

  @Override
  public List<Integer> copyEventsOnDate(String sourceDate, String targetCalendarName,
                                        String targetDate) {
    Calendar sourceCalendar = calendarManager.getActiveCalendar();
    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    List<Event> sourceEvents = sourceCalendar.getEventsOnDate(sourceDate);
    LocalDate targetLocalDate = parseDate(targetDate);

    return copyer.copyEventsOnDate(sourceEvents, sourceCalendar,
            targetCalendar, targetLocalDate);
  }

  @Override
  public List<Integer> copyEventsBetween(String startDate, String endDate,
                                         String targetCalendarName, String targetStartDate) {
    Calendar sourceCalendar = calendarManager.getActiveCalendar();
    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    String startDateTime = startDate + "T00:00";
    String endDateTime = endDate + "T23:59";
    List<Event> sourceEvents = sourceCalendar.getEventsInRange(startDateTime, endDateTime);

    LocalDate sourceStart = parseDate(startDate);
    LocalDate targetStart = parseDate(targetStartDate);

    return copyer.copyEventsInRange(sourceEvents, sourceCalendar, targetCalendar,
            sourceStart, targetStart);
  }

  @Override
  public String getCalendarSummary() {
    StringBuilder summary = new StringBuilder();
    summary.append("Calendar Summary:\n");
    summary.append("- Total calendars: ").append(calendarManager.getCalendarCount()).append("\n");

    String activeName = calendarManager.getActiveCalendarName();
    if (activeName != null) {
      Calendar activeCalendar = calendarManager.getActiveCalendar();
      summary.append("- Active calendar: ").append(activeName)
              .append(" (").append(activeCalendar.getTimezone()).append(")\n");
      summary.append("- Events in active calendar: ")
              .append(activeCalendar.getEventCount()).append("\n");
    } else {
      summary.append("- No active calendar\n");
    }

    summary.append("- Available calendars: ").append(calendarManager.getAllCalendarNames());
    return summary.toString();
  }

  @Override
  public boolean isValidTimezone(String timezone) {
    return timezoneConverter.isValidTimezone(timezone);
  }

  @Override
  public java.util.Set<String> getAvailableTimezones() {
    return timezoneConverter.getAvailableTimezones();
  }

  private LocalDate parseDate(String dateString) {
    try {
      return LocalDate.parse(dateString, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateString +
              ". Expected format: yyyy-MM-dd");
    }
  }

  private LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString, DATETIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeString +
              ". Expected format: yyyy-MM-ddTHH:mm");
    }
  }
}