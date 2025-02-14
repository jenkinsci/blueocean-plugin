package io.jenkins.blueocean.service.embedded.util;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

/**
 * Matches case-insensitive glob string patterns
 * See https://en.wikipedia.org/wiki/Glob_(programming)
 */
public final class GlobMatcher {

    private final PathMatcher matcher;

    public GlobMatcher(String patternStr) {
        try {
            String multiDirUpperGlobPattern = patternStr.replaceAll("\\*", "**").toUpperCase();
            this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + multiDirUpperGlobPattern);
        } catch (Throwable e) {
            throw new IllegalArgumentException(String.format("bad pattern '%s'", patternStr), e);
        }
    }

    /**
     * @param input to check
     * @return matches pattern or not
     */
    public boolean matches(String input) {
        String upperInput = input.toUpperCase();
        return matcher.matches(FileSystems.getDefault().getPath(upperInput));
    }
}
