package view;

import model.Event;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A class that implements the view aspect of a Calendar.
 */
public class CalendarView implements CalendarViewInterface {
  private final DateTimeFormatter TIME_FORMAT;

  public CalendarView() {
    this.TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  }

  @Override
  public void printEventsOnDate(String date, List<Event> events) {
    System.out.println("Events on " + date + ":");

    if (events.isEmpty()) {
      System.out.println("  (no events)");
      return;
    }

    for (Event event : events) {
      printSingleEvent(event);
    }
  }

  @Override
  public void printEventsInRange(String startTime, String endTime, List<Event> events) {
    System.out.println("Events from " + startTime + " to " + endTime + ":");

    if (events.isEmpty()) {
      System.out.println("  (no events)");
      return;
    }

    for (Event event : events) {
      printSingleEvent(event);
    }
  }

  private void printSingleEvent(Event event) {
    String startTime = event.getStartDateTime().format(TIME_FORMAT);
    String endTime = event.getEndDateTime().format(TIME_FORMAT);
    String location = event.getLocation().isEmpty() ? "N/A" : event.getLocation();

    System.out.println(String.format("  - [%s~%s] %s (loc: %s, status: %s)",
            startTime, endTime, event.getSubject(), location, event.getStatus().name()));
  }

  @Override
  public void printStatus(String dateTime, boolean isBusy) {
    String status = isBusy ? "Busy" : "Available";
    System.out.println("Status at " + dateTime + ": " + status);
  }

  @Override
  public void printSuccess(String message) {
    System.out.println("Success: " + message);
  }

  @Override
  public void printError(String message) {
    System.out.println("Error: " + message);
  }
}