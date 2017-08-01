package io.jenkins.blueocean.service.embedded.jira;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

@Restricted(NoExternalUse.class)
public class JIRAIssue extends BlueIssue {

    private static final Logger LOGGER = Logger.getLogger(JIRAIssue.class.getName());

    private final String issueKey;
    private final String issueURL;

    public JIRAIssue(String issueKey, String issueURL) {
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
            JiraIssue issue = action.getIssue();
            try {
                return Lists.<BlueIssue>newArrayList(new JIRAIssue(issue.getKey(), jiraSite.getUrl(issue).toString()));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot create representation of JIRA issue", e);
            }
            return null;
        }

        @Override
        public Collection<BlueIssue> getIssues(ChangeLogSet.Entry changeSetEntry) {
            Run run = changeSetEntry.getParent().getRun();
            JiraSite jiraSite = JiraSite.get(run.getParent());
            if (jiraSite == null) {
                return null;
            }
            JiraBuildAction action = run.getAction(JiraBuildAction.class);
            if (action == null) {
                return null;
            }
            Matcher m = jiraSite.getIssuePattern().matcher(changeSetEntry.getMsg());
            Set<BlueIssue> issues = Sets.newHashSet();
            while (m.find()) {
                if (m.groupCount() >= 1) {
                    String id = m.group(1);
                    JiraIssue issue = action.getIssue(id);
                    if (issue == null) {
                        continue;
                    }
                    try {
                        issues.add(new JIRAIssue(issue.getKey(), jiraSite.getUrl(issue).toString()));
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Cannot create representation of JIRA issue", e);
                    }
                }
            }
            return issues;
        }
    }
}
