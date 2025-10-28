package model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class that contains and manages multiple of a user's calendars that they can
 * call upon. While there will always be an active calendar.
 */
public class CalendarManager {
  private final Map<String, Calendar> calendars;
  private Calendar activeCalendar;

  /**
   * A constructor for the calendar manager that sets its variables to a default empty.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
    this.activeCalendar = null;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   * The calendar name must be unique and non-empty.
   *
   * @param name     the unique name for the new calendar
   * @param timezone the timezone for the calendar using ZoneId
   * @throws IllegalArgumentException if the name is null, empty, or already exists
   */
  public void createCalendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }
    calendars.put(name, new Calendar(name, timezone));
  }

  /**
   * Sets the specified calendar as the active calendar for subsequent operations.
   * All calendar operations will be performed on the active calendar until
   * a different calendar is selected.
   *
   * @param name the name of the calendar to make active
   * @throws IllegalArgumentException if no calendar with the specified name exists
   */
  public void useCalendar(String name) {
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }
    this.activeCalendar = calendar;
  }

  /**
   * Retrieves the currently active calendar.
   * There must be an active calendar set before calling this method.
   *
   * @return the currently active Calendar object
   * @throws IllegalStateException if no calendar is currently active
   */
  public Calendar getActiveCalendar() {
    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar. Use 'use calendar' command first.");
    }
    return activeCalendar;
  }

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar to retrieve
   * @return the Calendar object with the specified name
   * @throws IllegalArgumentException if no calendar with the specified name exists
   */
  public Calendar getCalendar(String name) {
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }
    return calendar;
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of the active calendar, or null if no calendar is active
   */
  public String getActiveCalendarName() {
    return activeCalendar != null ? activeCalendar.getName() : null;
  }

  /**
   * Checks if a calendar with the specified name exists.
   *
   * @param name the name of the calendar to check for
   * @return true if a calendar with the specified name exists, false otherwise
   */
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }

  /**
   * Retrieves the names of all calendars managed by this CalendarManager.
   *
   * @return a Set containing the names of all calendars
   */
  public Set<String> getAllCalendarNames() {
    return calendars.keySet();
  }

  /**
   * Edits a property of an existing calendar.
   * Supported properties are "name" and "timezone".
   * When changing the name, the new name must be unique and non-empty.
   * When changing the timezone, the value must be a valid timezone identifier.
   *
   * @param name     the name of the calendar to edit
   * @param property the property to change ("name" or "timezone")
   * @param newValue the new value for the specified property
   * @throws IllegalArgumentException if the calendar doesn't exist, property is invalid,
   *                                  new name is empty/duplicate, or timezone is invalid
   */
  public void editCalendarProperty(String name, String property, String newValue) {
    Calendar calendar = getCalendar(name);

    switch (property.toLowerCase()) {
      case "name":
        if (newValue == null || newValue.trim().isEmpty()) {
          throw new IllegalArgumentException("Calendar name cannot be empty");
        }
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException("Calendar with name '" + newValue +
                  "' already exists");
        }
        calendars.remove(name);
        calendar.setName(newValue);
        calendars.put(newValue, calendar);
        break;

      case "timezone":
        try {
          ZoneId newZoneId = ZoneId.of(newValue);
          calendar.setTimezone(newZoneId);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue);
        }
        break;

      default:
        throw new IllegalArgumentException("Invalid property: " + property + ". " +
                "Must be 'name' or 'timezone'");
    }
  }

  /**
   * Gets the total number of calendars managed by this CalendarManager.
   *
   * @return the number of calendars currently managed
   */
  public int getCalendarCount() {
    return calendars.size();
  }
}