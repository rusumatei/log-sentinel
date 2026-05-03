package ubb.bmad.logsentinel.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LogTailerTest {

    @TempDir
    Path tempDir;

    @Test
    void testTailerDetectsNewLines() throws Exception {
        File logFile = tempDir.resolve("test.log").toFile();
        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write("Existing Line 1\n");
        }

        List<String> detectedLines = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        LogTailer tailer = new LogTailer(false); // Process existing lines
        
        Thread tailerThread = new Thread(() -> {
            tailer.startTailing(logFile.getAbsolutePath(), line -> {
                detectedLines.add(line);
                if (detectedLines.size() == 2) {
                    latch.countDown();
                }
            });
        });
        tailerThread.start();

        // Add a new line while tailing
        Thread.sleep(600); // Wait for tailer to start
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write("New Line 2\n");
            fw.flush();
        }

        boolean reached = latch.await(3, TimeUnit.SECONDS);
        tailer.stop();
        tailerThread.join();

        assertTrue(reached, "Tailer should have detected 2 lines");
        assertEquals("Existing Line 1", detectedLines.get(0));
        assertEquals("New Line 2", detectedLines.get(1));
    }

    @Test
    void testJumpToEnd() throws Exception {
        File logFile = tempDir.resolve("jump.log").toFile();
        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write("Old Line\n");
        }

        List<String> detectedLines = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        LogTailer tailer = new LogTailer(true); // JUMP TO END
        
        Thread tailerThread = new Thread(() -> {
            tailer.startTailing(logFile.getAbsolutePath(), line -> {
                detectedLines.add(line);
                latch.countDown();
            });
        });
        tailerThread.start();

        Thread.sleep(600);
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write("Fresh Line\n");
            fw.flush();
        }

        boolean reached = latch.await(3, TimeUnit.SECONDS);
        tailer.stop();
        tailerThread.join();

        assertTrue(reached);
        assertEquals(1, detectedLines.size());
        assertEquals("Fresh Line", detectedLines.get(0));
    }

    @Test
    void testTruncationDetection() throws Exception {
        File logFile = tempDir.resolve("rotate.log").toFile();
        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write("Long content that will be truncated\n");
        }

        List<String> detectedLines = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        LogTailer tailer = new LogTailer(false);
        
        Thread tailerThread = new Thread(() -> {
            tailer.startTailing(logFile.getAbsolutePath(), line -> {
                detectedLines.add(line);
                if (detectedLines.contains("After Truncation")) {
                    latch.countDown();
                }
            });
        });
        tailerThread.start();

        Thread.sleep(600);
        
        // Truncate file (simulate rotation)
        try (FileWriter fw = new FileWriter(logFile, false)) { // Overwrite
            fw.write("After Truncation\n");
            fw.flush();
        }

        boolean reached = latch.await(3, TimeUnit.SECONDS);
        tailer.stop();
        tailerThread.join();

        assertTrue(reached, "Tailer should have reset pointer and read new content after truncation");
        assertTrue(detectedLines.contains("After Truncation"));
    }

    @Test
    void testBoundedLineReading() throws Exception {
        File logFile = tempDir.resolve("bounded.log").toFile();
        
        // 1. Normal line
        // 2. Line exactly at limit (8192)
        // 3. Line exceeding limit (8192 + extra)
        // 4. Next normal line
        
        String normalLine = "Normal Line";
        String limitLine = "A".repeat(8192);
        String exceedingLine = "B".repeat(9000);
        String finalLine = "After Malicious Line";

        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write(normalLine + "\n");
            fw.write(limitLine + "\n");
            fw.write(exceedingLine + "\n");
            fw.write(finalLine + "\n");
        }

        List<String> detectedLines = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(4);
        
        LogTailer tailer = new LogTailer(false);
        
        Thread tailerThread = new Thread(() -> {
            tailer.startTailing(logFile.getAbsolutePath(), line -> {
                detectedLines.add(line);
                latch.countDown();
            });
        });
        tailerThread.start();

        boolean reached = latch.await(3, TimeUnit.SECONDS);
        tailer.stop();
        tailerThread.join();

        assertTrue(reached, "Should have processed 4 log entries");
        assertEquals(normalLine, detectedLines.get(0));
        assertEquals(limitLine, detectedLines.get(1));
        // exceedingLine should be truncated to 8KB
        assertEquals("B".repeat(8192), detectedLines.get(2));
        assertEquals(finalLine, detectedLines.get(3));
    }
}
