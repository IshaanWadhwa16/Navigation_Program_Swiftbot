import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one fully validated QR code.
 *
 * A QR code is either:
 * 1. one or more movement commands,
 * 2. one retrace command, or
 * 3. one write-log command.
 */
public final class ParsedQrCode {
	// Represents a QR code after CommandParser has checked that it is valid.
    // final means this class cannot be extended by another class.
    public enum Type {
    	// An enum is used because there are only three possible QR code types.

        MOVEMENTS,
        RETRACE,
        WRITE_LOG
    }

    private final Type type;
 // Stores which type of QR code this object represents.
    private final List<MovementCommand> movements;
 // Stores the movement commands when the QR type is MOVEMENTS.
    private final int retraceCount;
 // Stores how many previous movements should be repeated for a T command.

    private ParsedQrCode(Type type, List<MovementCommand> movements, int retraceCount) {
    	// Private constructor used internally to create a validated QR object.
        this.type = type;
        // Stores the QR code type in this object.
        this.movements = movements;
        this.retraceCount = retraceCount;
    }

    public static ParsedQrCode movements(List<MovementCommand> movements) {
    	// Creates a ParsedQrCode representing one or more movement commands.
        return new ParsedQrCode(
                Type.MOVEMENTS,
                Collections.unmodifiableList(new ArrayList<MovementCommand>(movements)),
                0);
     // Creates a MOVEMENTS type ParsedQrCode.
        // A copy of the movement list is created and made unmodifiable.
        // Retrace count is 0 because this is not a T command.
    }

    public static ParsedQrCode retrace(int count) {
    	// Creates a ParsedQrCode representing a T retrace command.
        return new ParsedQrCode(Type.RETRACE, Collections.<MovementCommand>emptyList(), count);
        // Sets the type to RETRACE.
        // Uses an empty movement list because T does not directly contain movement commands.
        // Stores the number of previous movements that need to be repeated.
    }

    public static ParsedQrCode writeLog() {
    	// Creates a ParsedQrCode representing the W command.
        return new ParsedQrCode(Type.WRITE_LOG, Collections.<MovementCommand>emptyList(), 0);
     // Sets the type to WRITE_LOG.
    }

    public Type getType() {
    	// Getter method used to find out which type of QR command was received.
        return type;
     // Returns MOVEMENTS, RETRACE or WRITE_LOG.
    }

    public List<MovementCommand> getMovements() {
    	// Getter method used to access the validated movement commands.
        return movements;
     // Returns the stored list of movement commands.
    }

    public int getRetraceCount() {
    	// Getter method used to access the number from a T command.
        return retraceCount;
     // Returns how many previous movements should be retraced.
    }
}
