package controller;

import model.CalendarModel;
import model.Event;
import model.Status;
import view.CalendarView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller layer that parses user commands and delegates to the model and view.
 */
public class CalendarController implements CalendarControllerInterface {
  private final CalendarModel model;
  private final CalendarView view;

  /**
   * Initializes a controllers ability to delegate inputs to the model and view.
   *
   * @param model calendar model
   * @param view  calendar view
   */
  public CalendarController(CalendarModel model, CalendarView view) {
    this.model = model;
    this.view = view;
  }

  @Override
  public boolean processCommand(String command) {
    if (command == null) {
      return false;
    }

    String cleanCommand = command.trim();
    if (cleanCommand.isEmpty()) {
      return true;
    }
    if (cleanCommand.equalsIgnoreCase("exit")) {
      return false;
    }

    try {
      if (handleCreateCommands(cleanCommand)) {
        return true;
      }
      if (handleEditCommands(cleanCommand)) {
        return true;
      }
      if (handleDeleteCommands(cleanCommand)) {
        return true;
      }
      if (handleQueryCommands(cleanCommand)) {
        return true;
      }
      if (handleStatusCommand(cleanCommand)) {
        return true;
      }

      view.printError("Invalid command format");
    } catch (IllegalArgumentException e) {
      view.printError(e.getMessage());
    } catch (Exception e) {
      view.printError("Something went wrong: " + e.getMessage());
    }

    return true;
  }

  private boolean handleCreateCommands(String command) {
    if (tryCreateSingleEvent(command)) {
      return true;
    }
    if (tryCreateRepeatingEvent(command)) {
      return true;
    }
    if (tryCreateAllDayEvent(command)) {
      return true;
    }
    tryCreateRepeatingAllDayEvent(command);
    return false;
  }

