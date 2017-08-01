package io.jenkins.blueocean.service.embedded.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.plugins.jira.JiraBuildAction;
import hudson.plugins.jira.JiraSession;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.model.JiraIssue;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;

import java.util.List;
import java.util.Set;

@Extension
public class JiraSCMListener extends SCMListener {
    @Override
    public void onChangeLogParsed(Run<?, ?> run, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
        JiraSite jiraSite = JiraSite.get(run.getParent());
        if (jiraSite == null) {
            return;
        }
        List<String> issueKeys = Lists.newArrayList();
        for (ChangeLogSet.Entry entry : changelog) {
            issueKeys.addAll(BlueJiraIssue.findIssueKeys(entry.getMsg(), jiraSite.getIssuePattern()));
        }
        String jql = constructJQLQuery(issueKeys);
        JiraSession session = jiraSite.getSession();
        // Query for JIRA issues
        Set<JiraIssue> issuesFromJqlSearch = Sets.newHashSet(Iterables.transform(session.getIssuesFromJqlSearch(jql), new Function<Issue, JiraIssue>() {
            @Override
            public JiraIssue apply(Issue input) {
                return new JiraIssue(input);
            }
        }));
        // If there are no JIRA issues, do not update the actions
        if (issuesFromJqlSearch.isEmpty()) {
            return;
        }
        // Create or update the JiraBuildAction
        JiraBuildAction action = run.getAction(JiraBuildAction.class);
        if (action == null) {
            run.addAction(new JiraBuildAction(run, issuesFromJqlSearch));
        } else {
            action.addIssues(issuesFromJqlSearch);
        }
        run.save();
    }

    static String constructJQLQuery(List<String> issueKeys) {
        StringBuilder jql = new StringBuilder();
        jql.append("key in (");
        for (int i = 0; i < issueKeys.size(); i++) {
            jql.append("'");
            jql.append(issueKeys.get(i));
            jql.append("'");
            if (issueKeys.size() > 1 &&  i+1 < issueKeys.size()) {
                jql.append(",");
            }
        }
        jql.append(")");
        return jql.toString();
    }
}
