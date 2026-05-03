package ubb.bmad.logsentinel.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Robust file tailing mechanism using RandomAccessFile.
 * Implements polling-based tailing with rotation and truncation detection.
 */
public class LogTailer {
    private volatile boolean running = true;
    private final boolean jumpToEnd;
    public static final int MAX_LINE_LENGTH = 8192; // 8KB limit per line

    public LogTailer(boolean jumpToEnd) {
        this.jumpToEnd = jumpToEnd;
    }

    /**
     * Stops the tailing loop gracefully.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Starts the tailing loop on the current thread.
     * 
     * @param filePath      Path to the log file.
     * @param lineProcessor Callback for each new line detected.
     */
    public void startTailing(String filePath, Consumer<String> lineProcessor) {
        File file = new File(filePath);
        long lastPointer = 0;

        // Initialize pointer if jumping to end
        if (jumpToEnd && file.exists()) {
            lastPointer = file.length();
        }

        while (running) {
            if (!file.exists()) {
                System.out.println("Waiting for log file to be created: " + filePath);
                waitForFile(file);
                lastPointer = 0; // Reset for new file
            }

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                // Check for truncation or rotation
                if (file.length() < lastPointer) {
                    System.out.println("Log rotation or truncation detected. Resetting pointer.");
                    lastPointer = 0;
                }

                raf.seek(lastPointer);

                String line;
                while (running && (line = readBoundedLine(raf)) != null) {
                    lineProcessor.accept(line);
                    lastPointer = raf.getFilePointer();
                }

                // Wait for more data or for file to appear/be replaced
                if (running) {
                    Thread.sleep(500); // Prevent CPU spinning
                }

            } catch (IOException e) {
                System.err.println("Error reading log file: " + e.getMessage());
                sleepQuietly(1000); // Wait a bit before retrying after error
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    /**
     * Reads a line from the RandomAccessFile with a strict length limit to prevent OOM.
     */
    public static String readBoundedLine(RandomAccessFile raf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        boolean eol = false;

        while (baos.size() < MAX_LINE_LENGTH) {
            b = raf.read();
            if (b == -1) {
                if (baos.size() == 0) return null;
                break;
            }
            if (b == '\n') {
                eol = true;
                break;
            }
            if (b == '\r') {
                eol = true;
                long cur = raf.getFilePointer();
                if (raf.read() != '\n') {
                    raf.seek(cur);
                }
                break;
            }
            baos.write(b);
        }

        if (!eol && baos.size() >= MAX_LINE_LENGTH) {
            System.err.println("Critical Warning: Log line exceeds 8KB limit. Truncating and skipping to next newline to prevent OOM/DoS.");
            // Skip the rest of the line
            while ((b = raf.read()) != -1 && b != '\n' && b != '\r');
            if (b == '\r') {
                long cur = raf.getFilePointer();
                if (raf.read() != '\n') {
                    raf.seek(cur);
                }
            }
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    private void waitForFile(File file) {
        while (running && !file.exists()) {
            sleepQuietly(1000);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
