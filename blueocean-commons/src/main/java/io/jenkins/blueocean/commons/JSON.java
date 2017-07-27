package io.jenkins.blueocean.commons;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nonnull;

/** JSON Utility */
public class JSON {

    /**
     * Sanitises string by removing any ISO control characters, tabs and line breaks
     * @param input string
     * @return sanitized string
     */
    public static String sanitizeString(@Nonnull String input) {
        return CharMatcher.JAVA_ISO_CONTROL.and(CharMatcher.anyOf("\r\n\t")).removeFrom(input);
    }

    /**
     * Escapes any characters in the given string that are not safe for ECMA script
     * @param input string
     * @return escaped string
     */
    public static String escape(String input) {
        return StringEscapeUtils.escapeEcmaScript(input);
    }

    private JSON() {}
}
