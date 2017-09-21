package io.blueocean.ath;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.openqa.selenium.logging.LogEntry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a Selenium LogEntry to log4j
 * @author cliffmeyers
 */
class LogEntryLogger {
    private static Logger logger = Logger.getLogger(LogEntryLogger.class);

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

    static void recordLogEntry(LogEntry entry) {
        if (!logger.isInfoEnabled()) {
            return;
        }

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
}
