/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.service.embedded.util.UserSSHKeyManager;
import jenkins.plugins.git.GitSampleRepoRule;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.util.OsUtils;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.*;
import org.junit.runners.Parameterized;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Testing the git load/save backend
 * @author kzantow
 */
public class GitReadSaveTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule repoWithJenkinsfiles = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoNoJenkinsfile = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoForSSH = new GitSampleRepoRule();

    private final Logger logger = Logger.getLogger(getClass().getName());

    private SSHServer sshd;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GitReadSaveTest() {
        this("jenkins");
    }

    private GitReadSaveTest(String blueOrganisation) {
        GitScmTest.TestOrganizationFactoryImpl.orgRoot = blueOrganisation;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        setupScm();
    }

    private String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    private static final String masterPipelineScript = "pipeline { stage('Build 1') { steps { echo 'build' } } }";
    private static final String branchPipelineScript = "pipeline { stage('Build 2') { steps { echo 'build' } } }";
    private static final String newPipelineScript = "pipeline { stage('Build 3') { steps { echo 'build' } } }";

    private void setupScm() throws Exception {
        // create git repo
        repoWithJenkinsfiles.init();
        repoWithJenkinsfiles.write("Jenkinsfile", masterPipelineScript);
        repoWithJenkinsfiles.write("file", "initial content");
        repoWithJenkinsfiles.git("add", "Jenkinsfile");
        repoWithJenkinsfiles.git("commit", "--all", "--message=flow");

        //create feature branch
        repoWithJenkinsfiles.git("checkout", "-b", "feature/ux-1");
        repoWithJenkinsfiles.write("Jenkinsfile", branchPipelineScript);
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        repoWithJenkinsfiles.write("file", "subsequent content1");
        repoWithJenkinsfiles.git("commit", "--all", "--message=tweaked1");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoWithJenkinsfiles.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");

        repoNoJenkinsfile.init();
        repoNoJenkinsfile.write("file", "nearly empty file");
        repoNoJenkinsfile.git("add", "file");
        repoNoJenkinsfile.git("commit", "--all", "--message=initilaize the repo");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoNoJenkinsfile.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");
    }

    private void startSSH() throws Exception {
        repoForSSH.init();
        repoForSSH.write("Jenkinsfile", masterPipelineScript);
        repoForSSH.git("add", "Jenkinsfile");
        repoForSSH.git("commit", "--all", "--message=initilaize the repo");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoForSSH.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");

        // Set up an SSH server with access to a git repo
        User user = login();
        final BasicSSHUserPrivateKey key = UserSSHKeyManager.getOrCreate(user);
        final JSch jsch = new JSch();
        final KeyPair pair = KeyPair.load(jsch, key.getPrivateKey().getBytes(), null);

        File keyFile = new File(System.getProperty("TEST_SSH_SERVER_KEY_FILE", File.createTempFile("hostkey", "ser").getCanonicalPath()));
        int port = Integer.parseInt(System.getProperty("TEST_SSH_SERVER_PORT", "0"));
        boolean allowLocalUser = Boolean.getBoolean("TEST_SSH_SERVER_ALLOW_LOCAL");
        String userPublicKey = Base64.encode(pair.getPublicKeyBlob());
        sshd = new SSHServer(repoForSSH.getRoot(), keyFile, port, allowLocalUser, ImmutableMap.of("bob", userPublicKey));
        // Go, go, go
        sshd.start();
    }

    @After
    public void stopSSHServer() throws InterruptedException, IOException {
        if (sshd != null) {
            String ssh = "ssh -p " + sshd.getPort() + " bob@127.0.0.1";
            String remote = "ssh://bob@127.0.0.1:" + sshd.getPort() + "" + repoForSSH.getRoot().getCanonicalPath();
            logger.fine(ssh + " // remote: " + remote);
            sshd.stop();
        }
    }

    @Test
    public void testGitCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, repoWithJenkinsfiles, masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, repoNoJenkinsfile, null);
    }

    @Test
    public void testGitCacheCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, repoWithJenkinsfiles, masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, repoNoJenkinsfile, null);
    }

    @Test
    public void testBareRepoReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, repoWithJenkinsfiles, masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, repoNoJenkinsfile, null);
    }

    @Test
    public void bareRepoReadWriteOverSSH() throws Exception {
        if (!OsUtils.isUNIX()) {
            return; // can't really run this on windows
        }
        startSSH();
        String userHostPort = "bob@127.0.0.1:" + sshd.getPort();
        String remote = "ssh://" + userHostPort + "" + repoForSSH.getRoot().getCanonicalPath();
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, remote, repoForSSH, masterPipelineScript);
    }

    private void testGitReadWrite(final @Nonnull GitReadSaveService.ReadSaveType type, @Nonnull GitSampleRepoRule repo, @Nullable String startPipelineScript) throws Exception {
        testGitReadWrite(type, repo.getRoot().getCanonicalPath(), repo, startPipelineScript);
    }

    private void testGitReadWrite(final @Nonnull GitReadSaveService.ReadSaveType type, @Nonnull String remote, @Nonnull GitSampleRepoRule repo, @Nullable String startPipelineScript) throws Exception {
        GitReadSaveService.setType(type);

        String jobName = repo.getRoot().getName();

        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of(
                        "name", jobName,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of(
                            "uri", remote,
                            "credentialId", UserSSHKeyManager.getOrCreate(user).getId())
                )).build(Map.class);

        assertEquals(jobName, r.get("name"));

        String urlJobPrefix = "/organizations/" + getOrgName() + "/pipelines/" + jobName;

        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get(urlJobPrefix + "/scm/content/?branch=master&path=Jenkinsfile&type="+type.name())
                .build(Map.class);

        String base64Data = (String)((Map)r.get("content")).get("base64Data");

        assertEquals(startPipelineScript, base64Data == null ? null : new String(Base64.decode(base64Data), "utf-8"));

        // Update the remote
        String newBase64Data = Base64.encode(newPipelineScript.getBytes("utf-8"));
        Map<String,String> content = new HashMap<>();
        content.put("message", "Save Jenkinsfile");
        content.put("path", "Jenkinsfile");
        content.put("branch", "master");
        content.put("sourceBranch", "master");
        content.put("repo", jobName); // if no repo, this is not in an org folder
        content.put("sha", "");
        content.put("base64Data", newBase64Data);
        content.put("type", type.name());

        new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put(urlJobPrefix + "/scm/content/")
                .data(ImmutableMap.of("content", content))
                .build(Map.class);

        // Check to make sure the remote was actually updated:
        // refs udpated in our sample repo, not working tree, update it to get contents:
        repo.git("reset", "--hard", "refs/heads/master");
        String remoteJenkinsfile = FileUtils.readFileToString(new File(repo.getRoot(), "Jenkinsfile"));
        Assert.assertEquals(newPipelineScript, remoteJenkinsfile);

        // check to make sure we get the same thing from the service
        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get(urlJobPrefix + "/scm/content/?branch=master&path=Jenkinsfile&type="+type.name())
                .build(Map.class);

        base64Data = (String)((Map)r.get("content")).get("base64Data");
        Assert.assertEquals(base64Data, newBase64Data);
    }
}
