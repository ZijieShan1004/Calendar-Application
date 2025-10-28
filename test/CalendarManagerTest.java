import model.CalendarManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;

/**
 * A test class for all the methods in a CalendarManager.
 */
public class CalendarManagerTest {
  private CalendarManager manager;

  @Before
  public void setUp() {
    manager = new CalendarManager();
  }

  @Test
  public void testCreateCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    assertTrue(manager.hasCalendar("Work"));
    assertEquals(1, manager.getCalendarCount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateCalendar() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.createCalendar("Work", ZoneId.of("UTC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarEmptyName() {
    manager.createCalendar("", ZoneId.of("UTC"));
  }

  @Test
  public void testUseCalendar() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.useCalendar("Work");
    assertEquals("Work", manager.getActiveCalendarName());
    assertNotNull(manager.getActiveCalendar());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseNonExistentCalendar() {
    manager.useCalendar("NonExistent");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetActiveCalendarWhenNone() {
    manager.getActiveCalendar();
  }

  @Test
  public void testEditCalendarName() {
    manager.createCalendar("OldName", ZoneId.of("UTC"));
    manager.editCalendarProperty("OldName", "name", "NewName");
    assertTrue(manager.hasCalendar("NewName"));
    assertFalse(manager.hasCalendar("OldName"));
  }

  @Test
  public void testEditCalendarTimezone() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.useCalendar("Work");
    manager.editCalendarProperty("Work", "timezone", "America/New_York");
    assertEquals(ZoneId.of("America/New_York"), manager.getActiveCalendar().getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidProperty() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.editCalendarProperty("Work", "invalid", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezone() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.editCalendarProperty("Work", "timezone", "Invalid/Timezone");
  }

  @Test
  public void testGetCalendarNames() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    manager.createCalendar("Personal", ZoneId.of("America/New_York"));
    assertEquals(2, manager.getAllCalendarNames().size());
    assertTrue(manager.getAllCalendarNames().contains("Work"));
    assertTrue(manager.getAllCalendarNames().contains("Personal"));
  }

  @Test
  public void testGetCalendar() {
    manager.createCalendar("Work", ZoneId.of("UTC"));
    assertNotNull(manager.getCalendar("Work"));
    assertEquals("Work", manager.getCalendar("Work").getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonExistentCalendar() {
    manager.getCalendar("NonExistent");
  }
}