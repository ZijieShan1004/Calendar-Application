package model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that represents a calendar with its name and timezone. Has similar
 * methods to the Model as it can cycle through a Map of events and assigns Ids.
 */
public class Calendar implements CalendarInterface {
  private String name;
  private ZoneId timezone;
  private final Map<Integer, Event> events;
  private int nextEventId;
  private int nextSeriesId;

  private final DateTimeFormatter DATE_FORMAT;
  private final DateTimeFormatter TIME_FORMAT;
  private final DateTimeFormatter DATETIME_FORMAT;

  /**
   * A constructor for a Calendar. Takes in a name and a specified timezone.
   * Similar to that of the Model impl.
   *
   * @param name     name of the Calendar
   * @param timezone timezone of the calendar
   */
  public Calendar(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.events = new HashMap<>();
    this.nextEventId = 1;
    this.nextSeriesId = 1;

    this.DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    this.DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  @Override
  public int createSingleEvent(String subject, String description, String startDate,
                               String startTime, String endDate, String endTime,
                               String location, Status status) {

    String actualEndDate = (endDate == null || endDate.isEmpty()) ? startDate : endDate;
    String actualEndTime = (endTime == null || endTime.isEmpty()) ? startTime : endTime;

    LocalDateTime start = parseDateTime(startDate, startTime);
    LocalDateTime end = parseDateTime(actualEndDate, actualEndTime);

    validateEventTimes(start, end);
    checkForDuplicateEvent(subject, start, end);
    checkForTimeConflict(start, end);

    Event event = new Event.Builder()
            .subject(subject)
            .description(description == null ? "" : description)
            .startDateTime(start)
            .endDateTime(end)
            .location(location == null ? "" : location)
            .status(status)
            .build();

    int eventId = nextEventId++;
    events.put(eventId, event);
    return eventId;
  }

  @Override
  public int createAllDayEvent(String subject, String description, String date,
                               String location, Status status) {
    LocalDate eventDate = parseDate(date);
    LocalDateTime start = LocalDateTime.of(eventDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(eventDate, LocalTime.of(17, 0));

    checkForDuplicateEvent(subject, start, end);

    Event event = new Event.Builder()
            .subject(subject)
            .description(description == null ? "" : description)
            .startDateTime(start)
            .endDateTime(end)
            .location(location == null ? "" : location)
            .status(status)
            .build();

    int eventId = nextEventId++;
    events.put(eventId, event);
    return eventId;
  }

  @Override
  public int createTimedEventSeries(String subject, String description, String startDate,
                                    String startTime, String endDate, String endTime,
                                    String weekdays, int recurrence, String untilDate,
                                    String location, Status status) {

    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Must specify which days of the week");
    }

    LocalDateTime firstStart = parseDateTime(startDate, startTime);
    LocalDateTime firstEnd = parseDateTime(endDate, endTime);

    if (!firstStart.toLocalDate().equals(firstEnd.toLocalDate())) {
      throw new IllegalArgumentException("Each event must start and end on the same day");
    }

    validateEventTimes(firstStart, firstEnd);
    List<DayOfWeek> targetDays = parseWeekdays(weekdays);
    LocalDate until = (untilDate == null || untilDate.isEmpty()) ? null : parseDate(untilDate);

    List<Event> seriesEvents = generateSeriesEvents(subject, description, location, status,
            firstStart, firstEnd, targetDays, recurrence, until);

    int seriesId = nextSeriesId++;
    for (Event event : seriesEvents) {
      event.setSeriesId(seriesId);
      events.put(nextEventId++, event);
    }

    return seriesId;
  }

  @Override
  public int createAllDayEventSeries(String subject, String description, String date,
                                     String weekdays, int recurrence, String untilDate,
                                     String location, Status status) {

    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Must specify which days of the week");
    }

    LocalDate startDate = parseDate(date);
    List<DayOfWeek> targetDays = parseWeekdays(weekdays);
    LocalDate until = (untilDate == null || untilDate.isEmpty()) ? null : parseDate(untilDate);

    List<Event> seriesEvents = generateAllDaySeriesEvents(subject, description, location,
            status, startDate, targetDays, recurrence, until);

    int seriesId = nextSeriesId++;
    for (Event event : seriesEvents) {
      event.setSeriesId(seriesId);
      events.put(nextEventId++, event);
    }

    return seriesId;
  }

