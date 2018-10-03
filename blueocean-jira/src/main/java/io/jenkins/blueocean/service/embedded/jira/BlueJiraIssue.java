package io.jenkins.blueocean.service.embedded.jira;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraJobAction;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.model.JiraIssue;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.model.BlueIssue;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Restricted(NoExternalUse.class)
public class BlueJiraIssue extends BlueIssue {

    private static final Logger LOGGER = Logger.getLogger(BlueJiraIssue.class.getName());

    private final String issueKey;
    private final String issueURL;

    public BlueJiraIssue(String issueKey, String issueURL) {
        this.issueKey = issueKey;
        this.issueURL = issueURL;
    }

    @Override
    public String getId() {
        return issueKey;
    }

    @Override
    public String getURL() {
        return issueURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlueJiraIssue that = (BlueJiraIssue) o;
        return Objects.equal(issueKey, that.issueKey) &&
            Objects.equal(issueURL, that.issueURL);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(issueKey, issueURL);
    }

    @Extension
    public static class FactoryImpl extends BlueIssueFactory {

        @Override
        public Collection<BlueIssue> getIssues(Job job) {
            JiraSite jiraSite = JiraSite.get(job);
            if (jiraSite == null) {
                return null;
            }
            JiraJobAction action = job.getAction(JiraJobAction.class);
            if (action == null) {
                return null;
            }
            BlueIssue issue = BlueJiraIssue.create(jiraSite, action.getIssue());
            if (issue == null) {
                return null;
            }
            return ImmutableList.of(issue);
        }

        @Override
        public Collection<BlueIssue> getIssues(ChangeLogSet.Entry changeSetEntry) {
            Run run = changeSetEntry.getParent().getRun();
            final JiraSite site = JiraSite.get(run.getParent());
            if (site == null) {
                return null;
            }
            final JiraBuildAction action = run.getAction(JiraBuildAction.class);
            if (action == null) {
                return null;
            }
            Collection<String> issueKeys = findIssueKeys(changeSetEntry.getMsg(), site.getIssuePattern());
            Iterable<BlueIssue> transformed = Iterables.transform(issueKeys,
                                                                  s -> BlueJiraIssue.create(site, action.getIssue(s)));
            return ImmutableList.copyOf(Iterables.filter(transformed, Predicates.notNull()));
        }
    }

    static Collection<String> findIssueKeys(String input, Pattern pattern) {
        Matcher m = pattern.matcher(input);
        Set<String> issues = new HashSet();
        while (m.find()) {
            if (m.groupCount() >= 1) {
                String id = m.group(1);
                issues.add(id);
            }
        }
        return issues;
    }

    @CheckForNull
    static BlueIssue create(@Nonnull JiraSite site, @Nullable JiraIssue issue) {
        if (issue == null) {
            return null;
        }
        try {
            return new BlueJiraIssue(issue.getKey(), site.getUrl(issue).toString());
        } catch (IOException e) {
            return null;
        }
    }
}
