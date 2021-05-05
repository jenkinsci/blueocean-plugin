package io.jenkins.blueocean.preload;

import hudson.model.FreeStyleProject;
import hudson.model.Run;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.ChangeSetContainerImpl;
import io.jenkins.blueocean.service.embedded.rest.FreeStylePipeline;
import io.jenkins.blueocean.service.embedded.rest.RunSearch;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BlueRunChangesetPreloaderTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void prefetchUrlIsRight() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject project = j.createFreeStyleProject("project");
        Run run = j.waitForCompletion(project.scheduleBuild2(0).waitForStart());

        FreeStylePipeline freeStylePipeline = (FreeStylePipeline) BluePipelineFactory.resolve(project);
        assertNotNull(freeStylePipeline);
        BlueRun blueRun = freeStylePipeline.getLatestRun();
        assertNotNull(blueRun);

        BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(Jenkins.getInstance());

        ChangeSetContainerImpl container = new ChangeSetContainerImpl(
                organization,
                blueRun,
                run
        );
        BlueRunChangesetPreloader preloader = new BlueRunChangesetPreloader();
        RESTFetchPreloader.FetchData fetchData = preloader.getFetchData(container);
        assertEquals("/blue/rest/organizations/jenkins/pipelines/project/runs/1/changeSet/?start=0&limit=101", fetchData.getRestUrl());
    }

}