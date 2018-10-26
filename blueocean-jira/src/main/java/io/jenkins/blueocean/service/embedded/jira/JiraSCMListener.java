package io.jenkins.blueocean.service.embedded.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Extension
public class JiraSCMListener extends SCMListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( JiraSCMListener.class);

    @Override
    public void onChangeLogParsed(Run<?, ?> run, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {

        try {
            JiraSite jiraSite = JiraSite.get(run.getParent());
            if (jiraSite == null) {
                return;
            }
            Collection<String> issueKeys = getIssueKeys(changelog, jiraSite.getIssuePattern());

            if (issueKeys.isEmpty()) {
                return;
            }
            String jql = constructJQLQuery(issueKeys);
            JiraSession session = jiraSite.getSession();
            if (session == null) {
                return;
            }
            // Query for JIRA issues
            List<Issue> issues = session.getIssuesFromJqlSearch(jql);
            Set<JiraIssue> issuesFromJqlSearch = issues == null ? Collections.emptySet() :
                issues.stream().map( input -> new JiraIssue(input) )
                    .collect( Collectors.toSet() );

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
        } catch (Exception e ){ // we do not want to fail the build if an issue happen here
            LOGGER.warn( "Failure executing Jira query to fetch issues. Skipping recording Jira issues.: {}", e.getMessage() );
            // stack trace in debug mode
            LOGGER.debug( e.getMessage(), e);
        }
    }

    static String constructJQLQuery( Collection<String> issueKeys) {
        StringBuilder jql = new StringBuilder();
        jql.append("key in (");
        Iterator<String> iterator = issueKeys.iterator();
        while ( iterator.hasNext() ) {
            String key = iterator.next();
            jql.append("'");
            jql.append(key);
            jql.append("'");
            if (iterator.hasNext()) {
                jql.append(",");
            }
        }
        jql.append(")");
        return jql.toString();
    }

    static Collection<String> getIssueKeys(ChangeLogSet<?> changelog, Pattern issuePattern) {
        Set<String> issueKeys = new HashSet<>();
        for (ChangeLogSet.Entry entry : changelog) {
            issueKeys.addAll(BlueJiraIssue.findIssueKeys(entry.getMsg(), issuePattern));
        }
        return issueKeys;
    }
}