  @Override
  public boolean editSingleEvent(String subject, String startDateTime, String property,
                                 String newValue) {
    Event event = findEvent(subject, startDateTime);
    updateEventProperty(event, property, newValue);
    return true;
  }

  @Override
  public int editEventsFrom(String subject, String startDateTime, String property,
                            String newValue) {
    Event targetEvent = findEvent(subject, startDateTime);

    if (targetEvent.getSeriesId() == null) {
      updateEventProperty(targetEvent, property, newValue);
      return 1;
    }

    int modifiedCount = 0;
    for (Event event : events.values()) {
      if (isSameSeriesFromIndex(event, targetEvent)) {
        updateEventProperty(event, property, newValue);
        modifiedCount++;
      }
    }
    return modifiedCount;
  }

  @Override
  public int editEntireSeries(String subject, String startDateTime, String property,
                              String newValue) {
    Event targetEvent = findEvent(subject, startDateTime);

    if (targetEvent.getSeriesId() == null) {
      updateEventProperty(targetEvent, property, newValue);
      return 1;
    }

    int modifiedCount = 0;
    for (Event event : events.values()) {
      if (isSameSeries(event, targetEvent)) {
        updateEventProperty(event, property, newValue);
        modifiedCount++;
      }
    }
    return modifiedCount;
  }

  @Override
  public boolean deleteSingleEvent(String subject, String startDateTime) {
    EventInfo eventInfo = findEventWithId(subject, startDateTime);
    events.remove(eventInfo.id);
    return true;
  }

  @Override
  public int deleteEventsFrom(String subject, String startDateTime) {
    Event targetEvent = findEvent(subject, startDateTime);

    if (targetEvent.getSeriesId() == null) {
      EventInfo eventInfo = findEventWithId(subject, startDateTime);
      events.remove(eventInfo.id);
      return 1;
    }

    List<Integer> toDelete = new ArrayList<>();
    for (Map.Entry<Integer, Event> entry : events.entrySet()) {
      Event event = entry.getValue();
      if (isSameSeriesFromIndex(event, targetEvent)) {
        toDelete.add(entry.getKey());
      }
    }

    for (Integer id : toDelete) {
      events.remove(id);
    }
    return toDelete.size();
  }

  @Override
  public int deleteEntireSeries(String subject, String startDateTime) {
    Event targetEvent = findEvent(subject, startDateTime);

    if (targetEvent.getSeriesId() == null) {
      EventInfo eventInfo = findEventWithId(subject, startDateTime);
      events.remove(eventInfo.id);
      return 1;
    }

    List<Integer> toDelete = new ArrayList<>();
    for (Map.Entry<Integer, Event> entry : events.entrySet()) {
      Event event = entry.getValue();
      if (isSameSeries(event, targetEvent)) {
        toDelete.add(entry.getKey());
      }
    }

    for (Integer id : toDelete) {
      events.remove(id);
    }
    return toDelete.size();
  }

