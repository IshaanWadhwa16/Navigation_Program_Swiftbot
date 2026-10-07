import java.awt.image.BufferedImage;

import swiftbot.Button;
import swiftbot.ButtonFunction;
import swiftbot.SwiftBotAPI;

//Imports BufferedImage so images captured by the SwiftBot camera can be stored.
/**
 * Contains every direct call to SwiftBot API 6.0.0.
 *
 * Keeping hardware code in one class makes the remaining program easier
 */
public final class SwiftBotController {
	  // This class controls the SwiftBot hardware such as camera, motors and buttons.
    // final means another class cannot extend this class.
    
    private final SwiftBotAPI swiftBot = SwiftBotAPI.INSTANCE;
    // SwiftBotAPI.INSTANCE gives access to the single SwiftBot API instance.
    // This object is used to communicate with the physical SwiftBot.

    private static final int TURN_SPEED = 40;
    private static final int MILLISECONDS_FOR_90_DEGREE_TURN = 620;

    // volatile allows the button callback and main loop to share this value safely.
    private volatile boolean running = true;
    // Stores whether the navigation program should continue running.
    // boolean can only contain true or false.
    // volatile makes changes visible between the X-button callback and main program.
    // true means the program should continue running.

    /**
     * Enables the physical X button so it stops the robot and program.
     */
    public void enableStopButton() {
    	// Disables existing button functions before assigning the new X-button function.

        swiftBot.disableAllButtons();

        swiftBot.enableButton(Button.X, new ButtonFunction() {
            @Override
         // Indicates that this run() method implements the ButtonFunction method.
            
            public void run() {
            	// This method automatically runs when the X button is pressed.
                running = false;
             // Changes running to false so the main navigation loop will stop.
                safeStop();
             // Immediately attempts to stop the SwiftBot motors.
                System.out.println("\nX button pressed. The program will terminate safely.");
            }
        });
    }

    /**
     * Takes a camera image and asks the API to decode a QR code from it.
     */
    public String scanQrCode() {
        BufferedImage qrImage = swiftBot.getQRImage();
        String decodedText = swiftBot.decodeQRImage(qrImage);

        if (decodedText == null) {
            return "";
        }

        return decodedText.trim();
    }

    /**
     * Executes one previously validated movement command.
     */
    public void executeMovement(MovementCommand command) {
        if (!running) {
            return;
        }

        int speed = command.getSpeed();
     // Gets the validated speed stored inside the MovementCommand object.
        int movementTimeMilliseconds = command.getDurationSeconds() * 1000;
        // Gets the duration in seconds and converts it into milliseconds.
        // For example, 2 seconds becomes 2000 milliseconds.

        try {
            switch (command.getDirection()) {
                case FORWARD:
                    // Equal positive wheel speeds move the robot forward.
                    swiftBot.move(speed, speed, movementTimeMilliseconds);
                    break;

                case BACKWARD:
                    // Equal negative wheel speeds move the robot backward.
                    swiftBot.move(-speed, -speed, movementTimeMilliseconds);
                    break;

                case LEFT:
                    turnLeft90Degrees();

                    if (running) {
                        // After turning, move forward at the QR speed and duration.
                        swiftBot.move(speed, speed, movementTimeMilliseconds);
                    }
                    break;

                case RIGHT:
                    turnRight90Degrees();

                    if (running) {
                        // After turning, move forward at the QR speed and duration.
                        swiftBot.move(speed, speed, movementTimeMilliseconds);
                    }
                    break;

                default:
                    throw new IllegalStateException("Unsupported direction.");
            }
        } finally {
            safeStop();
        }
    }

    private void turnLeft90Degrees() {
        // Left wheel backward and right wheel forward makes an on-the-spot left turn.
        swiftBot.move(-TURN_SPEED, TURN_SPEED, MILLISECONDS_FOR_90_DEGREE_TURN);
        safeStop();
    }

    private void turnRight90Degrees() {
        // Left wheel forward and right wheel backward makes an on-the-spot right turn.
        swiftBot.move(TURN_SPEED, -TURN_SPEED, MILLISECONDS_FOR_90_DEGREE_TURN);
        safeStop();
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Stops motors and disables lights/buttons before the program exits.
     */
    public void shutdown() {
        running = false;
        safeStop();
        swiftBot.disableAllButtons();
        swiftBot.disableButtonLights();
        swiftBot.disableUnderlights();
    }

    private void safeStop() {
        try {
            swiftBot.stopMove();
        } catch (Exception exception) {
            // A stop attempt should not crash the shutdown process.
        }
    }
}
