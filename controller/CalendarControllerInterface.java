package controller;

/**
 * Defines methods to process user commands for the calendar application.
 */
public interface CalendarControllerInterface {
  /**
   * Process a single user command.
   *
   * @param command raw command text
   * @return false if command is "exit", true otherwise
   */
  boolean processCommand(String command);
}
