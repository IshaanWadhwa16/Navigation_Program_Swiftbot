import java.io.File;
//Imports File so the program can work with the log file that gets created.
import java.io.IOException;
//Imports IOException so file-writing errors can be handled safely.
import java.util.ArrayList;
//Imports ArrayList so command histories can be stored dynamically.
import java.util.List;
//Imports List so movement and received commands can be stored as lists.

/**
 * Coordinates parsing, history, robot movement, retracing and logging.
 */
public final class NavigationSystem {
    private final SwiftBotController robotController;
 // Stores the SwiftBot controller used to control the real robot.
    private final CommandParser commandParser;
 // Stores the parser used to validate and understand QR commands.
    private final LogWriter logWriter;
 // Stores the object used to create the command history log file.


    // Stores every movement that has actually been executed.
    // It deliberately contains MovementCommand objects only, never T or W.
    private final List<MovementCommand> movementHistory = new ArrayList<MovementCommand>();

    // Stores QR commands received from the user, including T commands.
    // W is also added before its log is written so the log records it.
    private final List<String> receivedCommandHistory = new ArrayList<String>();

    private final long programStartNanos = System.nanoTime();

    public NavigationSystem(SwiftBotController robotController) {
    	// Constructor used to set up the NavigationSystem object.
        this.robotController = robotController;
     // Stores the SwiftBot controller passed into this class.
        this.commandParser = new CommandParser();
     // Creates the object used to validate QR commands.
        this.logWriter = new LogWriter();
     // Creates the object used to write the navigation log file.
    }

    /**
     * Validates the complete QR code first, then executes it.
     */
    public void processQrText(String qrText) {
        ParsedQrCode parsedQrCode;

        try {
            parsedQrCode = commandParser.parse(qrText);
        } catch (IllegalArgumentException exception) {
            System.out.println("INVALID QR CODE: " + exception.getMessage());
            System.out.println("No movement was performed. Please show a corrected QR code.");
            return;
        }

        // The brief requires commands to be displayed before movement starts.
        System.out.println("Valid command(s) received: " + qrText.trim());
        receivedCommandHistory.add(qrText.trim());

        switch (parsedQrCode.getType()) {
     // Checks which type of validated command was received.
            case MOVEMENTS:
            	// runs movement command
                executeMovementSequence(parsedQrCode.getMovements());
                break;

            case RETRACE:
                executeRetrace(parsedQrCode.getRetraceCount());
                break;

            case WRITE_LOG:
                writeLog();
                break;

            default:
                throw new IllegalStateException("Unsupported parsed QR type.");
        }
    }

    private void executeMovementSequence(List<MovementCommand> movements) {
    	// Executes one or more validated movement commands.
        for (MovementCommand movement : movements) {
        	// Loops through every movement in the QR command sequence.
            if (!robotController.isRunning()) {
                return;
             // Stops movement immediately if the program is no longer running.
            }

            System.out.println("Executing: " + movement.toQrText());
         // Displays the movement that is about to be executed.
            robotController.executeMovement(movement);
         // Sends the movement command to the SwiftBot controller.

            // Add only after execution so history represents completed movements.
            if (robotController.isRunning()) {
                movementHistory.add(movement);
             // Makes sure the program is still running after the movement.
             // Adds the completed movement to movement history.
            }
        }
    }

    private void executeRetrace(int count) {
    	// Handles the T command and repeats previous movements.
        if (count > movementHistory.size()) {
        	// Checks whether the user requested more movements than exist in history.
            System.out.println("RETRACE REJECTED: " + count
                    + " exceeds the " + movementHistory.size()
                    + " movement(s) executed so far.");
            return;
            //stops the retrace if its too large
        }

        System.out.println("Retracing the previous " + count + " movement(s).");
     // Tells the user how many movements will be repeated.
        
        // Begin at the most recent movement and work backwards.
        int firstIndex = movementHistory.size() - 1;
     // Gets the index of the most recently executed movement.
        int lastIndex = movementHistory.size() - count;
     // Calculates the final index needed for the retrace.


        // Store repeated commands separately so adding them does not change
        // the indexes while this loop is running.
        List<MovementCommand> repeatedMovements = new ArrayList<MovementCommand>();

        for (int index = firstIndex; index >= lastIndex; index--) {
        	// Loops backwards through the requested previous movements.
            if (!robotController.isRunning()) {
                return;
            }

            MovementCommand movement = movementHistory.get(index);
            System.out.println("Retrace executing: " + movement.toQrText());
            robotController.executeMovement(movement);

            if (robotController.isRunning()) {
                repeatedMovements.add(movement);
            }
        }

        // Replayed movements count as movements the SwiftBot has now executed.
        movementHistory.addAll(repeatedMovements);
    }

    private void writeLog() {
        try {
            File logFile = logWriter.writeLog(receivedCommandHistory, programStartNanos);
            System.out.println("Log written successfully.");
            System.out.println("Complete file path: " + logFile.getAbsolutePath());
        } catch (IOException exception) {
            System.out.println("LOG ERROR: The log file could not be written.");
            System.out.println("Reason: " + exception.getMessage());
        }
    }
}
