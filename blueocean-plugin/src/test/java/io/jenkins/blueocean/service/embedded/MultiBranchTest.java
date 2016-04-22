package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.service.embedded.scm.GitSampleRepoRule;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.hamcrest.collection.IsArrayContainingInAnyOrder;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class MultiBranchTest extends BaseTest{

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();


    private final String[] branches={"master", "feature1", "feature2"};

    @Before
    public void setup() throws Exception{
        super.setup();
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

        List<Map> resp = get("/organizations/jenkins/pipelines/", List.class);
        Assert.assertEquals(2, resp.size());
        validatePipeline(f, resp.get(0));
        validateMultiBranchPipeline(mp, resp.get(1), 3, 0, 0);
        Assert.assertEquals(mp.getBranch("master").getBuildHealth().getScore(), resp.get(0).get("weatherScore"));
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

        List<Map> resp = get("/organizations/jenkins/pipelines/", List.class);
        Assert.assertEquals(1, resp.size());
        validateMultiBranchPipeline(mp, resp.get(0), 2, 0, 0);
        Assert.assertNull(mp.getBranch("master"));
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


        Map resp = get("/organizations/jenkins/pipelines/p/");
        validateMultiBranchPipeline(mp,resp,3,0,0);

        List<String> names = (List<String>) resp.get("branchNames");

        IsArrayContainingInAnyOrder.arrayContainingInAnyOrder(names, branches);

        List<Map> br = get("/organizations/jenkins/pipelines/p/branches", List.class);

        List<String> branchNames = new ArrayList<>();
        List<Integer> weather = new ArrayList<>();
        for(Map b: br){
            branchNames.add((String) b.get("name"));
            weather.add((int) b.get("weatherScore"));
        }

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

        List<Map> br = get("/organizations/jenkins/pipelines/p/branches", List.class);

        List<String> branchNames = new ArrayList<>();
        List<Integer> weather = new ArrayList<>();
        for(Map b: br){
            branchNames.add((String) b.get("name"));
            weather.add((int) b.get("weatherScore"));
        }
        Assert.assertTrue(branchNames.contains(((Map)(br.get(0).get("latestRun"))).get("pipeline")));

        for(String n:branches){
            assertTrue(branchNames.contains(n));
        }

        WorkflowRun[] runs = {b1,b2,b3};

        int i = 0;
        for(String n:branches){
            WorkflowRun b = runs[i];

            Map run = get("/organizations/jenkins/pipelines/p/branches/"+n+"/runs/"+b.getId());
            validateRun(b,run);
            i++;
        }

        Map pr = get("/organizations/jenkins/pipelines/p/");
        validateMultiBranchPipeline(mp, pr, 3,3,0);

        sampleRepo.git("checkout","master");
        sampleRepo.write("file", "subsequent content11");
        sampleRepo.git("commit", "--all", "--message=tweaked11");

        p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b4 = p.getLastBuild();
        assertEquals(2, b4.getNumber());

        Map run = get("/organizations/jenkins/pipelines/p/branches/master/runs/"+b4.getId());
        validateRun(b4, run);
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

        List<Map> resp = get("/organizations/jenkins/pipelines/p/runs", List.class);
        Assert.assertEquals(3, resp.size());

        validateRun(firstStart,resp.get(0));
        Assert.assertEquals(commitId, resp.get(0).get("commitId"));
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


        Map run = get("/organizations/jenkins/pipelines/p/branches/master/runs/"+b4.getId()+"/");
        validateRun(b4, run);
        List<Map> changetSet = (List<Map>) run.get("changeSet");
        Map c = changetSet.get(0);

        Assert.assertEquals(changeLog.getCommitId(), c.get("commitId"));
        Map a = (Map) c.get("author");
        Assert.assertEquals(changeLog.getAuthor().getId(), a.get("id"));
        Assert.assertEquals(changeLog.getAuthor().getFullName(), a.get("fullName"));
    }

    @Test
    public void createUserFavouriteMultibranchTopLevelTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/p/favorite")
            .auth("alice", "alice")
            .data(ImmutableMap.of("favorite", true))
            .build(String.class);

        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("alice","alice")
            .build(List.class);

        Assert.assertEquals(l.size(), 1);
        Assert.assertEquals(((Map)l.get(0)).get("pipeline"),"/organizations/jenkins/pipelines/p/branches/master");

        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("bob","bob")
            .status(403)
            .build(String.class);

    }


    @Test
    public void createUserFavouriteMultibranchBranchTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/p/branches/feature1/favorite")
            .auth("alice", "alice")
            .data(ImmutableMap.of("favorite", true))
            .build(String.class);

        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("alice","alice")
            .build(List.class);

        Assert.assertEquals(l.size(), 1);
        Assert.assertEquals(((Map)l.get(0)).get("pipeline"),"/organizations/jenkins/pipelines/p/branches/feature1");

        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("bob","bob")
            .status(403)
            .build(String.class);

    }

    /*
     * FIXME: @vivek, @ivan. This test is flaking out on ci often.
     *
     * We don't think it is timing, but we do see errors like: java.io.IOException: cannot find current thread
     *  May be a workflow bug. This was introduced around the revision: 9df08944af1af260ef5f3ea902b7ca69aa53366a
     * ERROR in output.txt for surefire for this suite:
     * WARNING: failed to print message to dead CpsStepContext[3]:Owner[p/master/1:p/master #1]
java.io.IOException: cannot find current thread
	at org.jenkinsci.plugins.workflow.cps.CpsStepContext.doGet(CpsStepContext.java:287)
	at org.jenkinsci.plugins.workflow.support.DefaultStepContext.get(DefaultStepContext.java:71)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution.println(StageStepExecution.java:230)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution.access$100(StageStepExecution.java:36)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution$Stage.unblock(StageStepExecution.java:296)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution.exit(StageStepExecution.java:188)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution.access$200(StageStepExecution.java:36)
	at org.jenkinsci.plugins.workflow.support.steps.StageStepExecution$Listener.onCompleted(StageStepExecution.java:310)
	at hudson.model.listeners.RunListener.fireCompleted(RunListener.java:201)
	at org.jenkinsci.plugins.workflow.job.WorkflowRun.finish(WorkflowRun.java:521)
	at org.jenkinsci.plugins.workflow.job.WorkflowRun.access$1100(WorkflowRun.java:111)
	at org.jenkinsci.plugins.workflow.job.WorkflowRun$GraphL.onNewHead(WorkflowRun.java:777)
	at org.jenkinsci.plugins.workflow.cps.CpsFlowExecution.notifyListeners(CpsFlowExecution.java:843)
	at org.jenkinsci.plugins.workflow.cps.CpsThreadGroup$4.run(CpsThreadGroup.java:340)
	at org.jenkinsci.plugins.workflow.cps.CpsVmExecutorService$1.run(CpsVmExecutorService.java:32)
	at hudson.remoting.SingleLaneExecutorService$1.run(SingleLaneExecutorService.java:112)
	at jenkins.util.ContextResettingExecutorService$1.run(ContextResettingExecutorService.java:28)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
     *
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

        j.waitForCompletion(b1);

        List<Map> nodes = get("/organizations/jenkins/pipelines/p/branches/master/runs/1/nodes", List.class);

        Assert.assertEquals(3, nodes.size());
    }
    */


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
