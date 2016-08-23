package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.model.URLFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

@Extension
public class BlueURLFactory extends URLFactory {
    @Override
    public String getRunURL(Run<?, ?> run) {
        return generateBlueUrl(getOrganization(), run) + "pipeline";
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return generateBlueUrl(getOrganization(), run) + "changes";
    }

    @Override
    public String getProjectURL(Item item) {
        String org = getOrganization();
        return getBaseURL() + generateBlueUrl(org, item);
    }

    static String getOrganization() {
        Jenkins j = Jenkins.getInstance();
        return j.getDisplayName().toLowerCase();
    }

    static String getBaseURL() {
        String url = JenkinsLocationConfiguration.get().getUrl();
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    static String generateBlueUrl(String org, Run<?, ?> run) {
        Job<?, ?> parent = run.getParent();
        return generateBlueUrl(org, parent) + "/" + parent.getName() + "/details/" + run.getNumber() + "/";
    }

    static String generateBlueUrl(String org, Item i) {
        String url = "/organizations/" + org + "/pipelines/";
        if(i instanceof WorkflowJob) {
            WorkflowJob job = (WorkflowJob)i;
            ItemGroup it = job.getParent();
            if(it instanceof WorkflowMultiBranchProject) {
                url += ((WorkflowMultiBranchProject) it).getName() + "/branches/" + job.getName();
            } else {
                url += job.getName();
            }
        } else {
            url += i.getName();
        }
        return url;
    }
}