  private boolean tryCreateSingleEvent(String command) {
    Pattern pattern = Pattern.compile(
            "create event \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String subject = match.group(1);
    String[] startParts = match.group(2).split("T");
    String[] endParts = match.group(3).split("T");

    int eventId = model.createSingleEvent(subject, "", startParts[0], startParts[1],
            endParts[0], endParts[1], "", Status.PUBLIC);

    view.printSuccess("Created single event '" + subject
            + "' (id=" + eventId + ")");
    return true;
  }

  private boolean tryCreateRepeatingEvent(String command) {
    Pattern forTimesPattern = Pattern.compile(
            "create event \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) for (\\d+) " +
                    "times",
            Pattern.CASE_INSENSITIVE);

    Pattern untilDatePattern = Pattern.compile(
            "create event \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) " +
                    "until (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher forTimesMatch = forTimesPattern.matcher(command);
    if (forTimesMatch.matches()) {
      String subject = forTimesMatch.group(1);
      String[] startParts = forTimesMatch.group(2).split("T");
      String[] endParts = forTimesMatch.group(3).split("T");
      String weekdays = forTimesMatch.group(4);
      int times = Integer.parseInt(forTimesMatch.group(5));

      int seriesId = model.createTimedEventSeries(subject, "",
              startParts[0], startParts[1], endParts[0], endParts[1], weekdays, times,
              null, "", Status.PUBLIC);

      view.printSuccess("Created timed event series '" + subject +
              "' (seriesId=" + seriesId + ")");
      return true;
    }

    Matcher untilDateMatch = untilDatePattern.matcher(command);
    if (untilDateMatch.matches()) {
      String subject = untilDateMatch.group(1);
      String[] startParts = untilDateMatch.group(2).split("T");
      String[] endParts = untilDateMatch.group(3).split("T");
      String weekdays = untilDateMatch.group(4);
      String untilDate = untilDateMatch.group(5);

      int seriesId = model.createTimedEventSeries(subject, "", startParts[0], startParts[1],
              endParts[0], endParts[1], weekdays, 0,
              untilDate, "", Status.PUBLIC);

      view.printSuccess("Created timed event series '"
              + subject + "' (seriesId=" + seriesId + ")");
      return true;
    }

    return false;
  }

  private boolean tryCreateAllDayEvent(String command) {
    Pattern pattern = Pattern.compile(
            "create event \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String subject = match.group(1);
    String date = match.group(2);

    int eventId = model.createAllDayEvent(subject, "", date, "",
            Status.PUBLIC);
    view.printSuccess("Created all-day event '"
            + subject + "' (id=" + eventId + ")");
    return true;
  }

  private boolean tryCreateRepeatingAllDayEvent(String command) {
    Pattern forTimesPattern = Pattern.compile(
            "create event \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) " +
                    "for (\\d+) times",
            Pattern.CASE_INSENSITIVE);

    Pattern untilDatePattern = Pattern.compile(
            "create event \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) " +
                    "until (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher forTimesMatch = forTimesPattern.matcher(command);
    if (forTimesMatch.matches()) {
      String subject = forTimesMatch.group(1);
      String date = forTimesMatch.group(2);
      String weekdays = forTimesMatch.group(3);
      int times = Integer.parseInt(forTimesMatch.group(4));

      int seriesId = model.createAllDayEventSeries(subject, "", date, weekdays, times,
              null, "", Status.PUBLIC);

      view.printSuccess("Created all-day event series '"
              + subject + "' (seriesId=" + seriesId + ")");
      return true;
    }

    Matcher untilDateMatch = untilDatePattern.matcher(command);
    if (untilDateMatch.matches()) {
      String subject = untilDateMatch.group(1);
      String date = untilDateMatch.group(2);
      String weekdays = untilDateMatch.group(3);
      String untilDate = untilDateMatch.group(4);

      int seriesId = model.createAllDayEventSeries(subject, "", date, weekdays, 0,
              untilDate, "", Status.PUBLIC);

      view.printSuccess("Created all-day event series '"
              + subject + "' (seriesId=" + seriesId + ")");
      return true;
    }

    return false;
  }

  private boolean handleEditCommands(String command) {
    String[] editTypes = {"edit event", "edit events", "edit series"};

    for (String editType : editTypes) {
      if (tryEditCommand(command, editType)) {
        return true;
      }
    }

    return false;
  }

  private boolean tryEditCommand(String command, String editType) {
    String keyword = editType.equals("edit event") ? "to" : "with";
    Pattern pattern = Pattern.compile(
            editType + " (subject|start|end|description|location|status) \"([^\"]+)\" " +
                    "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " + keyword + " (.+)",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String property = match.group(1);
    String subject = match.group(2);
    String startTime = match.group(3);
    String newValue = match.group(4).trim();

    if (editType.equals("edit event")) {
      model.editSingleEvent(subject, startTime, property, newValue);
      view.printSuccess("Edited single event '" + subject + "'");
    } else if (editType.equals("edit events")) {
      int count = model.editEventsFrom(subject, startTime, property, newValue);
      view.printSuccess("Edited " + count + " events from '"
              + subject + "'");
    } else {
      int count = model.editEntireSeries(subject, startTime, property, newValue);
      view.printSuccess("Edited entire series (" + count + " events) of '"
              + subject + "'");
    }

    return true;
  }

  private boolean handleDeleteCommands(String command) {
    String[] deleteTypes = {"delete event", "delete events", "delete series"};

    for (String deleteType : deleteTypes) {
      if (tryDeleteCommand(command, deleteType)) {
        return true;
      }
    }

    return false;
  }

  private boolean tryDeleteCommand(String command, String deleteType) {
    Pattern pattern = Pattern.compile(
            deleteType + " \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String subject = match.group(1);
    String startTime = match.group(2);

    if (deleteType.equals("delete event")) {
      model.deleteSingleEvent(subject, startTime);
      view.printSuccess("Deleted single event '" + subject + "'");
    } else if (deleteType.equals("delete events")) {
      int count = model.deleteEventsFrom(subject, startTime);
      view.printSuccess("Deleted " + count + " events from '" + subject + "'");
    } else {
      int count = model.deleteEntireSeries(subject, startTime);
      view.printSuccess("Deleted entire series (" + count + " events) of '" + subject + "'");
    }

    return true;
  }

  private boolean handleQueryCommands(String command) {
    if (tryPrintEventsOnDate(command)) {
      return true;
    }
    tryPrintEventsInRange(command);
    return false;
  }

  private boolean tryPrintEventsOnDate(String command) {
    Pattern pattern = Pattern.compile(
            "print events on (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String date = match.group(1);
    List<Event> events = model.getEventsOnDate(date);
    view.printEventsOnDate(date, events);
    return true;
  }

  private boolean tryPrintEventsInRange(String command) {
    Pattern pattern = Pattern.compile(
            "print events from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to " +
                    "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String startTime = match.group(1);
    String endTime = match.group(2);
    List<Event> events = model.getEventsInRange(startTime, endTime);
    view.printEventsInRange(startTime, endTime, events);
    return true;
  }

  private boolean handleStatusCommand(String command) {
    Pattern pattern = Pattern.compile(
            "show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String dateTime = match.group(1);
    boolean isBusy = model.isBusy(dateTime);
    view.printStatus(dateTime, isBusy);
    return true;
  }
}