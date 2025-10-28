import model.TimezoneConverter;
import model.Event;
import model.Status;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test class for all the methods of the TimezoneConverter class.
 */
public class TimezoneConverterTest {
  private TimezoneConverter converter;

  @Before
  public void setUp() {
    converter = new TimezoneConverter();
  }

  @Test
  public void testConvertTimeSameZone() {
    LocalDateTime time = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime result = converter.convertTime(time, ZoneId.of("UTC"), ZoneId.of("UTC"));
    assertEquals(time, result);
  }

  @Test
  public void testConvertTimeESTtoPST() {
    LocalDateTime estTime = LocalDateTime.of(2025, 1, 15, 14, 0); // 2 PM EST
    LocalDateTime pstTime = converter.convertTime(estTime,
            ZoneId.of("America/New_York"), ZoneId.of("America/Los_Angeles"));
    assertEquals(11, pstTime.getHour()); // Should be 11 AM PST
  }

  @Test
  public void testConvertTimePSTtoEST() {
    LocalDateTime pstTime = LocalDateTime.of(2025, 1, 15, 11, 0); // 11 AM PST
    LocalDateTime estTime = converter.convertTime(pstTime,
            ZoneId.of("America/Los_Angeles"), ZoneId.of("America/New_York"));
    assertEquals(14, estTime.getHour()); // Should be 2 PM EST
  }

  @Test
  public void testConvertEventTimezone() {
    Event originalEvent = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 15, 14, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 15, 15, 0))
            .location("Office")
            .status(Status.PUBLIC)
            .build();

    Event convertedEvent = converter.convertEventTimezone(originalEvent,
            ZoneId.of("America/New_York"), ZoneId.of("America/Los_Angeles"));

    assertEquals("Test Event", convertedEvent.getSubject());
    assertEquals("Office", convertedEvent.getLocation());
    assertEquals(Status.PUBLIC, convertedEvent.getStatus());
    assertEquals(11, convertedEvent.getStartDateTime().getHour()); // 2 PM EST -> 11 AM PST
    assertEquals(12, convertedEvent.getEndDateTime().getHour());   // 3 PM EST -> 12 PM PST
  }

  @Test
  public void testAreTimesEquivalent() {
    LocalDateTime estTime = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime pstTime = LocalDateTime.of(2025, 1, 15, 11, 0);

    assertTrue(converter.areTimesEquivalent(estTime, ZoneId.of("America/New_York"),
            pstTime, ZoneId.of("America/Los_Angeles")));
  }

  @Test
  public void testAreTimesNotEquivalent() {
    LocalDateTime time1 = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime time2 = LocalDateTime.of(2025, 1, 15, 12, 0);

    assertFalse(converter.areTimesEquivalent(time1, ZoneId.of("UTC"),
            time2, ZoneId.of("UTC")));
  }

  @Test
  public void testIsValidTimezone() {
    assertTrue(converter.isValidTimezone("America/New_York"));
    assertTrue(converter.isValidTimezone("UTC"));
    assertTrue(converter.isValidTimezone("Europe/London"));
    assertFalse(converter.isValidTimezone("Invalid/Timezone"));
    assertFalse(converter.isValidTimezone(""));
  }

  @Test
  public void testGetAvailableTimezones() {
    assertNotNull(converter.getAvailableTimezones());
    assertFalse(converter.getAvailableTimezones().isEmpty());
    assertTrue(converter.getAvailableTimezones().contains("UTC"));
    assertTrue(converter.getAvailableTimezones().contains("America/New_York"));
  }

  @Test
  public void testFormatWithTimezone() {
    LocalDateTime time = LocalDateTime.of(2025, 1, 15, 14, 0);
    String formatted = converter.formatWithTimezone(time, ZoneId.of("America/New_York"));
    assertTrue(formatted.contains("2025-01-15T14:00"));
  }

  @Test
  public void testConvertTimeString() {
    String result = converter.convertTimeString("2025-01-15T14:00",
            ZoneId.of("America/New_York"), ZoneId.of("America/Los_Angeles"));
    assertEquals("2025-01-15T11:00", result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTimeStringInvalidFormat() {
    converter.convertTimeString("invalid-format", ZoneId.of("UTC"), ZoneId.of("UTC"));
  }

  @Test
  public void testGetCurrentTimeInZone() {
    LocalDateTime utcTime = converter.getCurrentTimeInZone(ZoneId.of("UTC"));
    LocalDateTime estTime = converter.getCurrentTimeInZone(ZoneId.of("America/New_York"));
    assertNotNull(utcTime);
    assertNotNull(estTime);
  }
}
