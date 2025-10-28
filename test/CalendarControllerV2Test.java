import controller.CalendarControllerInterface;
import controller.CalendarControllerV2;
import model.CalendarModel;
import model.CalendarModelImpl;
import model.CalendarModelImplV2;
import model.CalendarModelV2;
import view.CalendarView;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test class for all the new functionalities of the new Controller.
 */
public class CalendarControllerV2Test {
  private CalendarControllerInterface enhancedController;
  private CalendarControllerInterface originalController;
  private ByteArrayOutputStream outputStream;

  @Before
  public void setUp() {
    CalendarModelV2 newModel = new CalendarModelImplV2();
    CalendarModel originalModel = new CalendarModelImpl();
    CalendarView view = new CalendarView();
    enhancedController = new CalendarControllerV2(newModel, view);
    originalController = new CalendarControllerV2(originalModel, view);

    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  public void testOriginalCommandsWithEnhancedModel() {
    // Test that all original commands work with enhanced model
    assertTrue(enhancedController.processCommand("create event " +
            "\"Test\" "
            + "from 2025-01-15T10:00 to 2025-01-15T11:00"));
    assertTrue(enhancedController.processCommand("print events on 2025-01-15"));
    assertTrue(enhancedController.processCommand("show status on 2025-01-15T10:30"));

    String output = outputStream.toString();
    assertTrue(output.contains("Success"));
    assertTrue(output.contains("Test"));
    assertTrue(output.contains("Busy"));
  }

  @Test
  public void testOriginalCommandsWithOriginalModel() {
    // Test that original commands work with original model
    assertTrue(originalController.processCommand("create event \"Original\" " +
            "from 2025-01-15T10:00 " +
            "to 2025-01-15T11:00"));
    assertTrue(originalController.processCommand("print events on 2025-01-15"));
    assertTrue(originalController.processCommand("show status on 2025-01-15T10:30"));

    String output = outputStream.toString();
    assertTrue(output.contains("Success"));
    assertTrue(output.contains("Original"));
    assertTrue(output.contains("Busy"));
  }

  @Test
  public void testEnhancedCommandsWithEnhancedModel() {
    // Test new calendar management commands
    assertTrue(enhancedController.processCommand("create calendar --name Work " +
            "--timezone America/New_York"));
    assertTrue(enhancedController.processCommand("create calendar --name Personal " +
            "--timezone America/Los_Angeles"));
    assertTrue(enhancedController.processCommand("use calendar --name Work"));
    assertTrue(enhancedController.processCommand("edit calendar --name Work " +
            "--property timezone Europe/London"));

    String output = outputStream.toString();
    assertTrue(output.contains("Created calendar 'Work'"));
    assertTrue(output.contains("Created calendar 'Personal'"));
    assertTrue(output.contains("Now using calendar 'Work'"));
    assertTrue(output.contains("Updated timezone"));
  }

  @Test
  public void testEnhancedCommandsWithOriginalModel() {
    // Enhanced commands should be ignored with original model
    assertTrue(originalController.processCommand("create calendar --name Work --timezone UTC"));
    assertTrue(originalController.processCommand("use calendar --name Work"));

    // Should not crash, but commands are ignored
    String output = outputStream.toString();
    assertFalse(output.contains("Created calendar"));
    assertFalse(output.contains("Now using calendar"));
  }

  @Test
  public void testEventCopyingCommands() {
    enhancedController.processCommand("create calendar --name Source " +
            "--timezone America/New_York");
    enhancedController.processCommand("create calendar --name Target " +
            "--timezone America/Los_Angeles");
    enhancedController.processCommand("use calendar --name Source");
    enhancedController.processCommand("create event \"Meeting\" from 2025-01-15T14:00 " +
            "to 2025-01-15T15:00");
    outputStream.reset();

    // Test copying commands
    assertTrue(enhancedController.processCommand("copy event Meeting on 2025-01-15T14:00 " +
            "--target Target to 2025-01-15T11:00"));
    assertTrue(enhancedController.processCommand("copy events on 2025-01-15 --target Target " +
            "to 2025-01-16"));
    assertTrue(enhancedController.processCommand("copy events between 2025-01-15 and 2025-01-15" +
            " --target Target to 2025-01-17"));

    String output = outputStream.toString();
    assertTrue(output.contains("Copied event 'Meeting'"));
    assertTrue(output.contains("Copied 1 events to calendar 'Target'"));
    assertTrue(output.contains("Copied 1 events from date range"));
  }

  @Test
  public void testRepeatingEventCommands() {
    assertTrue(enhancedController.processCommand("create event \"Standup\" from 2025-01-06T09:00" +
            " to 2025-01-06T09:30 repeats MTWRF for 5 times"));
    assertTrue(enhancedController.processCommand("create event \"Training\" on 2025-01-06 " +
            "repeats MTWRF until 2025-01-10"));

    String output = outputStream.toString();
    assertTrue(output.contains("Created timed event series 'Standup'"));
    assertTrue(output.contains("Created all-day event series 'Training'"));
  }

  @Test
  public void testEditCommands() {
    enhancedController.processCommand("create event \"Original\" from 2025-01-15T10:00 " +
            "to 2025-01-15T11:00");
    outputStream.reset();

    assertTrue(enhancedController.processCommand("edit event subject \"Original\" " +
            "from 2025-01-15T10:00 to Modified"));

    String output = outputStream.toString();
    assertTrue(output.contains("Edited single event"));
  }

  @Test
  public void testDeleteCommands() {
    enhancedController.processCommand("create event \"ToDelete\" from 2025-01-15T10:00 " +
            "to 2025-01-15T11:00");
    outputStream.reset();

    assertTrue(enhancedController.processCommand("delete event \"ToDelete\" " +
            "from 2025-01-15T10:00"));

    String output = outputStream.toString();
    assertTrue(output.contains("Deleted single event"));
  }

  @Test
  public void testInvalidCommands() {
    assertTrue(enhancedController.processCommand("invalid command"));
    assertTrue(enhancedController.processCommand(""));
    assertTrue(enhancedController.processCommand("create event invalid format"));

    String output = outputStream.toString();
    assertTrue(output.contains("Invalid command format") || output.contains("Error"));
  }

  @Test
  public void testExitCommand() {
    assertFalse(enhancedController.processCommand("exit"));
    assertFalse(enhancedController.processCommand("EXIT"));
    assertFalse(enhancedController.processCommand(null));
  }

  @Test
  public void testCaseInsensitiveCommands() {
    assertTrue(enhancedController.processCommand("CREATE CALENDAR --name Test --timezone UTC"));
    assertTrue(enhancedController.processCommand("USE CALENDAR --name Test"));
    assertTrue(enhancedController.processCommand("CREATE EVENT \"Test\" FROM 2025-01-15T10:00 " +
            "TO 2025-01-15T11:00"));

    String output = outputStream.toString();
    assertTrue(output.contains("Success"));
  }

  @Test
  public void testErrorHandling() {
    // Test invalid timezone
    assertTrue(enhancedController.processCommand("create calendar --name Test --timezone " +
            "Invalid/Zone"));

    // Test non-existent calendar
    assertTrue(enhancedController.processCommand("use calendar --name NonExistent"));

    // Test copying non-existent event
    enhancedController.processCommand("create calendar --name Target --timezone UTC");
    assertTrue(enhancedController.processCommand("copy event NonExistent on 2025-01-15T10:00" +
            " --target Target to 2025-01-15T10:00"));

    String output = outputStream.toString();
    assertTrue(output.contains("Error"));
  }

  @Test
  public void testComplexWorkflow() {
    assertTrue(enhancedController.processCommand("create calendar --name Work " +
            "--timezone America/New_York"));
    assertTrue(enhancedController.processCommand("create calendar --name Personal " +
            "--timezone America/Los_Angeles"));

    // Add events to work calendar
    assertTrue(enhancedController.processCommand("use calendar --name Work"));
    assertTrue(enhancedController.processCommand("create event \"Meeting\" " +
            "from 2025-01-15T14:00 to 2025-01-15T15:00"));
    assertTrue(enhancedController.processCommand("create event \"Standup\" " +
            "from 2025-01-16T09:00 to 2025-01-16T09:30 repeats MTWRF for 3 times"));

    // Copy events to personal calendar
    assertTrue(enhancedController.processCommand("copy events on 2025-01-15 --target Personal " +
            "to 2025-01-15"));
    assertTrue(enhancedController.processCommand("copy events between 2025-01-16 and 2025-01-18 " +
            "--target Personal to 2025-01-16"));

    assertTrue(enhancedController.processCommand("use calendar --name Personal"));
    assertTrue(enhancedController.processCommand("print events on 2025-01-15"));
    assertTrue(enhancedController.processCommand("print events from 2025-01-16T00:00 " +
            "to 2025-01-18T23:59"));
    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Standup"));
  }
}