  @Override
  public List<Event> getEventsOnDate(String date) {
    LocalDate targetDate = parseDate(date);
    return events.values().stream()
            .filter(event -> eventOccursOnDate(event, targetDate))
            .sorted(Comparator.comparing(Event::getStartDateTime))
            .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(String startTime, String endTime) {
    LocalDateTime start = parseDateTime(startTime);
    LocalDateTime end = parseDateTime(endTime);

    return events.values().stream()
            .filter(event -> eventOverlapsWith(event, start, end))
            .sorted(Comparator.comparing(Event::getStartDateTime))
            .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(String dateTime) {
    LocalDateTime time = parseDateTime(dateTime);
    return events.values().stream()
            .anyMatch(event -> isTimeInEvent(time, event));
  }

  @Override
  public int getEventCount() {
    return events.size();
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events.values());
  }

  private LocalDate parseDate(String dateString) {
    try {
      return LocalDate.parse(dateString, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateString);
    }
  }

  private LocalDateTime parseDateTime(String dateString, String timeString) {
    try {
      return LocalDateTime.of(
              LocalDate.parse(dateString, DATE_FORMAT),
              LocalTime.parse(timeString, TIME_FORMAT)
      );
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time: " + dateString + "T" + timeString);
    }
  }

  private LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString, DATETIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeString);
    }
  }

  private void validateEventTimes(LocalDateTime start, LocalDateTime end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Event cannot end before it starts");
    }
  }

  private void checkForDuplicateEvent(String subject, LocalDateTime start, LocalDateTime end) {
    boolean hasDuplicate = events.values().stream()
            .anyMatch(event -> event.getSubject().equals(subject) &&
                    event.getStartDateTime().equals(start) &&
                    event.getEndDateTime().equals(end));

    if (hasDuplicate) {
      throw new IllegalArgumentException("This exact event already exists");
    }
  }

  private void checkForTimeConflict(LocalDateTime start, LocalDateTime end) {
    boolean hasConflict = events.values().stream()
            .anyMatch(event -> eventsOverlap(start, end, event.getStartDateTime(),
                    event.getEndDateTime()));

    if (hasConflict) {
      throw new IllegalArgumentException("This time conflicts with an existing event");
    }
  }

  private boolean eventsOverlap(LocalDateTime start1, LocalDateTime end1,
                                LocalDateTime start2, LocalDateTime end2) {
    return start1.isBefore(end2) && end1.isAfter(start2);
  }

  private List<DayOfWeek> parseWeekdays(String weekdays) {
    List<DayOfWeek> days = new ArrayList<>();
    for (char dayChar : weekdays.toCharArray()) {
      Day day = Day.fromAbbreviation(dayChar);
      days.add(convertDayToDayOfWeek(day));
    }
    return days;
  }

  private DayOfWeek convertDayToDayOfWeek(Day day) {
    switch (day) {
      case M:
        return DayOfWeek.MONDAY;
      case T:
        return DayOfWeek.TUESDAY;
      case W:
        return DayOfWeek.WEDNESDAY;
      case R:
        return DayOfWeek.THURSDAY;
      case F:
        return DayOfWeek.FRIDAY;
      case S:
        return DayOfWeek.SATURDAY;
      case U:
        return DayOfWeek.SUNDAY;
      default:
        throw new IllegalArgumentException("Unknown day: " + day);
    }
  }

  private List<Event> generateSeriesEvents(String subject, String description, String location,
                                           Status status, LocalDateTime firstStart,
                                           LocalDateTime firstEnd, List<DayOfWeek> targetDays,
                                           int recurrence, LocalDate until) {
    List<Event> seriesEvents = new ArrayList<>();
    Duration eventDuration = Duration.between(firstStart, firstEnd);
    LocalDate currentDate = firstStart.toLocalDate();
    int eventCount = 0;
    int dayCount = 0;

    while (dayCount < 10000) {
      if (recurrence > 0 && eventCount >= recurrence) {
        break;
      }
      if (until != null && currentDate.isAfter(until)) {
        break;
      }

      if (targetDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, firstStart.toLocalTime());
        LocalDateTime eventEnd = eventStart.plus(eventDuration);

        checkForTimeConflict(eventStart, eventEnd);

        Event event = new Event.Builder()
                .subject(subject)
                .description(description == null ? "" : description)
                .startDateTime(eventStart)
                .endDateTime(eventEnd)
                .location(location == null ? "" : location)
                .status(status)
                .occurrenceIndex(eventCount)
                .build();

        seriesEvents.add(event);
        eventCount++;
      }

      currentDate = currentDate.plusDays(1);
      dayCount++;
    }

    if (seriesEvents.isEmpty()) {
      throw new IllegalArgumentException("No events could be created with these settings");
    }
    return seriesEvents;
  }

  private List<Event> generateAllDaySeriesEvents(String subject, String description,
                                                 String location, Status status,
                                                 LocalDate startDate,
                                                 List<DayOfWeek> targetDays,
                                                 int recurrence, LocalDate until) {
    List<Event> seriesEvents = new ArrayList<>();
    LocalDate currentDate = startDate;
    int eventCount = 0;
    int dayCount = 0;

    while (dayCount < 10000) {
      if (recurrence > 0 && eventCount >= recurrence) {
        break;
      }
      if (until != null && currentDate.isAfter(until)) {
        break;
      }

      if (targetDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, LocalTime.of(8, 0));
        LocalDateTime eventEnd = LocalDateTime.of(currentDate, LocalTime.of(17, 0));

        Event event = new Event.Builder()
                .subject(subject)
                .description(description == null ? "" : description)
                .startDateTime(eventStart)
                .endDateTime(eventEnd)
                .location(location == null ? "" : location)
                .status(status)
                .occurrenceIndex(eventCount)
                .build();

        seriesEvents.add(event);
        eventCount++;
      }

      currentDate = currentDate.plusDays(1);
      dayCount++;
    }

    if (seriesEvents.isEmpty()) {
      throw new IllegalArgumentException("No events could be created with these settings");
    }
    return seriesEvents;
  }

  private Event findEvent(String subject, String startDateTime) {
    return findEventWithId(subject, startDateTime).event;
  }

  private EventInfo findEventWithId(String subject, String startDateTime) {
    LocalDateTime targetTime = parseDateTime(startDateTime);

    List<EventInfo> matches = events.entrySet().stream()
            .filter(entry -> {
              Event event = entry.getValue();
              return event.getSubject().equals(subject) &&
                      event.getStartDateTime().equals(targetTime);
            })
            .map(entry -> new EventInfo(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found: " + subject + " @ " + startDateTime);
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events found: " + subject + " @ "
              + startDateTime);
    }
    return matches.get(0);
  }

  private void updateEventProperty(Event event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        event.setSubject(newValue);
        break;
      case "start":
        LocalDateTime newStart = parseDateTime(newValue);
        if (newStart.isAfter(event.getEndDateTime())) {
          throw new IllegalArgumentException("Start time cannot be after end time");
        }
        removeEventFromSeries(event);
        event.setStartDateTime(newStart);
        break;
      case "end":
        LocalDateTime newEnd = parseDateTime(newValue);
        if (newEnd.isBefore(event.getStartDateTime())) {
          throw new IllegalArgumentException("End time cannot be before start time");
        }
        removeEventFromSeries(event);
        event.setEndDateTime(newEnd);
        break;
      case "description":
        event.setDescription(newValue);
        break;
      case "location":
        event.setLocation(newValue);
        break;
      case "status":
        try {
          event.setStatus(Status.valueOf(newValue.toUpperCase()));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid status: " + newValue);
        }
        break;
      default:
        throw new IllegalArgumentException("Cannot edit property: " + property);
    }
  }

  private void removeEventFromSeries(Event event) {
    if (event.getSeriesId() != null) {
      event.setSeriesId(null);
      event.setOccurrenceIndex(0);
    }
  }

  private boolean isSameSeriesFromIndex(Event event, Event targetEvent) {
    return targetEvent.getSeriesId() != null &&
            targetEvent.getSeriesId().equals(event.getSeriesId()) &&
            event.getOccurrenceIndex() >= targetEvent.getOccurrenceIndex();
  }

  private boolean isSameSeries(Event event, Event targetEvent) {
    return targetEvent.getSeriesId() != null &&
            targetEvent.getSeriesId().equals(event.getSeriesId());
  }

  private boolean eventOccursOnDate(Event event, LocalDate date) {
    LocalDate eventStart = event.getStartDateTime().toLocalDate();
    LocalDate eventEnd = event.getEndDateTime().toLocalDate();
    return !date.isBefore(eventStart) && !date.isAfter(eventEnd);
  }

  private boolean eventOverlapsWith(Event event, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return !event.getEndDateTime().isBefore(rangeStart) &&
            !event.getStartDateTime().isAfter(rangeEnd);
  }

  private boolean isTimeInEvent(LocalDateTime time, Event event) {
    return !time.isBefore(event.getStartDateTime()) &&
            time.isBefore(event.getEndDateTime());
  }

  private static class EventInfo {
    final int id;
    final Event event;

    EventInfo(int id, Event event) {
      this.id = id;
      this.event = event;
    }
  }
}
