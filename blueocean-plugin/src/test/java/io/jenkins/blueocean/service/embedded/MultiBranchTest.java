package io.jenkins.blueocean.service.embedded;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.service.embedded.scm.GitSampleRepoRule;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsArrayContainingInAnyOrder;
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
            .body("[0].weatherScore", Matchers.is(f.getBuildHealth().getScore()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(mp.getName()))
            .body("[1].displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("[1].numberOfFailingBranches", Matchers.equalTo(0))
            .body("[1].numberOfSuccessfulBranches", Matchers.equalTo(0))
            .body("[1].totalNumberOfBranches", Matchers.equalTo(3))
            .body("[1].weatherScore", Matchers.is(mp.getBranch("master").getBuildHealth().getScore()));
    }


    @Test
    public void getMultiBranchPipelinesWithNonMasterBranch() throws Exception {
        sampleRepo.git("branch","-D", "master");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");

        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        given().log().all().get("/organizations/jenkins/pipelines/").then().log().all().statusCode(200);
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

        Response response = given().log().all().get("/organizations/jenkins/pipelines/p/");
        ValidatableResponse validatableResponse = response.then().log().all().statusCode(200);
            validatableResponse.body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo(mp.getName()))
            .body("displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("numberOfFailingBranches", Matchers.equalTo(0))
            .body("numberOfSuccessfulBranches", Matchers.equalTo(0))
            .body("totalNumberOfBranches", Matchers.equalTo(3));

        List<String> names = with(response.asString()).get("branchNames");
        IsArrayContainingInAnyOrder.arrayContainingInAnyOrder(names, branches);


        Response r = given().log().all().get("/organizations/jenkins/pipelines/p/branches");
        r.then().log().all().statusCode(200);

        String body = r.asString();
        List<String> branchNames = with(body).get("name");
        List<Integer> weather = with(body).get("weatherScore");
        for(String n:branches){
            assertTrue(branchNames.contains(n));
        }

        for(int s:weather){
            assertEquals(100, s);
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
        r.then().log().all().statusCode(200)
            .body("latestRun[0].pipeline", Matchers.anyOf(Matchers.equalTo("feature1"),
                Matchers.equalTo("feature2"), Matchers.equalTo("master")));

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
                .body("pullRequest", Matchers.nullValue())
                .body("startTime", Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
            i++;
        }

        given().log().all().get("/organizations/jenkins/pipelines/p/").then().log().all().statusCode(200)
            .body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo(mp.getName()))
            .body("displayName", Matchers.equalTo(mp.getDisplayName()))
            .body("numberOfFailingBranches", Matchers.equalTo(0))
            .body("numberOfSuccessfulBranches", Matchers.equalTo(3))
            .body("totalNumberOfBranches", Matchers.equalTo(3));


        sampleRepo.git("checkout","master");
        sampleRepo.write("file", "subsequent content11");
        sampleRepo.git("commit", "--all", "--message=tweaked11");

        p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b4 = p.getLastBuild();
        assertEquals(2, b4.getNumber());

        Response run = given().log().all().get("/organizations/jenkins/pipelines/p/branches/master/runs/"+b4.getId());
        run.then().log().all().statusCode(200)
            .statusCode(200)
            .body("id", Matchers.equalTo(b4.getId()))
            .body("pipeline", Matchers.equalTo(b4.getParent().getName()))
            .body("organization", Matchers.equalTo("jenkins"))
            .body("startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b4.getStartTimeInMillis()))));;


    }

    @Test
    public void getMultiBranchPipelineActivityRuns() throws Exception {
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

        WorkflowRun firstStart = b1;

        if(b2.getStartTimeInMillis() < firstStart.getStartTimeInMillis()) {
            firstStart = b2;
        }
        if(b3.getStartTimeInMillis() < firstStart.getStartTimeInMillis()) {
            firstStart = b3;
        }

        BuildData d = firstStart.getAction(BuildData.class);

        String commitId = "";
        if(d != null) {
            commitId = d.getLastBuiltRevision().getSha1String();
        }
        Response r = given().log().all().get("/organizations/jenkins/pipelines/p/runs");
        r.then().log().all().statusCode(200)
            .body("size()",Matchers.is(3))
            .body("pipeline[0]",Matchers.equalTo(firstStart.getParent().getName()))
            .body("id[0]", Matchers.equalTo(firstStart.getNumber()+""))
            .body("commitId[0]", Matchers.equalTo(commitId));


    }

    @Test
    public void getMultiBranchPipelineRunChangeSets() throws Exception {
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

        sampleRepo.git("checkout","master");
        sampleRepo.write("file", "subsequent content11");
        sampleRepo.git("commit", "--all", "--message=tweaked11");

        p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b4 = p.getLastBuild();
        assertEquals(2, b4.getNumber());

        ChangeLogSet.Entry changeLog = b4.getChangeSets().get(0).iterator().next();

        Response run = given().log().all().get("/organizations/jenkins/pipelines/p/branches/master/runs/"+b4.getId()+"/");
        run.then().log().all().statusCode(200)
            .statusCode(200)
            .body("id", Matchers.equalTo(b4.getId()))
            .body("pipeline", Matchers.equalTo(b4.getParent().getName()))
            .body("organization", Matchers.equalTo("jenkins"))
            .body("startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b4.getStartTimeInMillis()))))
            .body("changeSet[0].author.id", Matchers.equalTo(changeLog.getAuthor().getId()))
            .body("changeSet[0].author.fullName", Matchers.equalTo(changeLog.getAuthor().getFullName()))
            .body("changeSet[0].commitId", Matchers.equalTo(changeLog.getCommitId()));
    }



    @Test
    public void getMultiBranchPipelineRunStages() throws Exception {
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


        Response r = given().log().all().get("/organizations/jenkins/pipelines/p/branches/master/runs/1/nodes");
        r.then().log().all().statusCode(200);


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
        sampleRepo.git("checkout", "-b", "feature1");
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

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature2");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
            "   stage ('Build'); " +
            "   echo ('Building'); " +
            "   stage ('Test'); " +
            "   echo ('Testing'); " +
            "   stage ('Deploy'); " +
            "   echo ('Deploying'); " +
            "}");
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
