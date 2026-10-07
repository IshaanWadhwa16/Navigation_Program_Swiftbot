/**
 * The four movement directions supported by the Navigate task.
 */
public enum Direction {
	// Creates an enum called Direction to store the four possible movement directions.
    FORWARD('F'),
    BACKWARD('B'),
    LEFT('L'),
    RIGHT('R');

    private final char code;

    Direction(char code) {
        this.code = code;
     // Saves the supplied command letter inside the Direction object.
    }

    public char getCode() {
        return code;
     // Returns F, B, L or R depending on the direction.
    }

    /**
     * Converts a QR command letter into a Direction value.
     */
    public static Direction fromCode(char code) {
        char upperCaseCode = Character.toUpperCase(code);

        for (Direction direction : values()) {
        	// Loops through FORWARD, BACKWARD, LEFT and RIGHT.
            if (direction.code == upperCaseCode) {
                return direction;
            }
        }

        throw new IllegalArgumentException("Unsupported movement command: " + code);
     // Gives an error if the command is not F, B, L or R.
    }
}
