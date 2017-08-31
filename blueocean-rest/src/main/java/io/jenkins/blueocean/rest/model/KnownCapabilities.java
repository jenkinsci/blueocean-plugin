package io.jenkins.blueocean.rest.model;

/**
 * @author Vivek Pandey
 */
public class KnownCapabilities {
    public static final String RESOURCE="io.jenkins.blueocean.rest.model.Resource";
    public static final String GENERIC_RESOURCE="io.jenkins.blueocean.rest.model.GenericResource";
    public static final String BLUE_USER="io.jenkins.blueocean.rest.model.BlueUser";
    public static final String BLUE_RUN="io.jenkins.blueocean.rest.model.BlueRun";
    public static final String BLUE_QUEUE_ITEM="io.jenkins.blueocean.rest.model.BlueQueueItem";
    public static final String BLUE_PIPELINE_STEP="io.jenkins.blueocean.rest.model.BluePipelineStep";
    public static final String BLUE_PIPELINE_NODE="io.jenkins.blueocean.rest.model.BluePipelineNode";
    public static final String BLUE_PIPELINE_FOLDER="io.jenkins.blueocean.rest.model.BluePipelineFolder";
    public static final String BLUE_PIPELINE="io.jenkins.blueocean.rest.model.BluePipeline";
    public static final String BLUE_ORGANIZATION="io.jenkins.blueocean.rest.model.BlueOrganization";
    public static final String BLUE_MULTI_BRANCH_PIPELINE="io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline";
    public static final String BLUE_FAVORITE="io.jenkins.blueocean.rest.model.BlueFavorite";
    public static final String BLUE_EXTENSION_CLASS="io.jenkins.blueocean.rest.model.BlueExtensionClass";
    public static final String BLUE_BRANCH="io.jenkins.blueocean.rest.model.BlueBranch";
    public static final String PULL_REQUEST="io.jenkins.blueocean.rest.model.PullRequest";
    public static final String BLUE_ORGANIZATION_FOLDER = "io.jenkins.blueocean.rest.model.BlueOrganizationFolder";

    /** Jenkins core/plugin capabilities */
    public static final String JENKINS_WORKFLOW_JOB ="org.jenkinsci.plugins.workflow.job.WorkflowJob";
    public static final String JENKINS_WORKFLOW_RUN ="org.jenkinsci.plugins.workflow.job.WorkflowRun";
    public static final String JENKINS_ABSTRACT_FOLDER ="com.cloudbees.hudson.plugins.folder.AbstractFolder";
    public static final String JENKINS_JOB ="hudson.model.Job";
    public static final String JENKINS_MATRIX_PROJECT="hudson.matrix.MatrixProject";
    public static final String JENKINS_MULTI_BRANCH_PROJECT="jenkins.branch.MultiBranchProject";
    public static final String JENKINS_FREE_STYLE_BUILD="hudson.model.FreeStyleBuild";
    public static final String JENKINS_ORGANIZATION_FOLDER = "jenkins.branch.OrganizationFolder";

    /** Scm Content edit/save capability **/
    public static final String BLUE_SCM="io.jenkins.blueocean.rest.model.BluePipelineScm";
}
