import controller.CalendarController;

import model.CalendarModel;
import model.CalendarModelImpl;
import view.CalendarView;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A test class for the controller implementation.
 */
public class CalendarControllerTest {
  private CalendarController controller;
  private ByteArrayOutputStream outputStream;
  private PrintStream originalOut;

  @Before
  public void setUp() {
    CalendarModel model = new CalendarModelImpl();
    CalendarView view = new CalendarView();
    controller = new CalendarController(model, view);

    outputStream = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  public void testExitCommand() {
    boolean result = controller.processCommand("exit");
    assertFalse("Exit command should return false", result);
  }

  @Test
  public void testExitCommandCaseInsensitive() {
    boolean result = controller.processCommand("EXIT");
    assertFalse("Exit command should be case insensitive", result);
  }

  @Test
  public void testEmptyCommand() {
    boolean result = controller.processCommand("");
    assertTrue("Empty command should return true", result);
  }

  @Test
  public void testNullCommand() {
    boolean result = controller.processCommand(null);
    assertFalse("Null command should return false", result);
  }

  @Test
  public void testCreateSingleEventCommand() {
    boolean result = controller.processCommand("create event \"Meeting\" " +
            "from 2025-01-01T10:00 to 2025-01-01T11:00");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention event creation", output.contains("Created single event"));
  }

  @Test
  public void testCreateSingleEventCommandQuotedSubject() {
    boolean result = controller.processCommand("create event \"Project Planning Meeting\" " +
            "from 2025-01-01T10:00 to 2025-01-01T11:00");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
  }

  @Test
  public void testCreateEventSeriesWithRepetition() {
    boolean result = controller.processCommand("create event \"Class\" " +
            "from 2025-01-01T09:00 to 2025-01-01T10:00 repeats MWF for 5 times");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention series creation",
            output.contains("Created timed event series"));
  }

  @Test
  public void testCreateEventSeriesWithUntilDate() {
    boolean result = controller.processCommand("create event \"Workshop\" " +
            "from 2025-01-01T14:00 to 2025-01-01T16:00 repeats MW until 2025-01-15");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
  }

  @Test
  public void testCreateAllDayEvent() {
    boolean result = controller.processCommand("create event \"Holiday\" on 2025-12-25");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention all-day event", output.contains("Created all-day event"));
  }

