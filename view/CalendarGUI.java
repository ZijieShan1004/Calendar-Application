package view;

import controller.CalendarGUIController;
import model.Event;
import model.Status;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.SpinnerDateModel;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Single GUI for the calendar application that adapts to model capabilities.
 */
public class CalendarGUI extends JFrame implements CalendarGUIViewInterface {
  private final CalendarGUIController controller;

  // Date navigation
  private JLabel dateLabel;
  private JSpinner dateSpinner;
  private JButton prevButton;
  private JButton nextButton;
  private JButton todayButton;

  // Event display
  private JList<String> eventList;
  private DefaultListModel<String> eventListModel;
  private List<Event> currentEvents;

  // Calendar selection (enhanced only)
  private JComboBox<String> calendarComboBox;
  private JButton newCalendarButton;

  // Event creation
  private JTextField subjectField;
  private JSpinner startDateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endDateSpinner;
  private JSpinner endTimeSpinner;
  private JTextField locationField;
  private JComboBox<Status> statusComboBox;
  private JCheckBox allDayCheckBox;
  private JButton createButton;

  // Event editing (enhanced only)
  private JPanel editPanel;
  private JTextField editSubjectField;
  private JSpinner editStartDateSpinner;
  private JSpinner editStartTimeSpinner;
  private JSpinner editEndDateSpinner;
  private JSpinner editEndTimeSpinner;
  private JTextField editLocationField;
  private JComboBox<Status> editStatusComboBox;
  private JButton saveButton;
  private JButton cancelButton;
  private JButton deleteButton;
  private Event editEvent;

  // Status
  private JLabel statusLabel;

  // Current date
  private LocalDate currentDate;

  private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern(
          "EEEE, MMMM d, yyyy");

  /**
   * Construct the Calendar GUI window.
   * Initializes components layouts and event handlers.
   * Sets default close operation size and position.
   * Loads events for the current date.
   *
   * @param controller the controller handling GUI actions.
   */
  public CalendarGUI(CalendarGUIController controller) {
    super("Calendar Application");
    this.controller = controller;
    this.currentDate = LocalDate.now();

    initComponents();
    layoutComponents();
    setupEvents();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null);

