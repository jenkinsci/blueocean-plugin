package io.jenkins.blueocean.blueocean_git_pipeline;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests fot GitScm that don't run via network
 */
public class GitScmTest2 {

    @Test
    public void shouldMakeCredentialIdForHttp() {
        String result = GitScm.makeCredentialId("http://example.org/git/foo.git");
        Assert.assertEquals("git:http://example.org/git/foo.git", result);
    }

    @Test
    public void shouldMakeCredentialIdForHttps() {
        String result = GitScm.makeCredentialId("https://example.org/git/foo.git");
        Assert.assertEquals("git:https://example.org/git/foo.git", result);
    }

    @Test
    public void shouldNotMakeCredentialIdForSsh() {
        String result = GitScm.makeCredentialId("ssh://example.org/git/foo.git");
        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldNotMakeCredentialIdForGit() {
        String result = GitScm.makeCredentialId("git://example.org/git/foo.git");
        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldNotMakeCredentialIdForBadUrls() {
        Assert.assertEquals(null, GitScm.makeCredentialId("this is not a url worth mentioning"));
        Assert.assertEquals(null, GitScm.makeCredentialId("git://"));
        Assert.assertEquals(null, GitScm.makeCredentialId("http://"));
        Assert.assertEquals(null, GitScm.makeCredentialId(""));
        Assert.assertEquals(null, GitScm.makeCredentialId(null));
    }
}
