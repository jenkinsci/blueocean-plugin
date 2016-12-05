package io.jenkins.blueocean;

import hudson.Extension;
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

/**
 * Created by ivan on 13/09/16.
 */
@Extension
public class BlueOceanDisplayURLImpl extends DisplayURLProvider {
    @Override
    public String getRoot() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        String root = jenkins.getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return root + "blue/";
    }

    @Override
    public String getRunURL(Run<?, ?> run) {
        if(run instanceof WorkflowRun) {
            WorkflowJob job =  ((WorkflowRun) run).getParent();
            if(job.getParent() instanceof MultiBranchProject) {
                return getJobURL(((MultiBranchProject) job.getParent()))+ "detail/" +  encode(job.getDisplayName()) + "/" + run.getNumber() + "/";
            }
        }
        if(run.getParent() instanceof Job) {
            Job job = ((Job) run.getParent());
            return getJobURL(job) + "detail/" + encode(job.getDisplayName()) + "/" + run.getNumber() + "/";
        }

        return null;
    }

    @Override
    public String getChangesURL(Run<?, ?> run) {
        return getRunURL(run) + "changes";
    }

    public String getJobURL(MultiBranchProject<?, ?> project) {
        String jobPath = encode(project.getFullName());

        return getRoot() + "organizations/jenkins/pipelines/" + jobPath + "/";
    }
    @Override
    public String getJobURL(Job<?, ?> project) {
        String jobPath;
        if(project.getParent() instanceof MultiBranchProject) {
            jobPath = encode(project.getParent().getFullName());
        } else {
            jobPath = encode(project.getFullName());
        }

        return getRoot() + "organizations/jenkins/pipelines/" + jobPath + "/";
    }

    private String encode(String path) {
        try {
            return URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException("Error encoding url");
        }
    }
    @Override
    public String getTestUrl(TestResult result) {
        return getRunURL(result.getRun()) + "/tests";
    }
}
