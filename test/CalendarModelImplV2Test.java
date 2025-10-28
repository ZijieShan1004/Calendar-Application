import org.junit.Before;
import org.junit.Test;

import java.util.List;

import model.CalendarModelImplV2;
import model.Event;
import model.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test class for all the new CalendarModel methods.
 */
public class CalendarModelImplV2Test {
  private CalendarModelImplV2 model;

  @Before
  public void setUp() {
    model = new CalendarModelImplV2();
  }

  @Test
  public void testOriginalFunctionalityPreserved() {
    // Test that all original CalendarModel methods still work
    int eventId = model.createSingleEvent("Test Event", "Description",
            "2025-01-15", "10:00", "2025-01-15", "11:00",
            "Location", Status.PUBLIC);
    assertTrue(eventId > 0);

    List<Event> events = model.getEventsOnDate("2025-01-15");
    assertEquals(1, events.size());
    assertEquals("Test Event", events.get(0).getSubject());

    assertTrue(model.isBusy("2025-01-15T10:30"));
    assertFalse(model.isBusy("2025-01-15T09:30"));
  }

  @Test
  public void testDefaultCalendarCreated() {
    // Should have a default calendar already active
    assertNotNull(model.getCurrentCalendarName());
    int eventId = model.createSingleEvent("Default Event", "",
            "2025-01-15", "10:00", "2025-01-15", "11:00",
            "", Status.PUBLIC);
    assertTrue(eventId > 0);
  }

