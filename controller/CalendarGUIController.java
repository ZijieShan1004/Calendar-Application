package controller;

import model.CalendarModel;
import model.CalendarModelImplV2;
import model.Event;
import model.Status;
import view.CalendarGUIViewInterface;

import java.util.List;

/**
 * Simple controller that works with both basic and enhanced calendar models.
 */
public class CalendarGUIController {
  private final CalendarModel model;
  private final CalendarModelImplV2 enhancedModel; // null if basic model
  private CalendarGUIViewInterface view;

  /**
   * Handles user interactions in the GUI layer of the Calendar application.
   * Provides methods to create and manage events and calendars.
   */
  public CalendarGUIController(CalendarModel model) {
    this.model = model;
    this.enhancedModel = (model instanceof CalendarModelImplV2)
            ? (CalendarModelImplV2) model : null;
  }

  public void setView(CalendarGUIViewInterface view) {
    this.view = view;
  }

  public boolean hasEnhancedFeatures() {
    return enhancedModel != null;
  }

  /**
   * Create a new timed event in the current calendar.
   *
   * @param subject   event title
   * @param startDate start date in ISO format yyyy-MM-dd
   * @param startTime start time in HH:mm format
   * @param endDate   end date in ISO format yyyy-MM-dd
   * @param endTime   end time in HH:mm format
   * @param location  event location
   * @param status    event status
   * @return true if creation succeeds false otherwise
   */
  public boolean createSingleEvent(String subject, String startDate, String startTime,
                                   String endDate, String endTime, String location, Status status) {
    try {
      model.createSingleEvent(subject, "", startDate,
              startTime, endDate, endTime, location, status);
      return true;
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }

  /**
   * Create a new all‑day event on a given date.
   *
   * @param subject  event title
   * @param date     date in ISO format yyyy-MM-dd
   * @param location event location
   * @param status   event status
   * @return true if creation succeeds false otherwise
   */
  public boolean createAllDayEvent(String subject, String date, String location, Status status) {
    try {
      model.createAllDayEvent(subject, "", date, location, status);
      return true;
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }

  /**
   * Fetch events on a specific date from the model.
   *
   * @param date date in ISO format yyyy-MM-dd
   * @return list of events on that date or empty list on error
   */
  public List<Event> getEventsOnDate(String date) {
    try {
      return model.getEventsOnDate(date);
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return List.of();
    }
  }

  /**
   * Create a new named calendar with optional timezone.
   *
   * @param name     calendar name
   * @param timezone timezone ID such as "America/New_York"
   * @return true if calendar created false otherwise
   */
  public boolean createCalendar(String name, String timezone) {
    if (enhancedModel == null) {
      return false;
    }
    try {
      enhancedModel.createCalendar(name, timezone);
      return true;
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }

  /**
   * Switch the active calendar by name.
   *
   * @param name name of an existing calendar
   * @return true if switch succeeds false otherwise
   */
  public boolean switchCalendar(String name) {
    if (enhancedModel == null) {
      return false;
    }
    try {
      enhancedModel.useCalendar(name);
      return true;
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }

  /**
   * Getter of current calendar name.
   * @return current calendar name
   */
  public String getCurrentCalendarName() {
    if (enhancedModel == null) {
      return null;
    }
    return enhancedModel.getCurrentCalendarName();
  }

  /**
   * Edit a single event property.
   *
   * @param subject       original event title
   * @param startDateTime original start date and time in ISO format
   * @param property      field to edit (subject startTime endTime location status)
   * @param newValue      new value for the field
   * @return true if edit succeeds false otherwise
   */
  public boolean editEvent(String subject, String startDateTime, String property, String newValue) {
    if (enhancedModel == null) {
      return false;
    }
    try {
      return enhancedModel.editSingleEvent(subject, startDateTime, property, newValue);
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }

  /**
   * Delete a single event identified by title and start timestamp.
   *
   * @param subject       event title
   * @param startDateTime start date and time in ISO format
   * @return true if deletion succeeds false otherwise
   */
  public boolean deleteEvent(String subject, String startDateTime) {
    if (enhancedModel == null) {
      return false;
    }
    try {
      return enhancedModel.deleteSingleEvent(subject, startDateTime);
    } catch (Exception e) {
      if (view != null) {
        view.showError(e.getMessage());
      }
      return false;
    }
  }
}
