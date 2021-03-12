package io.blueocean.ath;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes a Selenium LogEntry to slf4j
 * @author cliffmeyers
 */
class LogEntryLogger {
    private static final Logger logger = LoggerFactory.getLogger(LogEntryLogger.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

    static void recordLogEntries(List<LogEntry> entries) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        if (entries.size() == 0) {
            logger.info("nothing written to browser console");
            return;
        }

        List<LogEntry> usefulEntries = entries.stream()
            .filter(entry -> !isSuperfluousLogEntry(entry))
            .collect(Collectors.toList());

        if (usefulEntries.iterator().hasNext()) {
            logger.info("browser console output below:");
            usefulEntries.stream().forEach(LogEntryLogger::recordLogEntry);
        } else {
            logger.info(String.format("nothing useful written to browser console; %s entries were hidden", entries.size()));
        }
    }

    private static void recordLogEntry(LogEntry entry) {
        String time;
        String level;
        String text;

        try {
            // handle messages written by @jenkins-cd/js-logging
            Map<String, Object> messageJson = jsonMapper.readValue(entry.getMessage(), typeRef);
            Map<String, Object> message = (Map<String, Object>) messageJson.get("message");
            time = String.valueOf(message.get("timestamp"));
            level = String.valueOf(message.get("level"));
            text = String.valueOf(message.get("text"));
        } catch (IOException e) {
            // handle messages written natively by console.error|warn|log|debug
            time = String.valueOf(entry.getTimestamp());
            level = String.valueOf(entry.getLevel());
            text = entry.getMessage();
        }

        logger.info(String.format("%s - %s - %s", time, level, text));
    }

    // special handling to suppress some repetitive logging messages that are not helpful
    private static final String MESSAGE_JS_LOGGING = "@jenkins-cd/logging is explained";
    private static final String MESSAGE_CHROME_CONSOLE = "Chrome displays console errors";
    private static final String MESSAGE_PASSWORD_INSECURE = "page includes a password or credit card input";

    private static boolean isSuperfluousLogEntry(LogEntry entry) {
        String message = entry.getMessage();
        return message.contains(MESSAGE_JS_LOGGING) || message.contains(MESSAGE_CHROME_CONSOLE) ||
            message.contains(MESSAGE_PASSWORD_INSECURE);
    }
}
