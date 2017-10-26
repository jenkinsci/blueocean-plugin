package io.jenkins.blueocean.service.embedded.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraSession;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.model.JiraIssue;
import hudson.scm.ChangeLogSet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JiraSite.class)
public class JiraSCMListenerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void onChangeLogParsed() throws Exception {
        JiraSCMListener listener = new JiraSCMListener();

        Job job = mock(Job.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(entry.getMsg()).thenReturn("TEST-123");

        ChangeLogSet<ChangeLogSet.Entry> set = new ChangeLogSet<ChangeLogSet.Entry>(run, null) {
            @Override
            public boolean isEmptySet() {
                return false;
            }

            @Override
            public Iterator<Entry> iterator() {
                return ImmutableSet.of(entry).iterator();
            }
        };

        // Setup JIRA site
        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        JiraSession session = mock(JiraSession.class);

        when(site.getIssuePattern()).thenReturn(JiraSite.DEFAULT_ISSUE_PATTERN);
        when(site.getSession()).thenReturn(session);
        when(JiraSite.get(job)).thenReturn(site);

        Issue rawIssue = mock(Issue.class);
        when(rawIssue.getKey()).thenReturn("TEST-123");
        when(rawIssue.getSummary()).thenReturn("Foo");

        when(session.getIssuesFromJqlSearch("key in ('TEST-123')")).thenReturn(Lists.newArrayList(rawIssue));
        JiraBuildAction action = new JiraBuildAction(run, Sets.<JiraIssue>newHashSet());
        when(run.getAction(JiraBuildAction.class)).thenReturn(action);

        listener.onChangeLogParsed(run, null,null, set);

        Assert.assertFalse(action.getIssues().isEmpty());
        JiraIssue issue = action.getIssue("TEST-123");
        Assert.assertNotNull(issue);
        Assert.assertEquals("TEST-123", issue.getKey());
    }

    @Test
    public void onChangeLogParsedCreatesAction() throws Exception {
        JiraSCMListener listener = new JiraSCMListener();

        Job job = mock(Job.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(entry.getMsg()).thenReturn("TEST-123");

        ChangeLogSet<ChangeLogSet.Entry> set = new ChangeLogSet<ChangeLogSet.Entry>(run, null) {
            @Override
            public boolean isEmptySet() {
                return false;
            }

            @Override
            public Iterator<Entry> iterator() {
                return ImmutableSet.of(entry).iterator();
            }
        };

        // Setup JIRA site
        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);
        JiraSession session = mock(JiraSession.class);

        when(site.getIssuePattern()).thenReturn(JiraSite.DEFAULT_ISSUE_PATTERN);
        when(site.getSession()).thenReturn(session);
        when(JiraSite.get(job)).thenReturn(site);

        Issue rawIssue = mock(Issue.class);
        when(rawIssue.getKey()).thenReturn("TEST-123");
        when(rawIssue.getSummary()).thenReturn("Foo");

        when(session.getIssuesFromJqlSearch("key in ('TEST-123')")).thenReturn(Lists.newArrayList(rawIssue));
        when(run.getAction(JiraBuildAction.class)).thenReturn(null);

        ArgumentCaptor<JiraBuildAction> actionArgumentCaptor = ArgumentCaptor.forClass(JiraBuildAction.class);

        listener.onChangeLogParsed(run, null,null, set);
        verify(run).addAction(actionArgumentCaptor.capture());

        JiraBuildAction action = actionArgumentCaptor.getValue();
        Assert.assertFalse(action.getIssues().isEmpty());
        JiraIssue issue = action.getIssue("TEST-123");
        Assert.assertNotNull(issue);
        Assert.assertEquals("TEST-123", issue.getKey());
    }

    @Test
    public void changeSetHasNoJiraIssue() throws Exception {
        JiraSCMListener listener = new JiraSCMListener();

        Job job = mock(Job.class);
        Run run = mock(Run.class);
        ChangeLogSet logSet = mock(ChangeLogSet.class);
        final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

        when(entry.getParent()).thenReturn(logSet);
        when(logSet.getRun()).thenReturn(run);
        when(run.getParent()).thenReturn(job);
        when(entry.getMsg()).thenReturn("No jira ticket here");

        ChangeLogSet<ChangeLogSet.Entry> set = new ChangeLogSet<ChangeLogSet.Entry>(run, null) {
            @Override
            public boolean isEmptySet() {
                return false;
            }

            @Override
            public Iterator<Entry> iterator() {
                return ImmutableSet.of(entry).iterator();
            }
        };

        // Setup JIRA site
        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);

        when(site.getIssuePattern()).thenReturn(JiraSite.DEFAULT_ISSUE_PATTERN);
        when(JiraSite.get(job)).thenReturn(site);

        JiraBuildAction action = new JiraBuildAction(run, Sets.<JiraIssue>newHashSet());
        when(run.getAction(JiraBuildAction.class)).thenReturn(action);

        listener.onChangeLogParsed(run, null,null, set);

        Assert.assertTrue(action.getIssues().isEmpty());
    }

    @Test
    public void noJiraSiteDefined() throws Exception {
        JiraSCMListener listener = new JiraSCMListener();
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        when(run.getParent()).thenReturn(job);
        listener.onChangeLogParsed(run, null, null, null);
    }

    @Test
    public void constructJQLQuery() throws Exception {
        Assert.assertEquals("key in ('JENKINS-123')", JiraSCMListener.constructJQLQuery(Lists.newArrayList("JENKINS-123")));
        Assert.assertEquals("key in ('JENKINS-123','FOO-123','VIVEK-123')", JiraSCMListener.constructJQLQuery(Lists.newArrayList("JENKINS-123", "FOO-123", "VIVEK-123")));
    }
}
