import controller.CalendarController;
import controller.CalendarGUIController;
import model.CalendarModel;
import model.CalendarModelImpl;
import model.CalendarModelImplV2;
import view.CalendarGUI;
import view.CalendarView;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main application class supporting interactive, headless, and GUI modes.
 */
public class CalendarApp {

  /**
   * Main entry point for CalendarApp.
   * Parses command line arguments and launches the GUI or console mode.
   *
   * @param args command line arguments to select mode and script path
   */
  public static void main(String[] args) {
    if (args.length < 2 || !args[0].equals("--mode")) {
      System.err.println("Usage: java CalendarApp --mode <interactive|headless|gui> [file]");
      return;
    }

    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        runInteractiveMode();
        break;
      case "headless":
        if (args.length < 3) {
          System.err.println("Headless mode requires a command file");
          return;
        }
        runHeadlessMode(args[2]);
        break;
      case "gui":
        runGUIMode();
        break;
      default:
        System.err.println("Mode must be: interactive, headless, or gui");
    }
  }

  private static void runInteractiveMode() {
    CalendarModel model = new CalendarModelImpl();
    CalendarView view = new CalendarView();
    CalendarController controller = new CalendarController(model, view);

    System.out.println("Welcome to your calendar! Type 'exit' to quit.");

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      while (true) {
        System.out.print("> ");
        String command = reader.readLine();
        if (command == null || !controller.processCommand(command)) {
          System.out.println("Goodbye!");
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading input: " + e.getMessage());
    }
  }

  private static void runHeadlessMode(String filename) {
    CalendarModel model = new CalendarModelImpl();
    CalendarView view = new CalendarView();
    CalendarController controller = new CalendarController(model, view);

    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String command;
      while ((command = reader.readLine()) != null) {
        command = command.trim();
        if (command.isEmpty() || command.startsWith("#")) {
          continue;
        }
        System.out.println("> " + command);
        if (!controller.processCommand(command)) {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
    }
  }

  private static void runGUIMode() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // Use default look and feel
    }

    SwingUtilities.invokeLater(() -> {
      try {
        // Try enhanced model first, fall back to basic
        CalendarModel model;
        try {
          model = new CalendarModelImplV2();
          System.out.println("Using enhanced model with multi-calendar support");
        } catch (Exception e) {
          model = new CalendarModelImpl();
          System.out.println("Using basic model");
        }

        CalendarGUIController controller = new CalendarGUIController(model);
        CalendarGUI gui = new CalendarGUI(controller);
        controller.setView(gui);

        gui.setVisible(true);
        System.out.println("Calendar GUI started successfully!");

      } catch (Exception e) {
        System.err.println("Error starting GUI: " + e.getMessage());
        e.printStackTrace();
      }
    });
  }
}