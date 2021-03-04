package io.jenkins.blueocean.blueocean_github_pipeline;


import hudson.Extension;
import hudson.model.Job;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.model.BlueIssue;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.HttpsRepositoryUriResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubIssue extends BlueIssue {

    private static final Pattern PATTERN = Pattern.compile("((?:[\\w-.]+\\/[\\w-.]+)?#[1-9]\\d*)");

    private final String id;
    private final String url;

    public GithubIssue(String id, String url) {
        this.id = id;
        this.url = url;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Extension
    public static class FactoryImpl extends BlueIssueFactory {
        @Override
        public Collection<BlueIssue> getIssues(Job job) {
            return null; // Github branches cannot be linked to tickets
        }

        @Override
        public Collection<BlueIssue> getIssues(ChangeLogSet.Entry changeSetEntry) {
            Job job = changeSetEntry.getParent().getRun().getParent();
            if (!(job.getParent() instanceof MultiBranchProject)) {
                return null;
            }
            MultiBranchProject mbp = (MultiBranchProject)job.getParent();
            List<SCMSource> scmSources = (List<SCMSource>) mbp.getSCMSources();
            SCMSource source = scmSources.isEmpty() ? null : scmSources.get(0);
            if (!(source instanceof GitHubSCMSource)) {
                return null;
            }
            GitHubSCMSource gitHubSource = (GitHubSCMSource)source;
            String apiUri =  gitHubSource.getApiUri();
            final String repositoryUri = new HttpsRepositoryUriResolver().getRepositoryUri(apiUri, gitHubSource.getRepoOwner(), gitHubSource.getRepository());
            Collection<BlueIssue> results = new ArrayList<>();
            for (String input : findIssueKeys(changeSetEntry.getMsg())) {
                String uri = repositoryUri.substring(0, repositoryUri.length() - 4);
                results.add(new GithubIssue("#" + input, String.format("%s/issues/%s", uri, input)));
            }
            return results;
        }

    }

    static Collection<String> findIssueKeys(String input) {
        Collection<String> ids = new ArrayList<>();
        Matcher m = PATTERN.matcher(input);
        while (m.find()) {
            if (m.groupCount() >= 1) {
                String issue = m.group(1);
                ids.add(issue.substring(1, issue.length()));
            }
        }
        return ids;
    }
}
