import java.util.ArrayList;
import java.util.List;
//Imports ArrayList so movement commands can be stored in a list.


/**
 * Validates and converts decoded QR text into program objects.
 *
 * All commands in a multiple-command QR code are validated before this
 * class returns. This ensures the robot cannot begin moving when a later
 * command in the same QR code is invalid.
 */
public final class CommandParser {
    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 100;
    private static final int MIN_DURATION_SECONDS = 1;
    private static final int MAX_DURATION_SECONDS = 6;
    private static final int MAX_COMMANDS_PER_QR = 10;

    public ParsedQrCode parse(String qrText) {
    	// This method receives the text scanned from the QR code and validates it.
        if (qrText == null || qrText.trim().isEmpty()) {
        // Checks whether the QR code is empty or contains no useful text.
            throw new IllegalArgumentException("The QR code contains no command text.");
         // Stops processing and gives the user a clear error message. 
        }

        String cleanedText = qrText.trim().toUpperCase();
     // Removes extra spaces and changes letters to uppercase.
        // For example, " f,50,2 " becomes "F,50,2".

        String[] commandParts = cleanedText.split(";", -1);
     // Splits multiple commands using the semicolon.
        // Example: F,50,2;R,60,1 becomes two separate commands.

        if (commandParts.length > MAX_COMMANDS_PER_QR) {
        	// Checks whether the QR code contains more than 10 commands.
            throw new IllegalArgumentException(
                    "A QR code can contain no more than " + MAX_COMMANDS_PER_QR + " commands.");
         // Rejects the QR code if it contains too many commands.
        }

        // T and W must each be the only command in their QR code.
        if (commandParts.length == 1) {
            String singleCommand = commandParts[0].trim();

            if (singleCommand.equals("W")) {
                return ParsedQrCode.writeLog();
             // Checks whether the command is W.
             // Returns a W command so the program can create the log file.
            }

            if (singleCommand.startsWith("T")) {
                return parseRetrace(singleCommand);
             // Checks whether the command starts with T for retrace.
             // Sends the T command to the retrace validation method.

            }
        }

        List<MovementCommand> movements = new ArrayList<MovementCommand>();
     // Creates a list that will store all validated movement commands.

        for (String commandPart : commandParts) {
        	// Goes through every command found inside the QR code.
            String commandText = commandPart.trim();
         // Removes unnecessary spaces from the current command.

            if (commandText.isEmpty()) {
            	// Checks for an empty command caused by an incorrect semicolon.
                throw new IllegalArgumentException("A command is missing before or after a semicolon.");
             // Rejects the complete QR code.
            }

            if (commandText.startsWith("T")) {
                throw new IllegalArgumentException(
                        "T cannot be used inside a sequence of multiple commands.");
            }

            if (commandText.equals("W") || commandText.startsWith("W,")) {
                throw new IllegalArgumentException(
                        "W must be used by itself in a QR code.");
            }

            movements.add(parseMovement(commandText));
        }

        return ParsedQrCode.movements(movements);
     // Returns all validated movement commands to the navigation system.
    }

    private MovementCommand parseMovement(String commandText) {
    	// This method validates F, B, L and R movement commands.
    	
        String[] values = commandText.split(",", -1);
     // Separates the command into command, speed and duration.
        // Example: F,50,2 becomes F / 50 / 2.

        if (values.length != 3) {
            throw new IllegalArgumentException(
                    "Movement commands must use the format F,speed,seconds (or B/L/R). Received: "
                            + commandText);
        }

        if (values[0].trim().length() != 1) {
            throw new IllegalArgumentException("The command letter must be F, B, L or R.");
         // Checks that the first value contains only one command letter.
         // Rejects an invalid command name.
        }

        Direction direction;
     // Creates a Direction variable for F, B, L or R.

        try {
        	// Attempts to convert the QR command letter into a Direction. gets the first character to convert it to direction
            direction = Direction.fromCode(values[0].trim().charAt(0));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("The command letter must be F, B, L or R.");
        }

        int speed = parseInteger(values[1].trim(), "speed");
        int duration = parseInteger(values[2].trim(), "duration");

        if (speed < MIN_SPEED || speed > MAX_SPEED) {
            throw new IllegalArgumentException("Speed must be between 1 and 100.");
        }

        if (duration < MIN_DURATION_SECONDS || duration > MAX_DURATION_SECONDS) {
            throw new IllegalArgumentException("Duration must be between 1 and 6 seconds.");
        }

        return new MovementCommand(direction, speed, duration);
    }

    private ParsedQrCode parseRetrace(String commandText) {
    	// This method validates the T retrace command.
        String[] values = commandText.split(",", -1);

        if (values.length != 2 || !values[0].trim().equals("T")) {
        	// Checks that retrace contains exactly T and a number.
            throw new IllegalArgumentException("Retrace must use the format T,count.");
        }

        int count = parseInteger(values[1].trim(), "retrace count");

        if (count < 1) {
            throw new IllegalArgumentException("Retrace count must be at least 1.");
        }

        return ParsedQrCode.retrace(count);
    }

    private int parseInteger(String value, String fieldName) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("The " + fieldName + " value is missing.");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("The " + fieldName + " must be a whole number.");
        }
    }
}
