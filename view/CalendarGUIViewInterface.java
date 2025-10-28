package view;

import model.Event;
import java.util.List;

/**
 * Simple interface for the calendar GUI view.
 */
public interface CalendarGUIViewInterface {

  /**
   * Display events in the schedule view.
   */
  void displayEvents(List<Event> events);

  /**
   * Show a success message to the user.
   */
  void showSuccess(String message);

  /**
   * Show an error message to the user.
   */
  void showError(String message);

  /**
   * Set GUI visibility.
   */
  void setVisible(boolean visible);
}
