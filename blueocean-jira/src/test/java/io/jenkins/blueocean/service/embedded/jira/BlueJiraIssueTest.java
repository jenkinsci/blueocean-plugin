package io.jenkins.blueocean.service.embedded.jira;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraJobAction;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.model.JiraIssue;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.model.BlueIssue;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JiraSite.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class BlueJiraIssueTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void findIssueKeys() throws MalformedURLException {
        Pattern issuePattern = JiraSite.DEFAULT_ISSUE_PATTERN;
        Assert.assertEquals(Sets.newHashSet("JENKINS-43400"), BlueJiraIssue.findIssueKeys("[JENKINS-43400] Print the error to the build log rather than", issuePattern));
        Assert.assertEquals(Sets.newHashSet("JENKINS-43400"), BlueJiraIssue.findIssueKeys("JENKINS-43400 Print the error to the build log rather than", issuePattern));
        Assert.assertEquals(Sets.newHashSet("JENKINS-43400"), BlueJiraIssue.findIssueKeys("foo/JENKINS-43400 Print the error to the build log rather than", issuePattern));
        Assert.assertEquals(Sets.newHashSet("TEST-123", "EXAMPLE-123", "JENKINS-43400"), BlueJiraIssue.findIssueKeys("foo/JENKINS-43400 TEST-123 [EXAMPLE-123] Print the error to the build log rather than", issuePattern));
    }

    @Test
    public void issuesForChangeSetItemForSite() throws Exception {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);

        Assert.assertTrue(BlueIssueFactory.resolve(entry).isEmpty());
    }

    @Test
    public void issuesForChangeSetItem() throws Exception {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);


        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        when(site.getIssuePattern()).thenReturn(JiraSite.DEFAULT_ISSUE_PATTERN);

        when(JiraSite.get(job)).thenReturn(site);

        when(entry.getMsg()).thenReturn("TEST-123");

        // Should resolve no issues because there is no JiraJobAction
        Assert.assertTrue(BlueIssueFactory.resolve(entry).isEmpty());

        // Setup a job with a JiraJobAction
        JiraIssue jiraIssue1 = new JiraIssue("FOO-123", "A cool issue");
        JiraIssue jiraIssue2 = new JiraIssue("FOO-124", "A cool issue");

        when(site.getUrl(jiraIssue1)).thenReturn(new URL("http://jira.example.com/browse/FOO-123"));
        when(site.getUrl(jiraIssue2)).thenReturn(new URL("http://jira.example.com/browse/FOO-124"));

        JiraBuildAction action = new JiraBuildAction(run, Sets.newLinkedHashSet(ImmutableSet.of(jiraIssue1, jiraIssue2)));
        when(run.getAction(JiraBuildAction.class)).thenReturn(action);

        // Expect two issues
        when(entry.getMsg()).thenReturn("something FOO-123 vivek FOO-124 #ace");
        List<BlueIssue> issues = Lists.newLinkedList(BlueIssueFactory.resolve(entry));
        Assert.assertEquals(2, issues.size());

        BlueIssue issue1 = issues.get(0);
        Assert.assertEquals("FOO-123", issue1.getId());
        Assert.assertEquals("http://jira.example.com/browse/FOO-123", issue1.getURL());

        BlueIssue issue2 = issues.get(1);
        Assert.assertEquals("FOO-124", issue2.getId());
        Assert.assertEquals("http://jira.example.com/browse/FOO-124", issue2.getURL());
    }

    @Test
    public void issuesForJob() throws Exception {
        Job job = mock(Job.class);

        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        when(JiraSite.get(job)).thenReturn(site);

        // Should resolve no issues because there is no JiraJobAction
        Assert.assertTrue(BlueIssueFactory.resolve(job).isEmpty());

        // Setup a job with a JiraJobAction
        JiraIssue jiraIssue = new JiraIssue("FOO-123", "A cool issue");

        when(site.getUrl(jiraIssue)).thenReturn(new URL("http://jira.example.com/browse/FOO-123"));

        JiraJobAction action = new JiraJobAction(job, jiraIssue);
        when(job.getAction(JiraJobAction.class)).thenReturn(action);

        // Expect a single issue
        Collection<BlueIssue> issues = BlueIssueFactory.resolve(job);
        Assert.assertEquals(1, issues.size());
        BlueIssue issue = issues.iterator().next();

        Assert.assertEquals("FOO-123", issue.getId());
        Assert.assertEquals("http://jira.example.com/browse/FOO-123", issue.getURL());
    }

    @Test
    public void issuesForJobNoSite() throws Exception {
        Job job = mock(Job.class);

        when(JiraSite.get(job)).thenReturn(null);

        // Should resolve no issues because there is no JiraJobAction
        Assert.assertTrue(BlueIssueFactory.resolve(job).isEmpty());
    }

    @Test
    public void issuesForJobNoAction() throws Exception {
        Job job = mock(Job.class);

        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        when(JiraSite.get(job)).thenReturn(site);
        when(job.getAction(JiraJobAction.class)).thenReturn(null);

        // Should resolve no issues because there is no JiraJobAction
        Assert.assertTrue(BlueIssueFactory.resolve(job).isEmpty());
    }

    @Test
    public void issuesForJobActionDoesNotHaveIssue() throws Exception {
        Job job = mock(Job.class);

        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        when(JiraSite.get(job)).thenReturn(site);
        JiraJobAction action = new JiraJobAction(job, null);
        when(job.getAction(JiraJobAction.class)).thenReturn(action);

        // Should resolve no issues because there is no JiraJobAction
        Assert.assertTrue(BlueIssueFactory.resolve(job).isEmpty());
    }

    @Test
    public void issueEqualityAndHashCode()  {
        BlueJiraIssue issue1 = new BlueJiraIssue("TEST-123", "http://jira.example.com/browse/TEST-123");
        BlueJiraIssue issue2 = new BlueJiraIssue("TEST-124", "http://jira.example.com/browse/TEST-124");

        Assert.assertTrue(issue1.equals(issue1));
        Assert.assertFalse(issue1.equals(issue2));
        Assert.assertFalse(issue1.equals(new Object()));

        Assert.assertNotEquals(issue1.hashCode(), issue2.hashCode());
    }
}
