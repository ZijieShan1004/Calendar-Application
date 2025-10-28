import org.junit.Test;

import controller.CalendarGUIController;
import model.CalendarModel;
import model.CalendarModelImpl;
import model.CalendarModelImplV2;
import model.Event;
import model.Status;
import view.CalendarGUIViewInterface;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Simple test class for GUI controller functionality.
 */
public class CalendarGUITest {

  @Test
  public void testBasicModelOperations() {
    CalendarModel model = new CalendarModelImpl();
    CalendarGUIController controller = new CalendarGUIController(model);
    TestView view = new TestView();
    controller.setView(view);

    assertFalse(controller.hasEnhancedFeatures());

    // Test event creation
    assertTrue(controller.createSingleEvent("Meeting", "2024-12-15", "10:00",
            "2024-12-15", "11:00", "Room A", Status.PUBLIC));

    List<Event> events = controller.getEventsOnDate("2024-12-15");
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());

    // Enhanced features should return false
    assertFalse(controller.createCalendar("Test", "UTC"));
    assertNull(controller.getCurrentCalendarName());
  }

  @Test
  public void testEnhancedModelOperations() {
    CalendarModelImplV2 model = new CalendarModelImplV2();
    CalendarGUIController controller = new CalendarGUIController(model);
    TestView view = new TestView();
    controller.setView(view);

    assertTrue(controller.hasEnhancedFeatures());

    // Test basic operations still work
    assertTrue(controller.createSingleEvent("Meeting", "2024-12-15", "10:00",
            "2024-12-15", "11:00", "Room A", Status.PUBLIC));

    List<Event> events = controller.getEventsOnDate("2024-12-15");
    assertEquals(1, events.size());

    // Test enhanced operations
    assertNotNull(controller.getCurrentCalendarName());

    // Test event editing
    assertTrue(controller.editEvent("Meeting", "2024-12-15T10:00", "subject", "Updated Meeting"));

    events = controller.getEventsOnDate("2024-12-15");
    assertEquals("Updated Meeting", events.get(0).getSubject());

    // Test event deletion
    assertTrue(controller.deleteEvent("Updated Meeting", "2024-12-15T10:00"));

    events = controller.getEventsOnDate("2024-12-15");
    assertEquals(0, events.size());
  }

  @Test
  public void testAllDayEvents() {
    CalendarModel model = new CalendarModelImpl();
    CalendarGUIController controller = new CalendarGUIController(model);
    TestView view = new TestView();
    controller.setView(view);

    assertTrue(controller.createAllDayEvent("Holiday", "2024-12-25", "Office", Status.PUBLIC));

    List<Event> events = controller.getEventsOnDate("2024-12-25");
    assertEquals(1, events.size());
    assertEquals("Holiday", events.get(0).getSubject());
  }

  @Test
  public void testErrorHandling() {
    CalendarModel model = new CalendarModelImpl();
    CalendarGUIController controller = new CalendarGUIController(model);
    TestView view = new TestView();
    controller.setView(view);

    // Invalid event (end before start)
    assertFalse(controller.createSingleEvent("Invalid", "2024-12-15", "15:00",
            "2024-12-15", "10:00", "", Status.PUBLIC));
    assertTrue(view.errorShown);
  }

  /**
   * Simple test implementation of the view interface.
   */
  private static class TestView implements CalendarGUIViewInterface {
    public boolean errorShown = false;
    public boolean successShown = false;
    public String lastMessage = "";

    @Override
    public void displayEvents(List<Event> events) {
      // Test implementation
    }

    @Override
    public void showSuccess(String message) {
      successShown = true;
      lastMessage = message;
    }

    @Override
    public void showError(String message) {
      errorShown = true;
      lastMessage = message;
    }

    @Override
    public void setVisible(boolean visible) {
      // Test implementation
    }
  }
}