  @Test
  public void testCreateAllDayEventSeries() {
    boolean result = controller.processCommand("create event \"Training\"" +
            " on 2025-01-06 repeats MTWRF for 5 times");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention all-day series",
            output.contains("Created all-day event series"));
  }

  @Test
  public void testCreateAllDayEventSeriesUntilDate() {
    boolean result = controller.processCommand("create event \"Conference\" " +
            "on 2025-06-01 repeats MTWRF until 2025-06-05");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
  }

  @Test
  public void testEditSingleEventSubject() {
    controller.processCommand("create event \"Old Name\" from 2025-01-01T10:00 " +
            "to 2025-01-01T11:00");
    outputStream.reset();

    boolean result = controller.processCommand("edit event subject \"Old Name\" " +
            "from 2025-01-01T10:00 to New Name");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention edit", output.contains("Edited single event"));
  }

  @Test
  public void testEditEventsFromSeries() {
    controller.processCommand("create event \"Series\" from 2025-01-01T09:00 to " +
            "2025-01-01T10:00 repeats MWF for 3 times");
    outputStream.reset();

    boolean result = controller.processCommand("edit events location \"Series\" " +
            "from 2025-01-01T09:00 with New Location");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention events edited", output.contains("Edited")
            && output.contains("events from"));
  }

  @Test
  public void testEditEntireSeries() {
    controller.processCommand("create event \"Series\" from 2025-01-01T09:00 to " +
            "2025-01-01T10:00 repeats MWF for 3 times");
    outputStream.reset();

    boolean result = controller.processCommand("edit series description \"Series\" " +
            "from 2025-01-01T09:00 with Updated Description");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention series edited", output.contains("Edited entire series"));
  }

  @Test
  public void testDeleteSingleEvent() {
    controller.processCommand("create event \"Temporary\" from 2025-01-01T10:00 " +
            "to 2025-01-01T11:00");
    outputStream.reset();

    boolean result = controller.processCommand("delete event \"Temporary\" from " +
            "2025-01-01T10:00");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention deletion", output.contains("Deleted single event"));
  }

  @Test
  public void testDeleteEventsFrom() {
    controller.processCommand("create event \"Series\" from 2025-01-01T09:00 to " +
            "2025-01-01T10:00 repeats MWF for 5 times");
    outputStream.reset();

    boolean result = controller.processCommand("delete events \"Series\" from " +
            "2025-01-01T09:00");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention events deleted", output.contains("Deleted")
            && output.contains("events from"));
  }

  @Test
  public void testDeleteEntireSeries() {
    controller.processCommand("create event \"Series\" from 2025-01-01T09:00 to " +
            "2025-01-01T10:00 repeats MWF for 3 times");
    outputStream.reset();

    boolean result = controller.processCommand("delete series \"Series\" from " +
            "2025-01-01T09:00");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
    assertTrue("Should mention series deleted", output.contains("Deleted entire series"));
  }

  @Test
  public void testPrintEventsOnDate() {
    controller.processCommand("create event \"Event1\" from 2025-01-01T10:00 to " +
            "2025-01-01T11:00");
    controller.processCommand("create event \"Event2\" on 2025-01-01");
    outputStream.reset();

    boolean result = controller.processCommand("print events on 2025-01-01");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show events", output.contains("Events on 2025-01-01"));
    assertTrue("Should list events", output.contains("Event1")
            || output.contains("Event2"));
  }

  @Test
  public void testPrintEventsOnDateEmpty() {
    boolean result = controller.processCommand("print events on 2025-01-01");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show no events message", output.contains("no events")
            || output.contains("Events on 2025-01-01"));
  }

  @Test
  public void testPrintEventsInRange() {
    controller.processCommand("create event \"Event1\" from 2025-01-01T10:00 to " +
            "2025-01-01T11:00");
    controller.processCommand("create event \"Event2\" from 2025-01-02T14:00 to " +
            "2025-01-02T15:00");
    outputStream.reset();

    boolean result = controller.processCommand("print events from 2025-01-01T00:00 to" +
            " 2025-01-02T23:59");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show events in range", output.contains("Events from"));
  }

  @Test
  public void testShowStatusBusy() {
    controller.processCommand("create event \"Meeting\" from 2025-01-01T10:00 to " +
            "2025-01-01T11:00");
    outputStream.reset();

    boolean result = controller.processCommand("show status on 2025-01-01T10:30");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show busy status", output.contains("Status at")
            && output.contains("Busy"));
  }

  @Test
  public void testShowStatusAvailable() {
    boolean result = controller.processCommand("show status on 2025-01-01T10:30");
    assertTrue("Command should succeed", result);

    String output = outputStream.toString();
    assertTrue("Should show available status", output.contains("Status at")
            && output.contains("Available"));
  }

  @Test
  public void testInvalidCommand() {
    boolean result = controller.processCommand("invalid command format");
    assertTrue("Should continue processing", result);

    String output = outputStream.toString();
    assertTrue("Should show error message", output.contains("Error")
            && output.contains("Invalid command format"));
  }

  @Test
  public void testCreateEventWithInvalidFormat() {
    boolean result = controller.processCommand("create event invalid format");
    assertTrue("Should continue processing", result);

    String output = outputStream.toString();
    assertTrue("Should show error message", output.contains("Error"));
  }

  @Test
  public void testEditNonExistentEvent() {
    boolean result = controller.processCommand("edit event subject \"Nonexistent\" from " +
            "2025-01-01T10:00 to New Name");
    assertTrue("Should continue processing", result);

    String output = outputStream.toString();
    assertTrue("Should show error message", output.contains("Error"));
  }

  @Test
  public void testDeleteNonExistentEvent() {
    boolean result = controller.processCommand("delete event \"Nonexistent\" from " +
            "2025-01-01T10:00");
    assertTrue("Should continue processing", result);

    String output = outputStream.toString();
    assertTrue("Should show error message", output.contains("Error"));
  }

  @Test
  public void testCreateEventCommandCaseInsensitive() {
    boolean result = controller.processCommand("CREATE EVENT \"Meeting\" FROM " +
            "2025-01-01T10:00 TO 2025-01-01T11:00");
    assertTrue("Command should be case insensitive", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
  }

  @Test
  public void testEditCommandCaseInsensitive() {
    controller.processCommand("create event \"Test\" from 2025-01-01T10:00 to " +
            "2025-01-01T11:00");
    outputStream.reset();

    boolean result = controller.processCommand("EDIT EVENT SUBJECT \"Test\" FROM " +
            "2025-01-01T10:00 TO New Name");
    assertTrue("Command should be case insensitive", result);

    String output = outputStream.toString();
    assertTrue("Should show success message", output.contains("Success"));
  }

  @Test
  public void testComplexWorkflow() {
    assertTrue("Create series should succeed",
            controller.processCommand("create event \"Class\" from 2025-01-01T09:00" +
                    " to 2025-01-01T10:00 repeats MWF for 3 times"));

    assertTrue("Edit single event should succeed",
            controller.processCommand("edit event location \"Class\" from 2025-01-01T09:00 " +
                    "to Room 101"));

    assertTrue("Edit events from should succeed",
            controller.processCommand("edit events description \"Class\" " +
                    "from 2025-01-01T09:00 with Updated description"));

    assertTrue("Print events should succeed",
            controller.processCommand("print events from 2025-01-01T00:00 " +
                    "to 2025-01-10T23:59"));

    assertTrue("Show status should succeed",
            controller.processCommand("show status on 2025-01-01T09:30"));

    assertTrue("Delete events from should succeed",
            controller.processCommand("delete events \"Class\" from 2025-01-01T09:00"));
  }

  @Test
  public void testWhiteSpaceHandling() {
    boolean result = controller.processCommand("create event \"Whitespace Meeting\" " +
            "from 2025-01-15T10:00 to 2025-01-15T11:00");
    assertTrue("Should handle command", result);

    String output = outputStream.toString();
    assertTrue("Should show some output", output.length() > 0);
    assertFalse("Should not show error", output.contains("Error"));
  }

  private void tearDown() {
    System.setOut(originalOut);
  }
}