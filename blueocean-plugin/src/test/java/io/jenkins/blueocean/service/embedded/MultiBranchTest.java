package io.jenkins.blueocean.service.embedded;

import com.jayway.restassured.response.Response;
import hudson.model.FreeStyleProject;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.service.embedded.scm.GitSampleRepoRule;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.path.json.JsonPath.with;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class MultiBranchTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule public JenkinsRule j = new JenkinsRule();
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    private final String[] branches={"master", "feature1", "feature2"};

    @Before
    public void setup() throws Exception {
        baseURI = j.jenkins.getRootUrl()+"blue/rest";
        enableLoggingOfRequestAndResponseIfValidationFails();
        setupScm();
    }

    @Test
    public void getMultiBranchPipelines() throws IOException, ExecutionException, InterruptedException {
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        FreeStyleProject f = j.jenkins.createProject(FreeStyleProject.class, "f");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        given().log().all().get("/organizations/jenkins/pipelines/").then().log().all().statusCode(200)
            .body("size()", Matchers.is(2))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(f.getName()))
            .body("[0].displayName", Matchers.equalTo(f.getDisplayName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(mp.getName()))
            .body("[1].displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("[1].numberOfFailingBranches", Matchers.equalTo(0))
            .body("[1].numberOfSuccessfulBranches", Matchers.equalTo(0))
            .body("[1].totalNumberOfBranches", Matchers.equalTo(3));
    }



    @Test
    public void getMultiBranchPipeline() throws IOException, ExecutionException, InterruptedException {
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        given().log().all().get("/organizations/jenkins/pipelines/p/").then().log().all().statusCode(200)
            .body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo(mp.getName()))
            .body("displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("numberOfFailingBranches", Matchers.equalTo(0))
            .body("numberOfSuccessfulBranches", Matchers.equalTo(0))
            .body("totalNumberOfBranches", Matchers.equalTo(3));


        Response r = given().log().all().get("/organizations/jenkins/pipelines/p/branches");
        r.then().log().all().statusCode(200);

        String body = r.asString();
        List<String> branchNames = with(body).get("name");
        for(String n:branches){
            assertTrue(branchNames.contains(n));
        }

    }

    @Test
    public void getMultiBranchPipelineRuns() throws Exception {
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        assertEquals(1, b1.getNumber());
        assertEquals(3, mp.getItems().size());

        //execute feature 1 branch build
        p = scheduleAndFindBranchProject(mp, "feature1");
        j.waitUntilNoActivity();
        WorkflowRun b2 = p.getLastBuild();
        assertEquals(1, b2.getNumber());


        //execute feature 2 branch build
        p = scheduleAndFindBranchProject(mp, "feature2");
        j.waitUntilNoActivity();
        WorkflowRun b3 = p.getLastBuild();
        assertEquals(1, b3.getNumber());

        Response r = given().log().all().get("/organizations/jenkins/pipelines/p/branches");
        r.then().log().all().statusCode(200);

        String body = r.asString();
        List<String> branchNames = with(body).get("name");
        for(String n:branches){
            assertTrue(branchNames.contains(n));
        }

        WorkflowRun[] runs = {b1,b2,b3};

        int i = 0;
        for(String n:branches){
            WorkflowRun b = runs[i];
            Response run = given().log().all().get("/organizations/jenkins/pipelines/p/branches/"+n+"/runs/"+b.getId());
            run.then().log().all().statusCode(200)
                .statusCode(200)
                .body("id", Matchers.equalTo(b.getId()))
                .body("pipeline", Matchers.equalTo(b.getParent().getName()))
                .body("organization", Matchers.equalTo("jenkins"))
                .body("startTime", Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));;
            i++;
        }

        given().log().all().get("/organizations/jenkins/pipelines/p/").then().log().all().statusCode(200)
            .body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo(mp.getName()))
            .body("displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("numberOfFailingBranches", Matchers.equalTo(0))
            .body("numberOfSuccessfulBranches", Matchers.equalTo(3))
            .body("totalNumberOfBranches", Matchers.equalTo(3));



    }


    private void setupScm() throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; node {checkout scm; echo readFile('file')}");
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature1");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; node {checkout scm; echo readFile('file').toUpperCase()}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature2");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; node {checkout scm; echo readFile('file').toUpperCase()}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content2");
        sampleRepo.git("commit", "--all", "--message=tweaked2");
    }


    private WorkflowJob scheduleAndFindBranchProject(WorkflowMultiBranchProject mp,  String name) throws Exception {
        mp.scheduleBuild2(0).getFuture().get();
        return findBranchProject(mp, name);
    }

    private WorkflowJob findBranchProject(WorkflowMultiBranchProject mp,  String name) throws Exception {
        WorkflowJob p = mp.getItem(name);
        if (p == null) {
            mp.getIndexing().writeWholeLogTo(System.out);
            fail(name + " project not found");
        }
        return p;
    }

}
