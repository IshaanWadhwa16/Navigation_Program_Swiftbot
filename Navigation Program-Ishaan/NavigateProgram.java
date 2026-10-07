
public final class NavigateProgram {
    private static final long QR_RETRY_DELAY_MILLISECONDS = 350;
 // Waits 350 milliseconds between QR scanning attempts.
    private static final long AFTER_COMMAND_DELAY_MILLISECONDS = 1200;
 // Waits 1.2 seconds after processing a command before scanning again.

    private NavigateProgram() {
        // Prevents accidental construction of this main-only class.
    }

    public static void main(String[] args) {
    	//Main Method
        SwiftBotController robotController = new SwiftBotController();
     // Creates the controller used to access the SwiftBot hardware.
        NavigationSystem navigationSystem = new NavigationSystem(robotController);
     // Creates the navigation system and gives it access to the robot controller.

        printHeader();
     // Displays the program title, commands and instructions on the console.
        robotController.enableStopButton();
     // Enables the physical X button so the user can terminate the program.

        try {
            runQrScanningLoop(robotController, navigationSystem);
         // Starts a try block so the program can always shut down safely.
        // Starts continuously scanning and processing QR codes. 
        } finally {
            robotController.shutdown();
            System.out.println("Program terminated safely.");
        }
    }

    private static void runQrScanningLoop(
            SwiftBotController robotController,
            NavigationSystem navigationSystem) {
    	// Controls the main loop that continuously scans QR codes.

        String lastAcceptedQrText = "";
     // Stores the previous QR code so it is not accidentally scanned twice.

        while (robotController.isRunning()) {
        	// Keeps the program running until the physical X button is pressed.
            System.out.println("\nShow the next QR code to the SwiftBot camera.");
            System.out.println("Press the physical X button to terminate.");

            String decodedQrText = waitForQrCode(robotController, lastAcceptedQrText);

            if (!robotController.isRunning()) {
            	// Checks whether X was pressed while waiting for a QR code.

                break;
             // Exits the scanning loop if the program has been stopped.
            }

            if (!decodedQrText.isEmpty()) {
            	// Continues only when valid QR text has actually been received.
                System.out.println("Decoded QR text: " + decodedQrText);
                navigationSystem.processQrText(decodedQrText);
                lastAcceptedQrText = decodedQrText;
                sleep(AFTER_COMMAND_DELAY_MILLISECONDS);
             // Waits briefly before allowing another QR code to be scanned.            
            }
        }
    }

    /**
     * Keeps taking camera images until a QR code is decoded.
     * The same QR text is not accepted twice in a row; the user should move
     * to a different QR code before the next command is processed.
     */
    private static String waitForQrCode(
            SwiftBotController robotController,
            String lastAcceptedQrText) {
    	// Waits until the SwiftBot camera successfully reads a new QR code.

        while (robotController.isRunning()) {
        	// Continues scanning while the program is still running.
            try {
            	// Attempts to scan and decode a QR code.
                String decodedText = robotController.scanQrCode();

                if (!decodedText.isEmpty()
                        && !decodedText.equalsIgnoreCase(lastAcceptedQrText)) {
                    return decodedText;
                }
            } catch (Exception exception) {
            	// Catches errors when the camera cannot decode the current image.
            	
                // A frame with no readable QR code is normal, so continue scanning.
            }

            sleep(QR_RETRY_DELAY_MILLISECONDS);
         // Waits 350 milliseconds before trying to scan again.
        }

        return "";
     // Returns empty text if the program was stopped before a QR was found.
    }

    private static void printHeader() {
        System.out.println("====================================================");
        System.out.println("            SWIFTBOT NAVIGATION PROGRAM");
        System.out.println("====================================================");
        System.out.println("F,speed,seconds   Move forward");
        System.out.println("B,speed,seconds   Move backward");
        System.out.println("L,speed,seconds   Turn left 90 degrees, then move");
        System.out.println("R,speed,seconds   Turn right 90 degrees, then move");
        System.out.println("T,count           Repeat previous movements");
        System.out.println("W                 Write command history to a log");
        System.out.println("Example sequence: F,50,2;R,60,1");
        System.out.println("Speed: 1-100 | Duration: 1-6 seconds");
        System.out.println("Maximum 10 commands per QR; T is forbidden in a sequence.");
        System.out.println("====================================================");
    }

    private static void sleep(long milliseconds) {
    	// Helper method used to pause the program for a specified amount of time.
        try {
        	// Attempts to pause the current thread.
            Thread.sleep(milliseconds);
         // Pauses the program for the supplied number of milliseconds.
        } catch (InterruptedException exception) {
        	// Handles the situation where the sleep is interrupted.
            Thread.currentThread().interrupt();
         // Restores the interrupted status instead of ignoring the interruption.
        }
    }
}
