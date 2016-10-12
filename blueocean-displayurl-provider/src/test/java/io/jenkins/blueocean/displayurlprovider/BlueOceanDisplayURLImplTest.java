package io.jenkins.blueocean.displayurlprovider;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import io.jenkins.blueocean.testing.GitSampleRepoRule;
import io.jenkins.blueocean.testing.JenkinsFile;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

/**
 * Created by ivan on 13/09/16.
 */
public class BlueOceanDisplayURLImplTest {

    private Pattern pathPAttern = Pattern.compile("http://.+:[0-9]+(/.*)");

    @Before
    public void before() throws Exception {
        repo.init();

    }

    private String getPath(String url) throws URISyntaxException {
        Matcher m = pathPAttern.matcher(url);
        m.matches();
        return m.group(1);
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public GitSampleRepoRule repo = new GitSampleRepoRule();

    @Test
    public void testProjectURL() throws IOException {
        FreeStyleProject p = j.createFreeStyleProject("abc");
        String url = DisplayURLProvider.get().getJobURL(p);
        Assert.assertEquals("http://localhost:63740/jenkins/blue/organizations/jenkins/pipelines/abc/", url);
    }

    @Test
    public void testProjectInFolder() throws Exception {
        MockFolder folder = j.createFolder("test");
        Project p = folder.createProject(FreeStyleProject.class, "abc");
        String url = getPath(DisplayURLProvider.get().getJobURL(p));
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/test%2Fabc/", url);

        p.scheduleBuild2(0).waitForStart();

        url = getPath(DisplayURLProvider.get().getRunURL(p.getLastBuild()));
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/test%2Fabc/detail/abc/1/", url);

        url = getPath(DisplayURLProvider.get().getChangesURL(p.getLastBuild()));
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/test%2Fabc/detail/abc/1/changes", url);

    }

    @Test
    public void testMultibranchUrls() throws Exception {
        repo.checkoutNewBranch("feature/test-1")
            .writeJenkinsFile(JenkinsFile.createFile().node().stage("stage1").echo("test").endNode())
            .addFile("Jenkinsfile")
            .commit("Initial commit to feature/test-1");

        MultiBranchTestBuilder mp = MultiBranchTestBuilder.createProjectInFolder(j, "folder", "test", repo);

        WorkflowJob job = mp.scheduleAndFindBranchProject("feature%2Ftest-1");

        String url = getPath(DisplayURLProvider.get().getRunURL(job.getFirstBuild()));

        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/folder%2Ftest/detail/feature%2Ftest-1/1/", url);

        url = getPath(DisplayURLProvider.get().getChangesURL(job.getFirstBuild()));
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/folder%2Ftest/detail/feature%2Ftest-1/1/changes", url);
    }

    public static class MultiBranchTestBuilder{
        private JenkinsRule j;
        private WorkflowMultiBranchProject mp;
        public MultiBranchTestBuilder(JenkinsRule j, WorkflowMultiBranchProject mp) {
            this.mp = mp;
            this.j = j;
        }


        public static MultiBranchTestBuilder createProjectInFolder(JenkinsRule j, String folderName, String name, GitSampleRepoRule gitRepo) throws IOException {
            MockFolder folder = j.createFolder(folderName);
            WorkflowMultiBranchProject mp = folder.createProject(WorkflowMultiBranchProject.class, name);
            mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, gitRepo.toString(), "", "*", "", false),
                new DefaultBranchPropertyStrategy(new BranchProperty[0])));

            for (SCMSource source : mp.getSCMSources()) {
                Assert.assertEquals(mp, source.getOwner());
            }

            return new MultiBranchTestBuilder(j, mp);
        }

        public static MultiBranchTestBuilder createProject(JenkinsRule j, String name, GitSampleRepoRule gitRepo) throws IOException {
            WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, name);
            mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, gitRepo.toString(), "", "*", "", false),
                new DefaultBranchPropertyStrategy(new BranchProperty[0])));

            for (SCMSource source : mp.getSCMSources()) {
                Assert.assertEquals(mp, source.getOwner());
            }

            return new MultiBranchTestBuilder(j, mp);
        }


        public WorkflowJob scheduleAndFindBranchProject(String name) throws Exception {
            mp.scheduleBuild2(0).getFuture().get();
            return findBranchProject(name);
        }

        public void schedule() throws Exception {
            mp.scheduleBuild2(0).getFuture().get();
        }

        public WorkflowJob findBranchProject(String name) throws Exception {
            WorkflowJob p = mp.getItem(name);
            if (p == null) {
                mp.getIndexing().writeWholeLogTo(System.out);
                fail(name + " project not found");
            }
            return p;
        }
    }
}
