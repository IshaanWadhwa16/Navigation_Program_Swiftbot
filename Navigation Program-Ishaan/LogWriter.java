import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes the required W-command log using standard Java file classes.
 */
public final class LogWriter {
    private static final DateTimeFormatter FILE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter CLOCK_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public File writeLog(List<String> receivedCommandHistory, long programStartNanos)
            throws IOException {
    	 // This method creates the log file and writes the required information into it.

        String fileName = "SwiftBot_Navigation_Log_"
                + LocalDateTime.now().format(FILE_TIME_FORMAT)
                + ".txt";

        File logFile = new File(fileName).getAbsoluteFile();
        long elapsedNanos = System.nanoTime() - programStartNanos;
        Duration elapsed = Duration.ofNanos(elapsedNanos);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("SWIFTBOT NAVIGATION COMMAND LOG");
            writer.newLine();
            writer.write("================================");
            writer.newLine();
            writer.write("Commands received:");
            writer.newLine();

            if (receivedCommandHistory.isEmpty()) {
                writer.write("No commands had been received before W.");
                writer.newLine();
            } else {
                for (int index = 0; index < receivedCommandHistory.size(); index++) {
                    writer.write((index + 1) + ". " + receivedCommandHistory.get(index));
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("Total program execution time: " + formatDuration(elapsed));
            writer.newLine();
            writer.write("Time the log was written (HH:MM:SS): "
                    + LocalTime.now().format(CLOCK_TIME_FORMAT));
            writer.newLine();
        }

        return logFile;
    }

    private String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
