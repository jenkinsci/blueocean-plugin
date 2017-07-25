package io.jenkins.blueocean.commons;

import org.junit.Assert;
import org.junit.Test;

public class JSONTest {
    @Test
    public void sanitize() throws Exception {
        Assert.assertEquals("hello vivek :)", JSON.sanitizeString("hello\r vivek\n\t :)"));
    }

    @Test
    public void escape() throws Exception {
        Assert.assertEquals("\\\\033[32m some text \\\\033[0m", JSON.escape("\\033[32m some text \\033[0m"));
    }
}
