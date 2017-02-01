package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GitScmTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }

    @Test
    public void simpleOrgTest() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);

        Assert.assertEquals("demo", resp.get("name"));
    }

    @Test
    public void simpleOrgShouldFailOnValidation1(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of(
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 400);

        Assert.assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        Assert.assertEquals(errors.get(0).get("field"), "name");
        Assert.assertEquals(errors.get(0).get("code"), "MISSING");
        Assert.assertEquals(errors.get(1).get("field"), "$class");
        Assert.assertEquals(errors.get(1).get("code"), "MISSING");
    }

    @Test
    public void simpleOrgShouldFailOnValidation2(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest"
                ), 400);

        Assert.assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        Assert.assertEquals(errors.get(0).get("field"), "scmConfig");
        Assert.assertEquals(errors.get(0).get("code"), "MISSING");
    }

    @Test
    public void simpleOrgShouldFailOnValidation3() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of()))
                .build(Map.class);

        Assert.assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        Assert.assertEquals(errors.get(0).get("field"), "scmConfig.uri");
        Assert.assertEquals(errors.get(0).get("code"), "MISSING");
    }


    @Test
    public void simpleOrgShouldFailOnValidation4() throws IOException, UnirestException {
        login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);


        Assert.assertEquals("demo", resp.get("name"));

        resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);

        List<Map> errors = (List<Map>) resp.get("errors");

        Assert.assertEquals(errors.get(0).get("field"), "name");
        Assert.assertEquals(errors.get(0).get("code"), "ALREADY_EXISTS");

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
    }
}
