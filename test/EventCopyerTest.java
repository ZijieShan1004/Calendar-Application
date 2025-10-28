import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import model.Calendar;
import model.Event;
import model.EventCopyer;
import model.Status;
import model.TimezoneConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test class for the methods of the EventCopyService class.
 */
public class EventCopyerTest {
  private EventCopyer copyer;
  private Calendar sourceCalendar;
  private Calendar targetCalendar;

  @Before
  public void setUp() {
    TimezoneConverter timezoneConverter = new TimezoneConverter();
    copyer = new EventCopyer(timezoneConverter);
    sourceCalendar = new Calendar("Source", ZoneId.of("America/New_York"));
    targetCalendar = new Calendar("Target", ZoneId.of("America/Los_Angeles"));
  }

  @Test
  public void testCopyEventSameTimezone() {
    Calendar sameTimezoneTarget = new Calendar("Target", ZoneId.of(
            "America/New_York"));

    Event sourceEvent = new Event.Builder()
            .subject("Meeting")
            .description("Important meeting")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 14, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))
            .location("Conference Room")
            .status(Status.PUBLIC)
            .build();

    LocalDateTime targetTime = LocalDateTime.of(2025, 1, 16, 10,
            0);
    int eventId = copyer.copyEvent(sourceEvent, sourceCalendar, sameTimezoneTarget,
            targetTime);

    assertTrue(eventId > 0);
    List<Event> copiedEvents = sameTimezoneTarget.getEventsOnDate("2025-01-16");
    assertEquals(1, copiedEvents.size());

    Event copiedEvent = copiedEvents.get(0);
    assertEquals("Meeting", copiedEvent.getSubject());
    assertEquals("Important meeting", copiedEvent.getDescription());
    assertEquals("Conference Room", copiedEvent.getLocation());
    assertEquals(Status.PUBLIC, copiedEvent.getStatus());
    assertEquals(LocalDateTime.of(2025, 1, 16, 10, 0),
            copiedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 1, 16, 11, 0),
            copiedEvent.getEndDateTime());
  }

  @Test
  public void testCopyEventDifferentTimezone() {
    Event sourceEvent = new Event.Builder()
            .subject("Meeting")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 14, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))
            .status(Status.PUBLIC)
            .build();

    LocalDateTime targetTime = LocalDateTime.of(2025, 1, 16, 11,
            0); // PST
    int eventId = copyer.copyEvent(sourceEvent, sourceCalendar, targetCalendar, targetTime);

    assertTrue(eventId > 0);
    List<Event> copiedEvents = targetCalendar.getEventsOnDate("2025-01-16");
    assertEquals(1, copiedEvents.size());
    assertEquals(LocalDateTime.of(2025, 1, 16, 11, 0), copiedEvents.get(0).getStartDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventInvalidDuration() {
    Event sourceEvent = new Event.Builder()
            .subject("Invalid")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 14, 0)) // End before start
            .status(Status.PUBLIC)
            .build();

    copyer.copyEvent(sourceEvent, sourceCalendar, targetCalendar, LocalDateTime.now());
  }

  @Test
  public void testCopyEventsOnDateSameTimezone() {
    Calendar sameTimezoneTarget = new Calendar("Target", ZoneId.of("America/New_York"));

    Event event1 = new Event.Builder()
            .subject("Event1")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 11, 0))
            .status(Status.PUBLIC)
            .build();

    Event event2 = new Event.Builder()
            .subject("Event2")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 14, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))
            .status(Status.PRIVATE)
            .build();

    List<Event> sourceEvents = Arrays.asList(event1, event2);
    LocalDate targetDate = LocalDate.of(2025, 1, 16);

    List<Integer> copiedIds = copyer.copyEventsOnDate(sourceEvents, sourceCalendar,
            sameTimezoneTarget, targetDate);

    assertEquals(2, copiedIds.size());
    List<Event> targetEvents = sameTimezoneTarget.getEventsOnDate("2025-01-16");
    assertEquals(2, targetEvents.size());
  }

  @Test
  public void testCopyEventsOnDateDifferentTimezone() {
    Event sourceEvent = new Event.Builder()
            .subject("EST Event")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 14, 0)) // 2 PM EST
            .endDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))   // 3 PM EST
            .status(Status.PUBLIC)
            .build();

    List<Event> sourceEvents = Arrays.asList(sourceEvent);
    LocalDate targetDate = LocalDate.of(2025, 1, 16);

    List<Integer> copiedIds = copyer.copyEventsOnDate(sourceEvents, sourceCalendar,
            targetCalendar, targetDate);

    assertEquals(1, copiedIds.size());
    List<Event> targetEvents = targetCalendar.getEventsOnDate("2025-01-16");
    assertEquals(1, targetEvents.size());

    // Should be converted to PST time (11 AM PST)
    Event copiedEvent = targetEvents.get(0);
    assertEquals(11, copiedEvent.getStartDateTime().getHour());
    assertEquals(12, copiedEvent.getEndDateTime().getHour());
  }

  @Test
  public void testCopyEventsInRange() {
    Event event1 = new Event.Builder()
            .subject("Event1")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 11, 0))
            .status(Status.PUBLIC)
            .build();

    Event event2 = new Event.Builder()
            .subject("Event2")
            .startDateTime(LocalDateTime.of(2025, 1, 17, 14, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 17, 15, 0))
            .status(Status.PUBLIC)
            .build();

    List<Event> sourceEvents = Arrays.asList(event1, event2);
    LocalDate sourceStartDate = LocalDate.of(2025, 1, 15);
    LocalDate targetStartDate = LocalDate.of(2025, 2, 1);

    List<Integer> copiedIds = copyer.copyEventsInRange(sourceEvents, sourceCalendar,
            targetCalendar, sourceStartDate, targetStartDate);

    assertEquals(2, copiedIds.size());

    // Event1 should be on 2025-02-01 (same offset as original)
    List<Event> feb1Events = targetCalendar.getEventsOnDate("2025-02-01");
    assertEquals(1, feb1Events.size());
    assertEquals("Event1", feb1Events.get(0).getSubject());

    // Event2 should be on 2025-02-03 (2 days offset from source start)
    List<Event> feb3Events = targetCalendar.getEventsOnDate("2025-02-03");
    assertEquals(1, feb3Events.size());
    assertEquals("Event2", feb3Events.get(0).getSubject());
  }

  @Test
  public void testCopyEventSeries() {
    Event event1 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 11, 0))
            .seriesId(1)
            .occurrenceIndex(0)
            .status(Status.PUBLIC)
            .build();

    Event event2 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 17, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 17, 11, 0))
            .seriesId(1)
            .occurrenceIndex(1)
            .status(Status.PUBLIC)
            .build();

    List<Event> seriesEvents = Arrays.asList(event2, event1); // Unsorted on purpose
    LocalDate targetStartDate = LocalDate.of(2025, 2, 1);

    List<Integer> copiedIds = copyer.copyEventSeries(seriesEvents, sourceCalendar,
            targetCalendar, targetStartDate);

    assertEquals(2, copiedIds.size());

    // Events should be copied maintaining their relative positions
    List<Event> feb1Events = targetCalendar.getEventsOnDate("2025-02-01");
    List<Event> feb3Events = targetCalendar.getEventsOnDate("2025-02-03");
    assertEquals(1, feb1Events.size());
    assertEquals(1, feb3Events.size());
  }

  @Test
  public void testGetCopySummary() {
    Event sourceEvent = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 11, 0))
            .status(Status.PUBLIC)
            .build();

    List<Event> sourceEvents = Arrays.asList(sourceEvent);
    String summary = copyer.getCopySummary(sourceEvents, sourceCalendar, targetCalendar);

    assertNotNull(summary);
    assertTrue(summary.contains("Copy Summary"));
    assertTrue(summary.contains("Source: Source"));
    assertTrue(summary.contains("Target: Target"));
    assertTrue(summary.contains("Events to copy: 1"));
    assertTrue(summary.contains("Timezone conversion will be applied"));
  }

  @Test
  public void testGetCopySummaryNoEvents() {
    String summary = copyer.getCopySummary(Arrays.asList(), sourceCalendar, targetCalendar);
    assertEquals("No events to copy", summary);
  }
}
