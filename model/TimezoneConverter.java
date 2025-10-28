package model;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * A class that helps a calendar convert a time from one time zone to another.
 */
public class TimezoneConverter {
  private final DateTimeFormatter DATETIME_FORMAT;

  /**
   * A constructor for a Timezone converter that sets the default format of a given time
   * to the pattern: "yyyy-MM-dd'T'HH:mm".
   */
  public TimezoneConverter() {
    DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  }

  /**
   * This method converts a given time from a time zone to a new given time zone.
   *
   * @param dateTime the given time
   * @param fromZone the zone to convert from
   * @param toZone   the zone to convert to
   * @return A new LocalDateTime to the new timezone
   */
  public LocalDateTime convertTime(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
    if (fromZone.equals(toZone)) {
      return dateTime;
    }

    try {
      ZonedDateTime zonedDateTime = dateTime.atZone(fromZone);
      return zonedDateTime.withZoneSameInstant(toZone).toLocalDateTime();
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Error converting time between timezones: "
              + e.getMessage());
    }
  }

  /**
   * Returns a new event with new start and end times from the given time zone to
   * the new time zone.
   *
   * @param event    the Event that is to be changed
   * @param fromZone the old time zone of the event
   * @param toZone   the new time zone for the event
   * @return the given event with its times converted
   */
  public Event convertEventTimezone(Event event, ZoneId fromZone, ZoneId toZone) {
    LocalDateTime convertedStart = convertTime(event.getStartDateTime(), fromZone, toZone);
    LocalDateTime convertedEnd = convertTime(event.getEndDateTime(), fromZone, toZone);

    return new Event.Builder()
            .subject(event.getSubject())
            .description(event.getDescription())
            .startDateTime(convertedStart)
            .endDateTime(convertedEnd)
            .location(event.getLocation())
            .status(event.getStatus())
            .seriesId(event.getSeriesId())
            .occurrenceIndex(event.getOccurrenceIndex())
            .build();
  }

  /**
   * Returns the current local date and time in the specified time zone.
   *
   * @param zone the target time zone
   * @return the current LocalDateTime in the given zone
   */
  public LocalDateTime getCurrentTimeInZone(ZoneId zone) {
    return ZonedDateTime.now(zone).toLocalDateTime();
  }

  /**
   * Returns true if two given times at their given zones are equal.
   *
   * @param time1 the first time to be compared
   * @param zone1 the timezone of the first time given
   * @param time2 the second time to be compared
   * @param zone2 the timezone of the second event given
   * @return a boolean if two times in their timezones are equal
   */
  public boolean areTimesEquivalent(LocalDateTime time1, ZoneId zone1,
                                    LocalDateTime time2, ZoneId zone2) {
    ZonedDateTime zoned1 = time1.atZone(zone1);
    ZonedDateTime zoned2 = time2.atZone(zone2);
    return zoned1.isEqual(zoned2);
  }

  /**
   * Retrieves all available time zone IDs recognized by the system.
   *
   * @return a Set of string identifiers for all available time zones
   */
  public Set<String> getAvailableTimezones() {
    return ZoneId.getAvailableZoneIds();
  }

  /**
   * Returns true if the String of the given timezone is a real timezone. Otherwise,
   * throws an exception
   *
   * @param timezone to be verified
   * @return a boolean whether or not a time zone is real
   */
  public boolean isValidTimezone(String timezone) {
    try {
      ZoneId.of(timezone);
      return true;
    } catch (DateTimeException e) {
      return false;
    }
  }

  /**
   * Formats the given LocalDateTime with its zone information.
   *
   * @param dateTime the date and time to format
   * @param zone     the time zone to apply
   * @return a string in the pattern "yyyy-MM-dd'T'HH:mm z"
   */
  public String formatWithTimezone(LocalDateTime dateTime, ZoneId zone) {
    ZonedDateTime zonedDateTime = dateTime.atZone(zone);
    return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm z"));
  }

  /**
   * Takes a dateTime string and converts it from one timezone to another.
   *
   * @param dateTimeString the dateTime to be converted
   * @param fromZone       the timezone the string is being converted from
   * @param toZone         the timezone the string is being converted to
   * @return the Converted string
   */
  public String convertTimeString(String dateTimeString, ZoneId fromZone, ZoneId toZone) {
    try {
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATETIME_FORMAT);
      LocalDateTime converted = convertTime(dateTime, fromZone, toZone);
      return converted.format(DATETIME_FORMAT);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeString);
    }
  }
}