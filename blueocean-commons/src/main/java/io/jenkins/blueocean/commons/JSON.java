package io.jenkins.blueocean.commons;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/** JSON Utility */
public class JSON {

    /**
     * Sanitises string by removing any ISO control characters, tabs and line breaks
     * @param input string
     * @return sanitized string
     */
    public static String sanitizeString(@NonNull String input) {
        // replace the guava method
        // return CharMatcher.JAVA_ISO_CONTROL.and(CharMatcher.anyOf("\r\n\t")).removeFrom(input);
        if (StringUtils.isEmpty(input)) {
            return null;
        }
        StringCharacterIterator iter = new  StringCharacterIterator(input);
        StringBuilder sb = new StringBuilder(input.length());
        for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
            boolean match = Character.isISOControl(c);
            switch (c) {
                case '\r':
                case '\n':
                case '\t':
                    if (match) continue;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private JSON() {}
}
