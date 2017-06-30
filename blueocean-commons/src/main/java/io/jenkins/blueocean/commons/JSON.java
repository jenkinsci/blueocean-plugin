package io.jenkins.blueocean.commons;

import com.google.common.base.CharMatcher;

import javax.annotation.Nonnull;

/** JSON Utility */
public class JSON {

    /**
     * Sanitises string by removing any ISO control characters, tabs and line breaks
     * @param input string
     * @return sanitized string
     */
    public static String sanitizeString(@Nonnull String input) {
        return CharMatcher.JAVA_ISO_CONTROL.and(CharMatcher.anyOf("\r\n\t").negate()).removeFrom(input);
    }

    private JSON() {}
}