    refreshEvents();
  }

  private void initComponents() {
    // Date navigation
    dateLabel = new JLabel();
    updateDateLabel();

    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    dateSpinner.setValue(java.sql.Date.valueOf(currentDate));

    prevButton = new JButton("◀ Previous");
    nextButton = new JButton("Next ▶");
    todayButton = new JButton("Today");

    // Event list
    eventListModel = new DefaultListModel<>();
    eventList = new JList<>(eventListModel);
    eventList.setVisibleRowCount(10);

    // Calendar selection (enhanced features only)
    if (controller.hasEnhancedFeatures()) {
      calendarComboBox = new JComboBox<>();
      calendarComboBox.addItem("Default");
      newCalendarButton = new JButton("New Calendar");
    }

    // Event creation form
    subjectField = new JTextField(15);

    startDateSpinner = new JSpinner(new SpinnerDateModel());
    startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
    startDateSpinner.setValue(java.sql.Date.valueOf(currentDate));

    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
    startTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.now()));

    endDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
    endDateSpinner.setValue(java.sql.Date.valueOf(currentDate));

    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
    endTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.now().plusHours(1)));

    locationField = new JTextField(15);
    statusComboBox = new JComboBox<>(Status.values());
    allDayCheckBox = new JCheckBox("All Day");
    createButton = new JButton("Create Event");

    // Event editing (enhanced features only)
    if (controller.hasEnhancedFeatures()) {
      createEditPanel();
    }

    // Status label
    statusLabel = new JLabel(" ");
  }

  private void createEditPanel() {
    editPanel = new JPanel(new GridBagLayout());
    editPanel.setBorder(new TitledBorder("Edit Event"));
    editPanel.setVisible(false);

    editSubjectField = new JTextField(15);
    editStartDateSpinner = new JSpinner(new SpinnerDateModel());
    editStartDateSpinner.setEditor(new JSpinner.DateEditor(editStartDateSpinner, "yyyy-MM-dd"));
    editStartTimeSpinner = new JSpinner(new SpinnerDateModel());
    editStartTimeSpinner.setEditor(new JSpinner.DateEditor(editStartTimeSpinner, "HH:mm"));
    editEndDateSpinner = new JSpinner(new SpinnerDateModel());
    editEndDateSpinner.setEditor(new JSpinner.DateEditor(editEndDateSpinner, "yyyy-MM-dd"));
    editEndTimeSpinner = new JSpinner(new SpinnerDateModel());
    editEndTimeSpinner.setEditor(new JSpinner.DateEditor(editEndTimeSpinner, "HH:mm"));
    editLocationField = new JTextField(15);
    editStatusComboBox = new JComboBox<>(Status.values());

    saveButton = new JButton("Save");
    cancelButton = new JButton("Cancel");
    deleteButton = new JButton("Delete");

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    editPanel.add(new JLabel("Subject:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    editPanel.add(editSubjectField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    editPanel.add(new JLabel("Start:"), gbc);
    gbc.gridx = 1;
    JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    startPanel.add(editStartDateSpinner);
    startPanel.add(editStartTimeSpinner);
    editPanel.add(startPanel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    editPanel.add(new JLabel("End:"), gbc);
    gbc.gridx = 1;
    JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    endPanel.add(editEndDateSpinner);
    endPanel.add(editEndTimeSpinner);
    editPanel.add(endPanel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    editPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    editPanel.add(editLocationField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.NONE;
    editPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    editPanel.add(editStatusComboBox, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(deleteButton);
    editPanel.add(buttonPanel, gbc);
  }

  private void layoutComponents() {
    setLayout(new BorderLayout(10, 10));

    // Top panel
    JPanel topPanel = new JPanel(new BorderLayout());

    // Calendar selection (enhanced only)
    if (controller.hasEnhancedFeatures()) {
      JPanel calPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      calPanel.setBorder(new TitledBorder("Calendar"));
      calPanel.add(new JLabel("Active:"));
      calPanel.add(calendarComboBox);
      calPanel.add(newCalendarButton);
      topPanel.add(calPanel, BorderLayout.WEST);
    }

    // Date navigation
    JPanel datePanel = new JPanel(new FlowLayout());
    datePanel.setBorder(new TitledBorder("Date"));
    datePanel.add(prevButton);
    datePanel.add(dateLabel);
    datePanel.add(nextButton);
    datePanel.add(dateSpinner);
    datePanel.add(todayButton);
    topPanel.add(datePanel, BorderLayout.CENTER);

    add(topPanel, BorderLayout.NORTH);

    // Center - event list
    JPanel centerPanel = new JPanel(new BorderLayout());
    String title = "Schedule View";
    if (controller.hasEnhancedFeatures()) {
      title += " (Double-click to edit)";
    }
    centerPanel.setBorder(new TitledBorder(title));
    centerPanel.add(new JScrollPane(eventList), BorderLayout.CENTER);
    add(centerPanel, BorderLayout.CENTER);

    // Right - event forms
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setPreferredSize(new Dimension(280, 0));

    // Create form
    JPanel createPanel = new JPanel(new GridBagLayout());
    createPanel.setBorder(new TitledBorder("Create Event"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    createPanel.add(new JLabel("Subject:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(subjectField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(allDayCheckBox, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    createPanel.add(new JLabel("Start Date:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(startDateSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(new JLabel("Start Time:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(startTimeSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(new JLabel("End Date:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(endDateSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(new JLabel("End Time:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(endTimeSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(locationField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.fill = GridBagConstraints.NONE;
    createPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(statusComboBox, gbc);

    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    createPanel.add(createButton, gbc);

    rightPanel.add(createPanel, BorderLayout.NORTH);

    // Edit form (enhanced only)
    if (controller.hasEnhancedFeatures()) {
      rightPanel.add(editPanel, BorderLayout.CENTER);
    }

    add(rightPanel, BorderLayout.EAST);

    // Bottom - status
    add(statusLabel, BorderLayout.SOUTH);

    // Padding
    ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
  }

  private void setupEvents() {
    // Date navigation
    prevButton.addActionListener(e -> changeDate(-1));
    nextButton.addActionListener(e -> changeDate(1));
    todayButton.addActionListener(e -> goToToday());
    dateSpinner.addChangeListener(e -> {
      java.util.Date date = (java.util.Date) dateSpinner.getValue();
      currentDate = new java.sql.Date(date.getTime()).toLocalDate();
      updateDateLabel();
      refreshEvents();
    });

    // Calendar management (enhanced only)
    if (controller.hasEnhancedFeatures()) {
      calendarComboBox.addActionListener(e -> switchCalendar());
      newCalendarButton.addActionListener(e -> createNewCalendar());

      // Event editing
      eventList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            int index = eventList.locationToIndex(e.getPoint());
            if (index >= 0 && currentEvents != null && index < currentEvents.size()) {
              editEventDialog(currentEvents.get(index));
            }
          }
        }
      });

      saveButton.addActionListener(e -> saveEdit());
      cancelButton.addActionListener(e -> cancelEdit());
      deleteButton.addActionListener(e -> deleteEvent());
    }

    // Event creation
    allDayCheckBox.addActionListener(e -> toggleAllDay());
    createButton.addActionListener(e -> createEvent());
  }

  private void changeDate(int days) {
    currentDate = currentDate.plusDays(days);
    updateDateLabel();
    dateSpinner.setValue(java.sql.Date.valueOf(currentDate));
    refreshEvents();
  }

  private void goToToday() {
    currentDate = LocalDate.now();
    updateDateLabel();
    dateSpinner.setValue(java.sql.Date.valueOf(currentDate));
    refreshEvents();
  }

  private void updateDateLabel() {
    dateLabel.setText(currentDate.format(DISPLAY_FORMAT));
  }

  private void switchCalendar() {
    String selected = (String) calendarComboBox.getSelectedItem();
    if (selected != null && controller.switchCalendar(selected)) {
      refreshEvents();
    }
  }

  private void createNewCalendar() {
    String name = JOptionPane.showInputDialog(this, "Calendar name:");
    if (name != null && !name.trim().isEmpty()) {
      if (controller.createCalendar(name.trim(), "UTC")) {
        calendarComboBox.addItem(name.trim());
        calendarComboBox.setSelectedItem(name.trim());
      }
    }
  }

  private void toggleAllDay() {
    boolean allDay = allDayCheckBox.isSelected();
    startTimeSpinner.setEnabled(!allDay);
    endTimeSpinner.setEnabled(!allDay);
    if (allDay) {
      startTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
      endTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0)));
    }
  }

  private void createEvent() {
    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      showError("Subject is required");
      return;
    }

    LocalDate startDate = ((java.sql.Date) startDateSpinner.getValue()).toLocalDate();
    LocalDate endDate = ((java.sql.Date) endDateSpinner.getValue()).toLocalDate();
    LocalTime startTime = ((java.sql.Time) startTimeSpinner.getValue()).toLocalTime();
    LocalTime endTime = ((java.sql.Time) endTimeSpinner.getValue()).toLocalTime();
    String location = locationField.getText().trim();
    Status status = (Status) statusComboBox.getSelectedItem();

    boolean success;
    if (allDayCheckBox.isSelected()) {
      success = controller.createAllDayEvent(
              subject, startDate.format(DATE_FORMAT), location, status);
    } else {
      success = controller.createSingleEvent(subject,
              startDate.format(DATE_FORMAT), startTime.format(TIME_FORMAT),
              endDate.format(DATE_FORMAT), endTime.format(TIME_FORMAT),
              location, status);
    }

    if (success) {
      clearForm();
      refreshEvents();
      showSuccess("Event created");
    }
  }

  private void editEventDialog(Event event) {
    editEvent = event;
    editSubjectField.setText(event.getSubject());
    editStartDateSpinner.setValue(java.sql.Date.valueOf(event.getStartDateTime().toLocalDate()));
    editStartTimeSpinner.setValue(java.sql.Time.valueOf(event.getStartDateTime().toLocalTime()));
    editEndDateSpinner.setValue(java.sql.Date.valueOf(event.getEndDateTime().toLocalDate()));
    editEndTimeSpinner.setValue(java.sql.Time.valueOf(event.getEndDateTime().toLocalTime()));
    editLocationField.setText(event.getLocation());
    editStatusComboBox.setSelectedItem(event.getStatus());

    editPanel.setVisible(true);
    revalidate();
    repaint();
  }

  private void saveEdit() {
    if (editEvent == null) {
      return;
    }

    String originalDateTime = editEvent.getStartDateTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    String newSubject = editSubjectField.getText().trim();

    if (controller.editEvent(editEvent.getSubject(), originalDateTime, "subject", newSubject)) {
      cancelEdit();
      refreshEvents();
      showSuccess("Event updated");
    }
  }

  private void cancelEdit() {
    editEvent = null;
    editPanel.setVisible(false);
    revalidate();
    repaint();
  }

  private void deleteEvent() {
    if (editEvent == null) {
      return;
    }

    int result = JOptionPane.showConfirmDialog(
            this, "Delete this event?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      String originalDateTime =
              editEvent.getStartDateTime().format(
                      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
      if (controller.deleteEvent(editEvent.getSubject(), originalDateTime)) {
        cancelEdit();
        refreshEvents();
        showSuccess("Event deleted");
      }
    }
  }

  private void clearForm() {
    subjectField.setText("");
    locationField.setText("");
    allDayCheckBox.setSelected(false);
    statusComboBox.setSelectedItem(Status.PUBLIC);
    startTimeSpinner.setEnabled(true);
    endTimeSpinner.setEnabled(true);
  }

  private void refreshEvents() {
    List<Event> events = controller.getEventsOnDate(currentDate.format(DATE_FORMAT));
    currentEvents = events;
    displayEvents(events);
  }

  @Override
  public void displayEvents(List<Event> events) {
    eventListModel.clear();
    if (events.isEmpty()) {
      eventListModel.addElement("No events for this date");
      return;
    }

    for (Event event : events) {
      LocalTime start = event.getStartDateTime().toLocalTime();
      LocalTime end = event.getEndDateTime().toLocalTime();

      String timeStr;
      if (start.equals(LocalTime.of(8, 0)) && end.equals(LocalTime.of(17, 0))) {
        timeStr = "[All Day] ";
      } else {
        timeStr = "[" + start.format(TIME_FORMAT) + "-" + end.format(TIME_FORMAT) + "] ";
      }

      String eventStr = timeStr + event.getSubject();
      if (!event.getLocation().isEmpty()) {
        eventStr += " @ " + event.getLocation();
      }
      if (event.getStatus() == Status.PRIVATE) {
        eventStr += " (Private)";
      }

      eventListModel.addElement(eventStr);
    }
  }

  @Override
  public void showSuccess(String message) {
    statusLabel.setText("✓ " + message);
    statusLabel.setForeground(Color.GREEN);
    Timer timer = new Timer(3000, e -> statusLabel.setText(" "));
    timer.setRepeats(false);
    timer.start();
  }

  @Override
  public void showError(String message) {
    statusLabel.setText("✗ " + message);
    statusLabel.setForeground(Color.RED);
    Timer timer = new Timer(5000, e -> statusLabel.setText(" "));
    timer.setRepeats(false);
    timer.start();
  }
}
