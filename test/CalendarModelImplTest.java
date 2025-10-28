import model.CalendarModel;
import model.CalendarModelImpl;
import model.Event;
import model.Status;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A test class for thr model implementation.
 */
public class CalendarModelImplTest {
  private CalendarModel model;
  private static final DateTimeFormatter DT_FMT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter D_FMT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Before
  public void setUp() {
    model = new CalendarModelImpl();
  }

  @Test
  public void testCreateSingleEvent() {
    int eid = model.createSingleEvent("Meeting", "Project",
            "2025-01-01", "10:00", "2025-01-01", "11:00", "Room", Status.PUBLIC);
    assertTrue(eid > 0);
  }

  @Test
  public void testCreateSingleEventWithDefaults() {
    int eid = model.createSingleEvent("Meeting", "", "2025-01-01",
            "10:00", "", "", "", Status.PUBLIC);
    assertTrue(eid > 0);
    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertEquals(1, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventDuplicate() {
    model.createSingleEvent("Meeting", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Meeting", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventEndBeforeStart() {
    model.createSingleEvent("Invalid", "", "2025-01-01",
            "11:00", "2025-01-01", "10:00", "", Status.PUBLIC);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventInvalidDate() {
    model.createSingleEvent("Invalid", "", "2025-13-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
  }

  @Test
  public void testCreateAllDayEvent() {
    int eid = model.createAllDayEvent("Holiday",
            "Vacation", "2025-12-25", "Home", Status.PRIVATE);
    assertTrue(eid > 0);

    List<Event> events = model.getEventsOnDate("2025-12-25");
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals("08:00", event.getStartDateTime().toLocalTime().toString());
    assertEquals("17:00", event.getEndDateTime().toLocalTime().toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateAllDayEventDuplicate() {
    model.createAllDayEvent("Holiday", "", "2025-12-25", "",
            Status.PUBLIC);
    model.createAllDayEvent("Holiday", "", "2025-12-25", "",
            Status.PUBLIC);
  }

  @Test
  public void testCreateTimedEventSeries() {
    int sid = model.createTimedEventSeries("Class", "Math", "2025-01-01",
            "09:00", "2025-01-01", "10:00",
            "MWF", 5, null, "Room", Status.PUBLIC);
    assertTrue(sid > 0);

    List<Event> events = model.getEventsInRange("2025-01-01T00:00", "2025-01-31T23:59");
    assertEquals(5, events.size());

    for (int i = 0; i < events.size(); i++) {
      assertEquals((Integer) sid, events.get(i).getSeriesId());
      assertEquals(i, events.get(i).getOccurrenceIndex());
    }
  }

  @Test
  public void testCreateTimedEventSeriesWithUntilDate() {
    int sid = model.createTimedEventSeries("Class", "", "2025-01-06",
            "09:00", "2025-01-06", "10:00", "MWF",
            0, "2025-01-15", "", Status.PUBLIC);
    assertTrue(sid > 0);

    List<Event> events = model.getEventsInRange("2025-01-01T00:00", "2025-01-31T23:59");
    assertTrue(events.size() > 1);
  }

  @Test
  public void testCreateAllDayEventSeries() {
    int sid = model.createAllDayEventSeries("Conference", "Tech",
            "2025-06-02", "MTWRF", 3, null, "Venue",
            Status.PUBLIC);
    assertTrue(sid > 0);

    List<Event> events = model.getEventsInRange("2025-06-01T00:00", "2025-06-30T23:59");
    assertEquals(3, events.size());

    for (Event event : events) {
      assertEquals("08:00", event.getStartDateTime().toLocalTime().toString());
      assertEquals("17:00", event.getEndDateTime().toLocalTime().toString());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTimedEventSeriesInvalidWeekdays() {
    model.createTimedEventSeries("Class", "", "2025-01-01",
            "09:00", "2025-01-01", "10:00", "MXF",
            5, null, "", Status.PUBLIC);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTimedEventSeriesSpansMultipleDays() {
    model.createTimedEventSeries("Invalid", "", "2025-01-01",
            "23:00", "2025-01-02", "01:00", "MWF",
            3, null, "", Status.PUBLIC);
  }

  @Test
  public void testEditSingleEventSubject() {
    model.createSingleEvent("Old Subject", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertTrue(model.editSingleEvent("Old Subject", "2025-01-01T10:00",
            "subject", "New Subject"));

    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertEquals("New Subject", events.get(0).getSubject());
  }

  @Test
  public void testEditSingleEventTime() {
    model.createSingleEvent("Event", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertTrue(model.editSingleEvent("Event", "2025-01-01T10:00",
            "start", "2025-01-01T10:30"));

    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertEquals("10:30", events.get(0).getStartDateTime().toLocalTime().toString());
  }

  @Test
  public void testEditEventsFrom() {
    int sid = model.createTimedEventSeries("Series", "",
            "2025-01-01", "09:00", "2025-01-01", "10:00",
            "MWF", 3, null, "", Status.PUBLIC);
    int modified = model.editEventsFrom("Series", "2025-01-01T09:00",
            "subject", "Changed");
    assertEquals(3, modified);
  }

  @Test
  public void testEditEntireSeries() {
    int sid = model.createTimedEventSeries("Series", "", "2025-01-01",
            "09:00", "2025-01-01", "10:00", "MWF",
            3, null, "", Status.PUBLIC);
    int modified = model.editEntireSeries("Series", "2025-01-01T09:00",
            "location", "Online");
    assertEquals(3, modified);
  }

  @Test
  public void testEditTimeRemovesFromSeries() {
    int sid = model.createTimedEventSeries("Series", "", "2025-01-01",
            "09:00", "2025-01-01", "10:00", "MWF",
            3, null, "", Status.PUBLIC);
    model.editSingleEvent("Series", "2025-01-01T09:00", "start",
            "2025-01-01T09:30");

    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertNull(events.get(0).getSeriesId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditInvalidProperty() {
    model.createSingleEvent("Event", "", "2025-01-01", "10:00",
            "2025-01-01", "11:00", "", Status.PUBLIC);
    model.editSingleEvent("Event", "2025-01-01T10:00", "invalid",
            "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistingEvent() {
    model.editSingleEvent("Nonexistent", "2025-01-01T10:00",
            "subject", "New");
  }

  @Test
  public void testDeleteSingleEvent() {
    model.createSingleEvent("Temp", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertTrue(model.deleteSingleEvent("Temp", "2025-01-01T10:00"));

    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertTrue(events.isEmpty());
  }

  @Test
  public void testDeleteEventsFrom() {
    int sid = model.createTimedEventSeries("Series", "", "2025-01-01",
            "09:00", "2025-01-01", "10:00", "MWF",
            3, null, "", Status.PUBLIC);
    int deleted = model.deleteEventsFrom("Series", "2025-01-01T09:00");
    assertEquals(3, deleted);
  }

  @Test
  public void testDeleteEntireSeries() {
    int sid = model.createTimedEventSeries("Series", "", "2025-01-01",
            "09:00", "2025-01-01", "10:00", "MWF",
            3, null, "", Status.PUBLIC);
    int deleted = model.deleteEntireSeries("Series", "2025-01-01T09:00");
    assertEquals(3, deleted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteNonExistentEvent() {
    model.deleteSingleEvent("Nonexistent", "2025-01-01T10:00");
  }

  @Test
  public void testGetEventsOnDate() {
    model.createSingleEvent("Event1", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    model.createAllDayEvent("Event2", "", "2025-01-01", "",
            Status.PUBLIC);

    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsOnDateEmpty() {
    List<Event> events = model.getEventsOnDate("2025-01-01");
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInRange() {
    model.createSingleEvent("Event1", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Event2", "", "2025-01-02",
            "10:00", "2025-01-02", "11:00", "", Status.PUBLIC);

    List<Event> events = model.getEventsInRange("2025-01-01T00:00", "2025-01-02T23:59");
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsInRangeEmpty() {
    List<Event> events = model.getEventsInRange("2025-01-01T00:00", "2025-01-02T23:59");
    assertTrue(events.isEmpty());
  }

  @Test
  public void testMultiDayEventQuery() {
    model.createSingleEvent("Long", "", "2025-01-01",
            "10:00", "2025-01-03", "11:00", "", Status.PUBLIC);
    List<Event> events = model.getEventsOnDate("2025-01-02");
    assertEquals(1, events.size());
  }

  @Test
  public void testIsBusyTrue() {
    model.createSingleEvent("Meeting", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertTrue(model.isBusy("2025-01-01T10:30"));
  }

  @Test
  public void testIsBusyFalse() {
    model.createSingleEvent("Meeting", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertFalse(model.isBusy("2025-01-01T09:30"));
  }

  @Test
  public void testIsBusyBoundaryStart() {
    model.createSingleEvent("Event", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertTrue(model.isBusy("2025-01-01T10:00"));
  }

  @Test
  public void testIsBusyBoundaryEnd() {
    model.createSingleEvent("Event", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    assertFalse(model.isBusy("2025-01-01T11:00"));
  }

  @Test
  public void testIsBusyAllDayEvent() {
    model.createAllDayEvent("Holiday", "", "2025-12-25", "",
            Status.PUBLIC);
    assertTrue(model.isBusy("2025-12-25T12:00"));
    assertFalse(model.isBusy("2025-12-26T12:00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventConflictDetection() {
    model.createSingleEvent("First", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Conflict", "", "2025-01-01",
            "10:30", "2025-01-01", "11:30", "", Status.PUBLIC);
  }

  @Test
  public void testNoConflictAdjacentEvents() {
    model.createSingleEvent("First", "", "2025-01-01",
            "10:00", "2025-01-01", "11:00", "", Status.PUBLIC);
    int eid = model.createSingleEvent("Second", "", "2025-01-01",
            "11:00", "2025-01-01", "12:00", "", Status.PUBLIC);
    assertTrue(eid > 0);
  }

  @Test
  public void testComplexSeriesEditingScenario() {
    int sid = model.createTimedEventSeries("First", "", "2025-05-05",
            "10:00", "2025-05-05", "11:00", "MW",
            6, null, "", Status.PUBLIC);

    List<Event> allEvents = model.getEventsInRange("2025-05-01T00:00", "2025-05-31T23:59");
    assertEquals("Should create 6 events initially", 6, allEvents.size());

    int modified1 = model.editEventsFrom("First", "2025-05-12T10:00",
            "subject", "Second");
    assertTrue("Should modify some events", modified1 > 0);

    List<Event> eventsAfterFirst = model.getEventsInRange("2025-05-01T00:00", "2025-05-31T23:59");
    long firstEvents = eventsAfterFirst.stream().filter(e ->
            "First".equals(e.getSubject())).count();
    long secondEvents = eventsAfterFirst.stream().filter(e ->
            "Second".equals(e.getSubject())).count();

    assertEquals("Should have remaining First events", 6 - modified1,
            (int) firstEvents);
    assertEquals("Should have Second events", modified1, (int) secondEvents);

    if (firstEvents > 0) {
      model.editEntireSeries("First", "2025-05-05T10:00", "subject",
              "Third");
    }

    if (secondEvents > 0) {
      Event secondEvent = eventsAfterFirst.stream()
              .filter(e -> "Second".equals(e.getSubject()))
              .findFirst()
              .orElse(null);
      if (secondEvent != null) {
        String secondDateTime = secondEvent.getStartDateTime().format(DT_FMT);
        model.editEntireSeries("Second", secondDateTime, "subject", "Third");
      }
    }

    List<Event> finalEvents = model.getEventsInRange("2025-05-01T00:00", "2025-05-31T23:59");
    assertEquals("Should still have 6 events total", 6, finalEvents.size());
    for (Event event : finalEvents) {
      assertEquals("All events should be renamed to Third", "Third",
              event.getSubject());
    }
  }
}