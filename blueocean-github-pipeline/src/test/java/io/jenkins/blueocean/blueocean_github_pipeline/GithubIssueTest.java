package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.util.DescribableList;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.model.BlueIssue;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GithubIssueTest {

    /*
     * Totally a dumb subclass so mockito can find the invocation of getParent properly (since parent is protected class)
     * See https://stackoverflow.com/questions/19915270/mockito-stub-abstract-parent-class-method?rq=1
     */
    public abstract class MockJob extends Job {

        @Nonnull
        @Override
        public ItemGroup getParent() {
            return super.getParent();
        }

        protected MockJob(ItemGroup parent, String name) {
            super(parent, name);
        }
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void findIssueKeys() {
        Assert.assertEquals("Find single", Collections.singletonList("123"), GithubIssue.findIssueKeys("Closed #123"));
        Assert.assertEquals("Find multiple", Arrays.asList("123", "143"), GithubIssue.findIssueKeys( "Closed #123 and #143"));
        Assert.assertEquals("Do not find alpha", new ArrayList(), GithubIssue.findIssueKeys( "#AAA"));
    }

    @Test
    public void jobNotImplemented() throws Exception {
        Job job = mock(MockJob.class);
        Collection<BlueIssue> resolved = BlueIssueFactory.resolve(job);
        Assert.assertTrue(resolved.isEmpty());
    }

    @Test
    public void changeSetEntry() throws Exception {
        MultiBranchProject project = mock(MultiBranchProject.class);
        Job job = mock(MockJob.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(project.getProperties()).thenReturn(new DescribableList(project));
        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(job.getParent()).thenReturn(project);

        GitHubSCMSource source = new GitHubSCMSource("foo", null, null, null, "example", "repo");
        when(project.getSCMSources()).thenReturn(Collections.singletonList(source));

        when(entry.getMsg()).thenReturn("Closed #123 #124");

        Collection<BlueIssue> resolved = BlueIssueFactory.resolve(entry);
        Assert.assertEquals(2, resolved.size());

        Map<String, BlueIssue> issueMap = Maps.uniqueIndex( resolved, input -> input.getId() );

        BlueIssue issue123 = issueMap.get("#123");
        Assert.assertEquals("https://github.com/example/repo/issues/123", issue123.getURL());

        BlueIssue issue124 = issueMap.get("#124");
        Assert.assertEquals("https://github.com/example/repo/issues/124", issue124.getURL());
    }

    @Test
    public void changeSetEntryIsNotGithub() throws Exception {
        MultiBranchProject project = mock(MultiBranchProject.class);
        Job job = mock(MockJob.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(project.getProperties()).thenReturn(new DescribableList(project));
        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(job.getParent()).thenReturn(project);

        when(entry.getMsg()).thenReturn("Closed #123 #124");
        when(project.getSCMSources()).thenReturn(Collections.singletonList(new GitSCMSource("http://example.com/repo.git")));

        Collection<BlueIssue> resolved = BlueIssueFactory.resolve(entry);
        Assert.assertEquals(0, resolved.size());
    }

    @Test
    public void changeSetJobParentNotMultibranch() throws Exception {
        AbstractFolder project = mock(AbstractFolder.class);
        Job job = mock(MockJob.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(project.getProperties()).thenReturn(new DescribableList(project));
        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(job.getParent()).thenReturn(project);

        when(entry.getMsg()).thenReturn("Closed #123 #124");

        Collection<BlueIssue> resolved = BlueIssueFactory.resolve(entry);
        Assert.assertEquals(0, resolved.size());
    }
}