  @Test
  public void testCreateMultipleCalendars() {
    model.createCalendar("Work", "America/New_York");
    model.createCalendar("Personal", "America/Los_Angeles");

    model.useCalendar("Work");
    assertEquals("Work", model.getCurrentCalendarName());

    model.useCalendar("Personal");
    assertEquals("Personal", model.getCurrentCalendarName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarInvalidTimezone() {
    model.createCalendar("Test", "Invalid/Timezone");
  }

  @Test
  public void testEditCalendarProperties() {
    model.createCalendar("Test", "UTC");
    model.editCalendarProperty("Test", "timezone", "America/New_York");
    model.editCalendarProperty("Test", "name", "NewName");

    model.useCalendar("NewName");
    assertEquals("NewName", model.getCurrentCalendarName());
  }

  @Test
  public void testEventIsolationBetweenCalendars() {
    model.createCalendar("Cal1", "UTC");
    model.createCalendar("Cal2", "UTC");

    model.useCalendar("Cal1");
    model.createSingleEvent("Event1", "", "2025-01-15",
            "10:00", "2025-01-15", "11:00", "", Status.PUBLIC);

    model.useCalendar("Cal2");
    model.createSingleEvent("Event2", "", "2025-01-15",
            "14:00", "2025-01-15", "15:00", "", Status.PUBLIC);

    // Cal2 should only see Event2
    List<Event> cal2Events = model.getEventsOnDate("2025-01-15");
    assertEquals(1, cal2Events.size());
    assertEquals("Event2", cal2Events.get(0).getSubject());

    // Cal1 should only see Event1
    model.useCalendar("Cal1");
    List<Event> cal1Events = model.getEventsOnDate("2025-01-15");
    assertEquals(1, cal1Events.size());
    assertEquals("Event1", cal1Events.get(0).getSubject());
  }

  @Test
  public void testCopyEventBetweenCalendars() {
    model.createCalendar("Source", "America/New_York");
    model.createCalendar("Target", "America/Los_Angeles");

    model.useCalendar("Source");
    model.createSingleEvent("Meeting", "Important", "2025-01-15",
            "14:00", "2025-01-15", "15:00", "Office",
            Status.PUBLIC);

    int copiedId = model.copyEvent("Meeting", "2025-01-15T14:00",
            "Target", "2025-01-15T11:00");
    assertTrue(copiedId > 0);

    model.useCalendar("Target");
    List<Event> targetEvents = model.getEventsOnDate("2025-01-15");
    assertEquals(1, targetEvents.size());
    assertEquals("Meeting", targetEvents.get(0).getSubject());
    assertEquals(11, targetEvents.get(0).getStartDateTime().getHour());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyNonExistentEvent() {
    model.createCalendar("Target", "UTC");
    model.copyEvent("NonExistent", "2025-01-15T10:00",
            "Target", "2025-01-15T10:00");
  }

  @Test
  public void testCopyEventsOnDate() {
    model.createCalendar("Source", "UTC");
    model.createCalendar("Target", "UTC");

    model.useCalendar("Source");
    model.createSingleEvent("Event1", "", "2025-01-15",
            "10:00", "2025-01-15", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Event2", "", "2025-01-15",
            "14:00", "2025-01-15", "15:00", "", Status.PUBLIC);

    List<Integer> copiedIds = model.copyEventsOnDate("2025-01-15",
            "Target", "2025-01-16");
    assertEquals(2, copiedIds.size());

    model.useCalendar("Target");
    List<Event> targetEvents = model.getEventsOnDate("2025-01-16");
    assertEquals(2, targetEvents.size());
  }

  @Test
  public void testCopyEventsBetween() {
    model.createCalendar("Source", "UTC");
    model.createCalendar("Target", "UTC");

    model.useCalendar("Source");
    model.createSingleEvent("Event1", "", "2025-01-15",
            "10:00", "2025-01-15", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Event2", "", "2025-01-16",
            "10:00", "2025-01-16", "11:00", "", Status.PUBLIC);
    model.createSingleEvent("Event3", "", "2025-01-18",
            "10:00", "2025-01-18", "11:00", "", Status.PUBLIC);

    List<Integer> copiedIds = model.copyEventsBetween("2025-01-15", "2025-01-16",
            "Target", "2025-02-01");
    assertEquals(2, copiedIds.size()); // Should copy Event1 and Event2, not Event3

    model.useCalendar("Target");
    List<Event> feb1Events = model.getEventsOnDate("2025-02-01");
    List<Event> feb2Events = model.getEventsOnDate("2025-02-02");
    assertEquals(1, feb1Events.size());
    assertEquals(1, feb2Events.size());
    assertEquals("Event1", feb1Events.get(0).getSubject());
    assertEquals("Event2", feb2Events.get(0).getSubject());
  }

  @Test
  public void testAllDayEventSeries() {
    model.createCalendar("Work", "UTC");
    model.useCalendar("Work");

    int seriesId = model.createAllDayEventSeries("Training", "Daily training",
            "2025-01-06", "MTWRF", 5, null,
            "Training Room", Status.PUBLIC);
    assertTrue(seriesId > 0);

    List<Event> allEvents = model.getEventsInRange("2025-01-06T00:00", "2025-01-12T23:59");
    assertEquals(5, allEvents.size());

    for (Event event : allEvents) {
      assertEquals(8, event.getStartDateTime().getHour());
      assertEquals(17, event.getEndDateTime().getHour());
      assertEquals("Training", event.getSubject());
    }
  }

  @Test
  public void testTimedEventSeries() {
    int seriesId = model.createTimedEventSeries("Standup", "Daily standup",
            "2025-01-06", "09:00", "2025-01-06", "09:30",
            "MTWRF", 3, null, "Conference Room", Status.PUBLIC);
    assertTrue(seriesId > 0);

    List<Event> seriesEvents = model.getEventsInRange("2025-01-06T00:00", "2025-01-10T23:59");
    assertEquals(3, seriesEvents.size());

    for (Event event : seriesEvents) {
      assertEquals("Standup", event.getSubject());
      assertEquals(9, event.getStartDateTime().getHour());
      assertEquals(0, event.getStartDateTime().getMinute());
      assertEquals(9, event.getEndDateTime().getHour());
      assertEquals(30, event.getEndDateTime().getMinute());
    }
  }

  @Test
  public void testUtilityMethods() {
    assertTrue(model.isValidTimezone("UTC"));
    assertTrue(model.isValidTimezone("America/New_York"));
    assertFalse(model.isValidTimezone("Invalid/Zone"));

    assertNotNull(model.getAvailableTimezones());
    assertFalse(model.getAvailableTimezones().isEmpty());

    String summary = model.getCalendarSummary();
    assertNotNull(summary);
    assertTrue(summary.contains("Calendar Summary"));
  }

  @Test
  public void testComplexWorkflow() {
    model.createCalendar("Work", "America/New_York");
    model.createCalendar("Personal", "America/Los_Angeles");
    model.createCalendar("Travel", "Europe/London");

    // Add events to work calendar
    model.useCalendar("Work");
    model.createTimedEventSeries("Daily Standup", "", "2025-01-06",
            "09:00",
            "2025-01-06", "09:30", "MTWRF", 5,
            null, "", Status.PUBLIC);

    // Copy work events to travel calendar for business trip
    model.copyEventsOnDate("2025-01-08", "Travel",
            "2025-01-08");

    model.useCalendar("Travel");
    List<Event> travelEvents = model.getEventsOnDate("2025-01-08");
    assertEquals(1, travelEvents.size());
    assertEquals("Daily Standup", travelEvents.get(0).getSubject());

    assertEquals(14, travelEvents.get(0).getStartDateTime().getHour());
  }
}