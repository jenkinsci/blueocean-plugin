package io.jenkins.blueocean.displayurlprovider;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.TestResult;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Extension
public class BlueOceanDisplayURLImpl extends DisplayURLProvider {
    @Override
    public String getRoot() {
        return super.getRoot() + "blue/";
    }

    @Override
    public String getRunURL(Run<?, ?> run) {
        if(run instanceof WorkflowRun) {
            WorkflowJob job =  ((WorkflowRun) run).getParent();
            if(job.getParent() instanceof MultiBranchProject) {
                return getJobURL(((MultiBranchProject) job.getParent()))+ "detail/" +  Util.encode(job.getDisplayName()) + "/" + run.getNumber() + "/";
            }
        }
        Job job = run.getParent();
        return getJobURL(job) + "detail/" + Util.encode(job.getDisplayName()) + "/" + run.getNumber() + "/";
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return getRunURL(run) + "changes";
    }

    @Override
    public String getJobURL(Job<?, ?> project) {
        String jobPath;
        if(project.getParent() instanceof MultiBranchProject) {
            jobPath = Util.encode(project.getParent().getFullName());
        } else {
            jobPath = Util.encode(project.getFullName());
        }

        return getRoot() + "organizations/jenkins/pipelines/" + jobPath + "/";
    }

    @Override
    public String getTestUrl(TestResult result) {
        return getRunURL(result.getRun()) + "/tests";
    }

    private String getJobURL(MultiBranchProject<?, ?> project) {
        String jobPath = Util.encode(project.getFullName());

        return getRoot() + "organizations/jenkins/pipelines/" + jobPath + "/";
    }
}
