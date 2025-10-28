import model.Event;
import model.Status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

/**
 * A test class for all Event methods and examples.
 */
public class EventTest {

  @Test
  public void testEventBuilderBasic() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertEquals("Test Event", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 1, 1, 11, 0), event.getEndDateTime());
  }

  @Test
  public void testEventBuilderWithAllProperties() {
    Event event = new Event.Builder()
            .subject("Complete Event")
            .description("This is a test event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .location("Conference Room A")
            .status(Status.PRIVATE)
            .seriesId(123)
            .occurrenceIndex(5)
            .build();

    assertEquals("Complete Event", event.getSubject());
    assertEquals("This is a test event", event.getDescription());
    assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 1, 1, 11, 0), event.getEndDateTime());
    assertEquals("Conference Room A", event.getLocation());
    assertEquals(Status.PRIVATE, event.getStatus());
    assertEquals((Integer) 123, event.getSeriesId());
    assertEquals(5, event.getOccurrenceIndex());
  }

  @Test
  public void testEventBuilderDefaults() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    assertEquals(Status.PUBLIC, event.getStatus());
    assertNull(event.getSeriesId());
    assertEquals(0, event.getOccurrenceIndex());
  }

  @Test(expected = IllegalStateException.class)
  public void testEventBuilderMissingSubject() {
    new Event.Builder()
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();
  }

  @Test(expected = IllegalStateException.class)
  public void testEventBuilderMissingStartDateTime() {
    new Event.Builder()
            .subject("Test Event")
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();
  }


  @Test
  public void testEventGettersSetters() {
    Event event = new Event.Builder()
            .subject("Original")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    event.setSubject("Modified");
    event.setDescription("New description");
    event.setStartDateTime(LocalDateTime.of(2025, 1, 1, 9, 0));
    event.setEndDateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
    event.setLocation("New location");
    event.setStatus(Status.PRIVATE);
    event.setSeriesId(456);
    event.setOccurrenceIndex(3);

    assertEquals("Modified", event.getSubject());
    assertEquals("New description", event.getDescription());
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), event.getEndDateTime());
    assertEquals("New location", event.getLocation());
    assertEquals(Status.PRIVATE, event.getStatus());
    assertEquals((Integer) 456, event.getSeriesId());
    assertEquals(3, event.getOccurrenceIndex());
  }

  @Test
  public void testEventEqualityBasic() {
    Event event1 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    Event event2 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertEquals("Events should be equal", event1, event2);
    assertEquals("Hash codes should be equal", event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testEventEqualityWithSeries() {
    Event event1 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(2)
            .build();

    Event event2 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(2)
            .build();

    assertEquals("Series events should be equal", event1, event2);
    assertEquals("Hash codes should be equal", event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testEventInequalityDifferentSubject() {
    Event event1 = new Event.Builder()
            .subject("Event 1")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    Event event2 = new Event.Builder()
            .subject("Event 2")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertNotEquals("Events with different subjects should not be equal", event1, event2);
  }

  @Test
  public void testEventInequalityDifferentTimes() {
    Event event1 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    Event event2 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 12, 0))
            .build();

    assertNotEquals("Events with different times should not be equal", event1, event2);
  }

  @Test
  public void testEventInequalityDifferentSeries() {
    Event event1 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(1)
            .build();

    Event event2 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(456)
            .occurrenceIndex(1)
            .build();

    assertNotEquals("Events with different series IDs should not be equal", event1, event2);
  }

  @Test
  public void testEventInequalityDifferentOccurrenceIndex() {
    Event event1 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(1)
            .build();

    Event event2 = new Event.Builder()
            .subject("Series Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(2)
            .build();

    assertNotEquals("Events with different occurrence indices should not be equal", event1, event2);
  }

  @Test
  public void testEventEqualityIgnoresOtherProperties() {
    Event event1 = new Event.Builder()
            .subject("Test Event")
            .description("Description 1")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .location("Location 1")
            .status(Status.PUBLIC)
            .build();

    Event event2 = new Event.Builder()
            .subject("Test Event")
            .description("Description 2")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .location("Location 2")
            .status(Status.PRIVATE)
            .build();

    assertEquals("Events should be equal despite different" +
            "description, location, and status", event1, event2);
  }

  @Test
  public void testEventEqualityWithNullSeries() {
    Event event1 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    Event event2 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(null)
            .build();

    assertEquals("Events with null series should be equal", event1, event2);
  }

  @Test
  public void testEventEqualitySeriesVsNonSeries() {
    Event event1 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    Event event2 = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(0)
            .build();

    assertNotEquals("Non-series event should not equal series event", event1, event2);
  }

  @Test
  public void testEventEqualityWithSelf() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertEquals("Event should equal itself", event, event);
  }

  @Test
  public void testEventEqualityWithNull() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertNotEquals("Event should not equal null", event, null);
  }

  @Test
  public void testEventEqualityWithDifferentClass() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

    assertNotEquals("Event should not equal different class", event, "Not an Event");
  }

  @Test
  public void testHashCodeConsistency() {
    Event event = new Event.Builder()
            .subject("Test Event")
            .startDateTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endDateTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .seriesId(123)
            .occurrenceIndex(2)
            .build();

    int hash1 = event.hashCode();
    int hash2 = event.hashCode();
    assertEquals("Hash code should be consistent", hash1, hash2);

    event.setDescription("New description");
    int hash3 = event.hashCode();
    assertEquals("Hash code should remain same when non-equality properties change", hash1, hash3);
  }
}
