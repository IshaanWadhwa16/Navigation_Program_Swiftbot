/**
 * Stores one validated movement command read from a QR code.
 *
 * For F and B, speed and duration describe the complete movement.
 * For L and R, the robot first turns approximately 90 degrees and then
 * travels forward using the stored speed and duration.
 */
public final class MovementCommand {
	// Creates a class that stores the details of one movement command.
    private final Direction direction;
 // Stores the movement direction: Forward, Backward, Left or Right.
    private final int speed;
 // Stores the speed value of the movement.
    private final int durationSeconds;
 // Stores how many seconds the movement should last.

    public MovementCommand(Direction direction, int speed, int durationSeconds) {
    	// Constructor used to create a new movement command object.
        this.direction = direction;
        this.speed = speed;
        this.durationSeconds = durationSeconds;
        //saves these into the constructor
    }

    public Direction getDirection() {
    	// Getter method used to access the stored movement direction.
        return direction;
     // Returns the direction of this command.
    }

    public int getSpeed() {
        return speed;
     // Getter method used to access the stored speed.
     // Returns the speed value.
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Recreates the exact text format used inside a QR code.
     */
    public String toQrText() {
        return direction.getCode() + "," + speed + "," + durationSeconds;
    }

    @Override
 // Tells Java that this method replaces the default toString() method.
    
    public String toString() {
        return toQrText();
     // Returns a readable text version of the MovementCommand object.
     // Uses the same format as the original QR command.
    }
}
