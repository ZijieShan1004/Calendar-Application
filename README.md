# Calendar-Application  
This is a project I developed during my CS3500 course(Object Oriented Design) at Northeastern University.  
This a Calendar Application with different functions.

1. Design changes and justification

- Refactored controller into CalendarControllerV2 and CalendarGUIController to follow single responsibility principle
- Replaced text inputs with Swing components like JSpinner and JComboBox to reduce input errors
- Default calendar uses system timezone and supports multiple calendars and custom timezone selection
- Errors are caught in controller and shown in dialog without exposing stack trace

2. Running instructions

- Double click CalendarApp jar to launch GUI mode
- Run `java -jar CalendarApp.jar` to launch GUI mode
- Run `java -jar CalendarApp.jar --mode interactive` to start interactive text mode
- Run `java -jar CalendarApp.jar --mode headless scripts/script.txt` to run script and exit

3. Features

### Required
- Create single event
- Create all day event
- View events by date or time range
- Error handling with user friendly messages

### Extra credit
- Multiple calendar management
- Event editing for subject time and location

4. Work Distribution
    1. For the most part, we tried our best to work together on all aspects of the code. However
       there are some parts that we each took more of a lead on
    2. Thanos \- Wrote most of the Controller package and its tests.
    3. Zijie \- Wrote most of the View package and its tests.
    4. Together \- the design of the Model package, many of the tests, the Main method. We also
       worked on actually implementing the View and Controller together
5. Notes for Grading
    1. We make a lot of tests that hopefully cover a lot of the edge cases that could potentially
       cause bugs so there should be no issues with running the code. 
   2. GUI shows schedule view for up to 10 events 
   3. Check system timezone if features fail  
