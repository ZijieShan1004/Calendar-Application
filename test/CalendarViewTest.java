import view.CalendarView;
import model.Event;
import model.Status;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A test class for the view implementation of a calendar.
 */
public class CalendarViewTest {
  private CalendarView view;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;

  @Before
  public void setUp() {
    view = new CalendarView();
    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  private String getOutput() {
    return outputStream.toString();
  }

  private Event createTestEvent(String subject, LocalDateTime start,
                                LocalDateTime end, String location, Status status) {
    return new Event.Builder()
            .subject(subject)
            .startDateTime(start)
            .endDateTime(end)
            .location(location)
            .status(status)
            .build();
  }

  @Test
  public void testPrintEventsOnDateWithEvents() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent("Meeting",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Conference Room", Status.PUBLIC));
    events.add(createTestEvent("Lunch",
            LocalDateTime.of(2025, 1, 1, 12, 0),
            LocalDateTime.of(2025, 1, 1, 13, 0),
            "", Status.PRIVATE));

    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    assertTrue("Should show date header", output.contains("Events on 2025-01-01:"));
    assertTrue("Should show first event", output.contains("Meeting"));
    assertTrue("Should show second event", output.contains("Lunch"));
    assertTrue("Should show time range", output.contains("10:00") && output.contains("11:00"));
    assertTrue("Should show location", output.contains("Conference Room"));
    assertTrue("Should show N/A for empty location", output.contains("N/A"));
    assertTrue("Should show status", output.contains("PUBLIC") && output.contains("PRIVATE"));
  }

  @Test
  public void testPrintEventsOnDateEmpty() {
    List<Event> events = new ArrayList<>();
    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    assertTrue("Should show date header", output.contains("Events on 2025-01-01:"));
    assertTrue("Should show no events message", output.contains("(no events)"));
  }

  @Test
  public void testPrintEventsOnDateFormatting() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent("Test Event",
            LocalDateTime.of(2025, 1, 1, 9, 30),
            LocalDateTime.of(2025, 1, 1, 10, 45),
            "Room 123", Status.PUBLIC));

    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    assertTrue("Should format datetime correctly", output.contains("2025-01-01T09:30"));
    assertTrue("Should format datetime correctly", output.contains("2025-01-01T10:45"));
    assertTrue("Should use proper bullet format", output.contains("  - ["));
    assertTrue("Should show tilde separator", output.contains("~"));
  }

  @Test
  public void testPrintEventsInRangeWithEvents() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent("Event 1",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Location 1", Status.PUBLIC));
    events.add(createTestEvent("Event 2",
            LocalDateTime.of(2025, 1, 2, 14, 0),
            LocalDateTime.of(2025, 1, 2, 15, 0),
            "Location 2", Status.PRIVATE));

    view.printEventsInRange("2025-01-01T00:00", "2025-01-02T23:59", events);
    String output = getOutput();

    assertTrue("Should show range header",
            output.contains("Events from 2025-01-01T00:00 to 2025-01-02T23:59:"));
    assertTrue("Should show both events",
            output.contains("Event 1") && output.contains("Event 2"));
    assertTrue("Should show both locations",
            output.contains("Location 1") && output.contains("Location 2"));
  }

  @Test
  public void testPrintEventsInRangeEmpty() {
    List<Event> events = new ArrayList<>();
    view.printEventsInRange("2025-01-01T00:00", "2025-01-02T23:59", events);
    String output = getOutput();

    assertTrue("Should show range header",
            output.contains("Events from 2025-01-01T00:00 to 2025-01-02T23:59:"));
    assertTrue("Should show no events message", output.contains("(no events)"));
  }

  @Test
  public void testPrintStatusBusy() {
    view.printStatus("2025-01-01T10:30", true);
    String output = getOutput();

    assertTrue("Should show datetime", output.contains("Status at 2025-01-01T10:30:"));
    assertTrue("Should show busy status", output.contains("Busy"));
  }

  @Test
  public void testPrintStatusAvailable() {
    view.printStatus("2025-01-01T10:30", false);
    String output = getOutput();

    assertTrue("Should show datetime", output.contains("Status at 2025-01-01T10:30:"));
    assertTrue("Should show available status", output.contains("Available"));
  }

  @Test
  public void testPrintSuccess() {
    view.printSuccess("Event created successfully");
    String output = getOutput();

    assertTrue("Should show success prefix", output.contains("Success:"));
    assertTrue("Should show message", output.contains("Event created successfully"));
  }

  @Test
  public void testPrintSuccessEmpty() {
    view.printSuccess("");
    String output = getOutput();

    assertTrue("Should show success prefix", output.contains("Success:"));
  }

  @Test
  public void testPrintError() {
    view.printError("Invalid command format");
    String output = getOutput();

    assertTrue("Should show error prefix", output.contains("Error:"));
    assertTrue("Should show message", output.contains("Invalid command format"));
  }

  @Test
  public void testPrintErrorEmpty() {
    view.printError("");
    String output = getOutput();

    assertTrue("Should show error prefix", output.contains("Error:"));
  }

  @Test
  public void testPrintEventsWithSpecialCharacters() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent("Meeting with 🎯 Special Characters & Symbols",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Room #123 @Building-A", Status.PUBLIC));

    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    assertTrue("Should handle special characters in subject", output.contains("🎯"));
    assertTrue("Should handle special characters in location", output.contains("#123"));
    assertTrue("Should handle ampersand", output.contains("&"));
  }

  @Test
  public void testPrintEventsWithLongNames() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent(
            "Very Long Event Name That Might Cause Formatting Issues In The Output",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Very Long Location Name That Also Might Cause Issues", Status.PUBLIC));

    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    assertTrue("Should handle long subject names",
            output.contains("Very Long Event Name"));
    assertTrue("Should handle long location names",
            output.contains("Very Long Location Name"));
  }

  @Test
  public void testMultipleEventsFormatting() {
    List<Event> events = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      events.add(createTestEvent("Event " + i,
              LocalDateTime.of(2025, 1, 1, 9 + i, 0),
              LocalDateTime.of(2025, 1, 1, 10 + i, 0),
              "Room " + i, Status.PUBLIC));
    }

    view.printEventsOnDate("2025-01-01", events);
    String output = getOutput();

    String[] lines = output.split("\n");
    int bulletCount = 0;
    for (String line : lines) {
      if (line.trim().startsWith("- [")) {
        bulletCount++;
      }
    }
    assertEquals("Should have 5 bullet points", 5, bulletCount);

    for (int i = 1; i <= 5; i++) {
      assertTrue("Should show Event " + i, output.contains("Event " + i));
      assertTrue("Should show Room " + i, output.contains("Room " + i));
    }
  }

  @Test
  public void testEventFormattingConsistency() {
    List<Event> events = new ArrayList<>();
    events.add(createTestEvent("Event",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Location", Status.PUBLIC));

    view.printEventsOnDate("2025-01-01", events);
    String dateOutput = getOutput();

    outputStream.reset();

    view.printEventsInRange("2025-01-01T00:00", "2025-01-01T23:59", events);
    String rangeOutput = getOutput();

    String eventLine = "  - [2025-01-01T10:00~2025-01-01T11:00] Event " +
            "(loc: Location, status: PUBLIC)";
    assertTrue("Date view should contain formatted event",
            dateOutput.contains(eventLine));
    assertTrue("Range view should contain formatted event",
            rangeOutput.contains(eventLine));
  }
}
