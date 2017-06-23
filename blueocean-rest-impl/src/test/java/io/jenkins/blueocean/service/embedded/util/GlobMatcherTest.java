package io.jenkins.blueocean.service.embedded.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GlobMatcherTest {
    @Test
    public void testMatchesOnSingleCharacter() throws Exception {
        GlobMatcher matcher = new GlobMatcher("A*");
        assertTrue(matcher.matches("AABBCC"));
        assertFalse(matcher.matches("FFFF"));
    }

    @Test
    public void testMatchesOnSingleCharacterLowerCase() throws Exception {
        GlobMatcher matcher = new GlobMatcher("a*");
        assertTrue(matcher.matches("AABBCC"));
        assertFalse(matcher.matches("FFFF"));
    }

    @Test
    public void testMatchesOnExactString() throws Exception {
        GlobMatcher matcher = new GlobMatcher("AABBCC");
        assertTrue(matcher.matches("AABBCC"));
        assertFalse(matcher.matches("FFFF"));
    }

    @Test
    public void testMatchesOnExactStringLowerCase() throws Exception {
        GlobMatcher matcher = new GlobMatcher("aabbcc");
        assertTrue(matcher.matches("AABBCC"));
        assertFalse(matcher.matches("FFFF"));
    }

    @Test
    public void testMatchesOnPath() throws Exception {
        GlobMatcher matcher = new GlobMatcher("A*/F*/P*");
        assertTrue(matcher.matches("A Folder/Folder/Pipeline"));
        assertFalse(matcher.matches("A Folder/Sub/Pipeline"));
    }

    @Test
    public void testMatchesOnPathLowerCase() throws Exception {
        GlobMatcher matcher = new GlobMatcher("a*/f*/p*");
        assertTrue(matcher.matches("A Folder/Folder/Pipeline"));
        assertFalse(matcher.matches("A Folder/Sub/Pipeline"));
    }
}
