package io.jenkins.blueocean.blueocean_git_pipeline;

import io.jenkins.blueocean.commons.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests fot GitScm that don't run via network
 */
public class GitScmNonNetworkTest {

    @Test
    public void shouldMakeCredentialIdForHttp() {
        String result = GitScm.makeCredentialId("http://example.org/git/foo.git");
        Assert.assertEquals("git:" + DigestUtils.sha256Hex("http://example.org"), result);
    }

    @Test
    public void shouldMakeCredentialIdForHttps() {
        String result = GitScm.makeCredentialId("https://example.org/git/foo.git");
        Assert.assertEquals("git:" + DigestUtils.sha256Hex("https://example.org"), result);
    }

    @Test
    public void shouldNotMakeCredentialIdForSsh() {
        String result = GitScm.makeCredentialId("ssh://example.org/git/foo.git");
        Assert.assertNull(result);
    }

    @Test
    public void shouldNotMakeCredentialIdForGit() {
        String result = GitScm.makeCredentialId("git://example.org/git/foo.git");
        Assert.assertNull(result);
    }

    @Test
    public void shouldNotMakeCredentialIdForBadUrls() {
        Assert.assertNull(GitScm.makeCredentialId("this is not a url worth mentioning"));
        Assert.assertNull(GitScm.makeCredentialId("git://"));
        Assert.assertNull(GitScm.makeCredentialId("http://"));
        Assert.assertNull(GitScm.makeCredentialId(""));
        Assert.assertNull(GitScm.makeCredentialId(null));
    }
}
