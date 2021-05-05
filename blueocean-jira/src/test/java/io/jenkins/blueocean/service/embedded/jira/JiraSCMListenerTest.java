package io.jenkins.blueocean.service.embedded.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JiraSite.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
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
                return Collections.singletonList(entry).iterator();
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

        when(session.getIssuesFromJqlSearch("key in ('TEST-123')")).thenReturn(Collections.singletonList(rawIssue));
        JiraBuildAction action = new JiraBuildAction(run, new HashSet());
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
                return Collections.singletonList(entry).iterator();
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

        when(session.getIssuesFromJqlSearch("key in ('TEST-123')")).thenReturn(Collections.singletonList(rawIssue));
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
                return Collections.singletonList(entry).iterator();
            }
        };

        // Setup JIRA site
        mockStatic(JiraSite.class);
        JiraSite site = mock(JiraSite.class);

        when(site.getIssuePattern()).thenReturn(JiraSite.DEFAULT_ISSUE_PATTERN);
        when(JiraSite.get(job)).thenReturn(site);

        JiraBuildAction action = new JiraBuildAction(run, new HashSet());
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
        Assert.assertEquals("key in ('JENKINS-123')",
                            JiraSCMListener.constructJQLQuery(Collections.singletonList("JENKINS-123")));
        Assert.assertEquals("key in ('JENKINS-123','FOO-123','VIVEK-123')",
                            JiraSCMListener.constructJQLQuery( Arrays.asList("JENKINS-123", "FOO-123", "VIVEK-123")));
    }

    @Test
    public void uniqueIssueKeys() throws Exception {
        ChangeLogSet<ChangeLogSet.Entry> entries = build( "TST-123", "TST-123", "TST-123", "TST-124",
                                                          "TST-123", "TST-124", "TST-125");
        Collection<String> keys = JiraSCMListener.getIssueKeys( entries, JiraSite.DEFAULT_ISSUE_PATTERN );
        Assert.assertEquals(3, keys.size());
    }


    private static ChangeLogSet build( String... texts) {
        List<ChangeLogSet.Entry> entries = Arrays.asList( texts ).stream().map( text -> {
            final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
            when(entry.getMsg()).thenReturn(text);
            return  entry;
        } ).collect( Collectors.toList() );

        return new ChangeLogSet<ChangeLogSet.Entry>(null, null) {
            @Override
            public boolean isEmptySet() {
                return false;
            }

            @Override
            public Iterator<Entry> iterator() {
                return entries.iterator();
            }
        };
    }

}
