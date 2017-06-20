package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.ImmutableMap;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Meredith
 */
public class BranchContainerImplTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }

    @Test
    public void testBranchOrdering() throws Exception {
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

        String token = getJwtToken(j.jenkins, "alice", "alice");

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/p/branches/feature2/favorite")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/p/branches/feature4/favorite")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        List l = request().get("/organizations/jenkins/pipelines/p/branches/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(4,l.size());
        Map o = (Map)l.get(1);
        Map o2 = (Map)l.get(0);
        Assert.assertTrue(o.get("name").equals("feature2") || o.get("name").equals("feature4"));

        WorkflowJob j1 = findBranchProject(mp, (String)o.get("name"));
        j.waitForCompletion(j1.getLastBuild());
        j.waitForCompletion(j1.scheduleBuild2(0).waitForStart());

        WorkflowJob j2 = findBranchProject(mp, (String)o2.get("name"));
        j.waitForCompletion(j2.getLastBuild());

        List l2 = request().get("/organizations/jenkins/pipelines/p/branches/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(4,l.size());
        Map o1 = (Map)l2.get(0);
        Map o3 = (Map)l2.get(1);

        Assert.assertEquals(o2.get("name"), o1.get("name"));
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

        sampleRepo.git("checkout", "-b", "feature4");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
            "   stage ('Build'); " +
            "   echo ('Building'); " +
            "   stage ('Test'); " +
            "   echo ('Testing'); " +
            "   stage ('Deploy'); " +
            "   echo ('Deploying'); " +
            "}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content234");
        sampleRepo.git("commit", "--all", "--message=tweaked4");
        sampleRepo.git("checkout", "master");
    }

}
