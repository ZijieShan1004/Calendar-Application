package view;

import java.util.List;

import model.Event;

/**
 * Defines methods for displaying calendar information to the user.
 */
public interface CalendarViewInterface {

  /**
   * Prints all events scheduled on a specific date.
   *
   * @param date   the date to print events for
   * @param events list of events on that date
   */
  void printEventsOnDate(String date, List<Event> events);

  /**
   * Prints all events within a specified time range.
   *
   * @param startTime the start of the time range
   * @param endTime   the end of the time range
   * @param events    list of events in that range
   */
  void printEventsInRange(String startTime, String endTime, List<Event> events);

  /**
   * Prints whether the user is busy or available at a specific time.
   *
   * @param dateTime the time to check
   * @param isBusy   true if busy, false if available
   */
  void printStatus(String dateTime, boolean isBusy);

  /**
   * Prints a success message.
   *
   * @param message the message to print
   */
  void printSuccess(String message);

  /**
   * Prints an error message.
   *
   * @param message the error message to print
   */
  void printError(String message);
}