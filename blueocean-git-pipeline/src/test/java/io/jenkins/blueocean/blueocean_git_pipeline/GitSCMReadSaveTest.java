/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import static io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest.getJwtToken;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.ModifiableTopLevelItemGroup;
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
 *
 * @author kzantow
 */
public class GitSCMReadSaveTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoNoJenkinsfile = new GitSampleRepoRule();

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GitSCMReadSaveTest() {
        this("jenkins");
    }
    
    private GitSCMReadSaveTest(String blueOrganisation) {
        System.out.println("setting org root to: " + blueOrganisation);
        GitScmTest.TestOrganizationFactoryImpl.orgRoot = blueOrganisation;
    }
    
    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }
    private String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    private ModifiableTopLevelItemGroup getOrgRoot() {
        return OrganizationFactory.getItemGroup(getOrgName());
    }

    private void setupScm() throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "stage 'build'\n "+"node {echo 'Building'}\n"+
                "stage 'test'\nnode { echo 'Testing'}\n"+
                "stage 'deploy'\nnode { echo 'Deploying'}\n"
        );
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature/ux-1");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
                "   stage ('Build'); " +
                "   echo ('Building'); " +
                "   stage ('Test'); " +
                "   echo ('Testing'); " +
                "   stage ('Deploy'); " +
                "   echo ('Deploying'); " +
                "}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");
        
        System.out.println("SampleRepo: " + sampleRepo.fileUrl());
        
        repoNoJenkinsfile.init();
        repoNoJenkinsfile.write("file", "nearly empty file");
        repoNoJenkinsfile.git("add", "file");
        repoNoJenkinsfile.git("commit", "--all", "--message=initilaize the repo");
    }

    @Test
    public void testGitReadWrite() throws UnirestException, IOException, Exception {
        testGitReadWrite(true);
    }

    @Test
    public void testGitSCMReadWrite() throws UnirestException, IOException, Exception {
        testGitReadWrite(false);
    }
    
    private void testGitReadWrite(boolean useGitReadSaveSerice) throws UnirestException, IOException, Exception {
        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of(
                        "name", "sampleRepo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.getRoot().getCanonicalPath(),
                        "useGitReadSaveSerice", useGitReadSaveSerice ? true : false)
                )).build(Map.class);

        assertEquals("sampleRepo", r.get("name"));

        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/" + getOrgName() + "/pipelines/sampleRepo/scm/content/?branch=master&path=Jenkinsfile")
                .build(Map.class);

        String base64Data = (String)((Map)r.get("content")).get("base64Data");

        Assert.assertNotNull(base64Data);
        
        String newBase64Data = Base64.encode("pipeline { stage('build') { steps { echo 'build' } } }".getBytes("utf-8"));
        Map<String,String> content = new HashMap<>();
        content.put("message", "Save Jenkinsfile");
        content.put("path", "Jenkinsfile");
        content.put("branch", "master");
        content.put("sourceBranch", "master");
        content.put("repo", "sampleRepo"); // if no repo, this is not in an org folder
        content.put("sha", "");
        content.put("base64Data", newBase64Data);
        
        new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/" + getOrgName() + "/pipelines/sampleRepo/scm/content/")
                .data(ImmutableMap.of("content", content))
                .build(Map.class);

        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/" + getOrgName() + "/pipelines/sampleRepo/scm/content/?branch=master&path=Jenkinsfile")
                .build(Map.class);

        base64Data = (String)((Map)r.get("content")).get("base64Data");
        
        sampleRepo.git("reset", "--hard", "refs/heads/master");
        String remoteJenkinsfile = FileUtils.readFileToString(new File(sampleRepo.getRoot(), "Jenkinsfile"));

        Assert.assertEquals(base64Data, newBase64Data);
        
        Assert.assertEquals(new String(Base64.decode(newBase64Data), "utf-8"), remoteJenkinsfile);
    }
}
