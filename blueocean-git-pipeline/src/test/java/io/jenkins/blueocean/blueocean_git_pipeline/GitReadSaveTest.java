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

import com.google.common.collect.ImmutableMap;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jenkins.plugins.git.GitSampleRepoRule;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 * Testing the git load/save backend
 * @author kzantow
 */
public class GitReadSaveTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoNoJenkinsfile = new GitSampleRepoRule();

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GitReadSaveTest() {
        this("jenkins");
    }

    private GitReadSaveTest(String blueOrganisation) {
        System.out.println("setting org root to: " + blueOrganisation);
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
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", masterPipelineScript);
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature/ux-1");
        sampleRepo.write("Jenkinsfile", branchPipelineScript);
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        sampleRepo.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");

        repoNoJenkinsfile.init();
        repoNoJenkinsfile.write("file", "nearly empty file");
        repoNoJenkinsfile.git("add", "file");
        repoNoJenkinsfile.git("commit", "--all", "--message=initilaize the repo");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoNoJenkinsfile.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");
    }

    @Test
    public void testGitCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, sampleRepo.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, repoNoJenkinsfile.getRoot(), null);
    }

    @Test
    public void testGitCacheCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, sampleRepo.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, repoNoJenkinsfile.getRoot(), null);
    }

    @Test
    public void testBareRepoReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, sampleRepo.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, repoNoJenkinsfile.getRoot(), null);
    }

    private void testGitReadWrite(final @Nonnull GitReadSaveService.ReadSaveType type, @Nonnull File remoteDir, @Nullable String startPipelineScript) throws Exception {
        GitReadSaveService.setType(type);

        String jobName = remoteDir.getName();

        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of(
                        "name", jobName,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", remoteDir.getCanonicalPath())
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
        sampleRepo.git("reset", "--hard", "refs/heads/master");
        String remoteJenkinsfile = FileUtils.readFileToString(new File(sampleRepo.getRoot(), "Jenkinsfile"));
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
