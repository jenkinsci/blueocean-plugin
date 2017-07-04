package io.jenkins.blueocean.commons;

import org.junit.Assert;
import org.junit.Test;

public class JSONTest {
    @Test
    public void sanitize() throws Exception {
        Assert.assertEquals("hello vivek :)", JSON.sanitizeString("hello\r vivek\n\t :)"));
    }
}
