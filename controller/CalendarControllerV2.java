package controller;

import model.CalendarModelImplV2;
import model.Event;
import model.Status;
import view.CalendarView;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced controller that works with both original and enhanced models.
 * Uses direct casting for enhanced features.
 */
public class CalendarControllerV2 implements CalendarControllerInterface {
  private final Object model;
  private final CalendarView view;
  private final boolean isNew;

  /**
   * A constructor for the new controller which additionally checks and accepts new
   * commands that can be delegated to the new model.
   *
   * @param model the model of the Calendar
   * @param view  the view of the Calendar
   */
  public CalendarControllerV2(Object model, CalendarView view) {
    this.model = model;
    this.view = view;
    this.isNew = model instanceof CalendarModelImplV2;
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
      if (isNew && handleEnhancedCommands(cleanCommand)) {
        return true;
      }

      if (handleOriginalCommands(cleanCommand)) {
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


  private boolean handleEnhancedCommands(String command) {
    if (!isNew) {
      return false;
    }

    try {
      CalendarModelImplV2 enhancedModel = (CalendarModelImplV2) model;

      if (tryCreateCalendar(command, enhancedModel)) {
        return true;
      }
      if (tryEditCalendar(command, enhancedModel)) {
        return true;
      }
      if (tryUseCalendar(command, enhancedModel)) {
        return true;
      }
      if (tryCopyEvent(command, enhancedModel)) {
        return true;
      }
      if (tryCopyEventsOnDate(command, enhancedModel)) {
        return true;
      }
      if (tryCopyEventsBetween(command, enhancedModel)) {
        return true;
      }
    } catch (ClassCastException e) {
      return false;
    }

    return false;
  }

  private boolean tryCreateCalendar(String command, CalendarModelImplV2 enhancedModel) {
    Pattern pattern = Pattern.compile(
            "create calendar --name ([\\w\\d]+) --timezone ([\\w/_-]+)",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String name = match.group(1).trim();
    String timezone = match.group(2);

    try {
      enhancedModel.createCalendar(name, timezone);
      view.printSuccess("Created calendar '" + name + "' with timezone '" + timezone + "'");
      return true;
    } catch (Exception e) {
      view.printError("Failed to create calendar: " + e.getMessage());
      return false;
    }
  }

  private boolean tryEditCalendar(String command, CalendarModelImplV2 enhancedModel) {
    Pattern pattern = Pattern.compile(
            "edit calendar --name ([\\w\\d]+) --property (name|timezone) ([\\w\\d/_-]+)",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String name = match.group(1).trim();
    String property = match.group(2).toLowerCase();
    String newValue = match.group(3).trim();

    try {
      enhancedModel.editCalendarProperty(name, property, newValue);
      view.printSuccess("Updated " + property + " of calendar '"
              + name + "' to '" + newValue + "'");
      return true;
    } catch (Exception e) {
      view.printError("Failed to edit calendar: " + e.getMessage());
      return false;
    }
  }

  private boolean tryUseCalendar(String command, CalendarModelImplV2 enhancedModel) {
    Pattern pattern = Pattern.compile(
            "use calendar --name ([\\w\\d]+)",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String name = match.group(1).trim();
    try {
      enhancedModel.useCalendar(name);
      view.printSuccess("Now using calendar '" + name + "'");
      return true;
    } catch (IllegalArgumentException e) {
      view.printError(e.getMessage());
      return false;
    }
  }

  private boolean tryCopyEvent(String command, CalendarModelImplV2 enhancedModel) {
    Pattern unquotedPattern = Pattern.compile(
            "copy event ([\\w-]+) on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "--target ([\\w\\d]+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher unquotedMatch = unquotedPattern.matcher(command);
    if (unquotedMatch.matches()) {
      return handleEventCopy(unquotedMatch, enhancedModel);
    }

    Pattern quotedPattern = Pattern.compile(
            "copy event \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "--target ([\\w\\d]+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher quotedMatch = quotedPattern.matcher(command);
    if (quotedMatch.matches()) {
      return handleEventCopy(quotedMatch, enhancedModel);
    }

    return false;
  }

  private boolean handleEventCopy(Matcher match, CalendarModelImplV2 enhancedModel) {
    String eventName = match.group(1).trim();
    String sourceDateTime = match.group(2);
    String targetCalendar = match.group(3).trim();
    String targetDateTime = match.group(4);

    try {
      int eventId = enhancedModel.copyEvent(eventName, sourceDateTime,
              targetCalendar, targetDateTime);
      view.printSuccess("Copied event '" + eventName + "' to calendar '" + targetCalendar
              + "' (id=" + eventId + ")");
      return true;
    } catch (Exception e) {
      view.printError("Failed to copy event: " + e.getMessage());
      return false;
    }
  }

  private boolean tryCopyEventsOnDate(String command, CalendarModelImplV2 enhancedModel) {
    Pattern pattern = Pattern.compile(
            "copy events on (\\d{4}-\\d{2}-\\d{2}) --target ([\\w\\d]+) " +
                    "to (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String sourceDate = match.group(1);
    String targetCalendar = match.group(2).trim();
    String targetDate = match.group(3);

    try {
      List<Integer> eventIds = enhancedModel.copyEventsOnDate(sourceDate,
              targetCalendar, targetDate);
      view.printSuccess("Copied " + eventIds.size() + " events to calendar '"
              + targetCalendar + "'");
      return true;
    } catch (Exception e) {
      view.printError("Failed to copy events: " + e.getMessage());
      return false;
    }
  }

  private boolean tryCopyEventsBetween(String command, CalendarModelImplV2 enhancedModel) {
    Pattern pattern = Pattern.compile(
            "copy events between (\\d{4}-\\d{2}-\\d{2}) and (\\d{4}-\\d{2}-\\d{2}) " +
                    "--target ([\\w\\d]+) to (\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher match = pattern.matcher(command);
    if (!match.matches()) {
      return false;
    }

    String startDate = match.group(1);
    String endDate = match.group(2);
    String targetCalendar = match.group(3).trim();
    String targetStartDate = match.group(4);

    try {
      List<Integer> eventIds = enhancedModel.copyEventsBetween(startDate, endDate,
              targetCalendar, targetStartDate);
      view.printSuccess("Copied " + eventIds.size() + " events from date range to calendar '"
              + targetCalendar + "'");
      return true;
    } catch (Exception e) {
      view.printError("Failed to copy events: " + e.getMessage());
      return false;
    }
  }

  private boolean handleOriginalCommands(String command) {
    if (handleCreateCommands(command)) {
      return true;
    }
    if (handleEditCommands(command)) {
      return true;
    }
    if (handleDeleteCommands(command)) {
      return true;
    }
    if (handleQueryCommands(command)) {
      return true;
    }
    handleStatusCommand(command);

    return false;
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
    Pattern unquotedPattern = Pattern.compile(
            "create event ([\\w-]+) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher unquotedMatch = unquotedPattern.matcher(command);
    if (unquotedMatch.matches()) {
      return handleSingleEventCreation(unquotedMatch);
    }

    Pattern quotedPattern = Pattern.compile(
            "create event \"([^\"]+)\" from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) " +
                    "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
            Pattern.CASE_INSENSITIVE);

    Matcher quotedMatch = quotedPattern.matcher(command);
    if (quotedMatch.matches()) {
      return handleSingleEventCreation(quotedMatch);
    }

    return false;
  }

  private boolean handleSingleEventCreation(Matcher match) {
    String subject = match.group(1);
    String[] startParts = match.group(2).split("T");
    String[] endParts = match.group(3).split("T");

    try {
      Method method = model.getClass().getMethod("createSingleEvent",
              String.class, String.class, String.class, String.class, String.class,
              String.class, String.class, Status.class);
      Object result = method.invoke(model, subject, "", startParts[0], startParts[1],
              endParts[0], endParts[1], "", Status.PUBLIC);
      int eventId = (Integer) result;
      view.printSuccess("Created single event '" + subject + "' (id=" + eventId + ")");
      return true;
    } catch (Exception e) {
      return false;
    }
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
      return handleTimedEventSeries(forTimesMatch, true);
    }

    Matcher untilDateMatch = untilDatePattern.matcher(command);
    if (untilDateMatch.matches()) {
      return handleTimedEventSeries(untilDateMatch, false);
    }

    return false;
  }

  private boolean handleTimedEventSeries(Matcher match, boolean useRecurrence) {
    String subject = match.group(1);
    String[] startParts = match.group(2).split("T");
    String[] endParts = match.group(3).split("T");
    String weekdays = match.group(4);

    try {
      Method method = model.getClass().getMethod(
              "createTimedEventSeries", String.class, String.class, String.class,
              String.class, String.class, String.class,
              String.class, int.class, String.class, String.class, Status.class);

      Object result;
      if (useRecurrence) {
        int times = Integer.parseInt(match.group(5));
        result = method.invoke(model, subject, "", startParts[0], startParts[1],
                endParts[0], endParts[1], weekdays, times, null, "", Status.PUBLIC);
      } else {
        String untilDate = match.group(5);
        result = method.invoke(model, subject, "", startParts[0], startParts[1],
                endParts[0], endParts[1], weekdays, 0, untilDate, "", Status.PUBLIC);
      }

      int seriesId = (Integer) result;
      view.printSuccess("Created timed event series '" + subject + "' (seriesId=" + seriesId + ")");
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      Method method = model.getClass().getMethod("createAllDayEvent",
              String.class, String.class, String.class, String.class, Status.class);
      Object result = method.invoke(model, subject, "", date, "", Status.PUBLIC);
      int eventId = (Integer) result;
      view.printSuccess("Created all-day event '" + subject + "' (id=" + eventId + ")");
      return true;
    } catch (Exception e) {
      return false;
    }
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
      return handleAllDayEventSeries(forTimesMatch, true);
    }

    Matcher untilDateMatch = untilDatePattern.matcher(command);
    if (untilDateMatch.matches()) {
      return handleAllDayEventSeries(untilDateMatch, false);
    }

    return false;
  }

  private boolean handleAllDayEventSeries(Matcher match, boolean useRecurrence) {
    String subject = match.group(1);
    String date = match.group(2);
    String weekdays = match.group(3);

    try {
      Method method = model.getClass().getMethod("createAllDayEventSeries",
              String.class, String.class, String.class, String.class, int.class,
              String.class, String.class, Status.class);

      Object result;
      if (useRecurrence) {
        int times = Integer.parseInt(match.group(4));
        result = method.invoke(model, subject, "", date, weekdays, times, null, "", Status.PUBLIC);
      } else {
        String untilDate = match.group(4);
        result = method.invoke(model, subject, "", date, weekdays, 0, untilDate, "", Status.PUBLIC);
      }

      int seriesId = (Integer) result;
      view.printSuccess("Created all-day event series '" + subject + "' (seriesId=" +
              seriesId + ")");
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      if (editType.equals("edit event")) {
        Method method = model.getClass().getMethod("editSingleEvent",
                String.class, String.class, String.class, String.class);
        method.invoke(model, subject, startTime, property, newValue);
        view.printSuccess("Edited single event '" + subject + "'");
      } else if (editType.equals("edit events")) {
        Method method = model.getClass().getMethod("editEventsFrom",
                String.class, String.class, String.class, String.class);
        Object result = method.invoke(model, subject, startTime, property, newValue);
        int count = (Integer) result;
        view.printSuccess("Edited " + count + " events from '" + subject + "'");
      } else {
        Method method = model.getClass().getMethod("editEntireSeries",
                String.class, String.class, String.class, String.class);
        Object result = method.invoke(model, subject, startTime, property, newValue);
        int count = (Integer) result;
        view.printSuccess("Edited entire series (" + count + " events) of '" + subject + "'");
      }
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      if (deleteType.equals("delete event")) {
        Method method = model.getClass().getMethod("deleteSingleEvent",
                String.class, String.class);
        method.invoke(model, subject, startTime);
        view.printSuccess("Deleted single event '" + subject + "'");
      } else if (deleteType.equals("delete events")) {
        Method method = model.getClass().getMethod("deleteEventsFrom",
                String.class, String.class);
        Object result = method.invoke(model, subject, startTime);
        int count = (Integer) result;
        view.printSuccess("Deleted " + count + " events from '" + subject + "'");
      } else {
        Method method = model.getClass().getMethod("deleteEntireSeries",
                String.class, String.class);
        Object result = method.invoke(model, subject, startTime);
        int count = (Integer) result;
        view.printSuccess("Deleted entire series (" + count + " events) of '" + subject + "'");
      }
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      Method method = model.getClass().getMethod("getEventsOnDate",
              String.class);
      Object result = method.invoke(model, date);
      @SuppressWarnings("unchecked")
      List<Event> events = (List<Event>) result;
      view.printEventsOnDate(date, events);
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      Method method = model.getClass().getMethod("getEventsInRange", String.class, String.class);
      Object result = method.invoke(model, startTime, endTime);
      @SuppressWarnings("unchecked")
      List<Event> events = (List<Event>) result;
      view.printEventsInRange(startTime, endTime, events);
      return true;
    } catch (Exception e) {
      return false;
    }
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

    try {
      Method method = model.getClass().getMethod("isBusy", String.class);
      Object result = method.invoke(model, dateTime);
      boolean isBusy = (Boolean) result;
      view.printStatus(dateTime, isBusy);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}