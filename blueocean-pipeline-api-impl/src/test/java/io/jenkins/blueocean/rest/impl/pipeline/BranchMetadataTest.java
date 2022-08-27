package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Job;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.Jenkins;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BranchMetadataTest {

    Jenkins jenkins;
    BlueOrganization org;
    Job job;
    BranchImpl branch;

    MockedStatic<Jenkins> jenkinsMockedStatic;

    @Before
    public void setup() {
        Caches.BRANCH_METADATA.invalidateAll();

        jenkins = mock(Jenkins.class);

        jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class);
        when(Jenkins.get()).thenReturn(jenkins);

        when(jenkins.getFullName()).thenReturn("");

        job = mock(Job.class);
        when(job.getParent()).thenReturn(jenkins);
        when(job.getFullName()).thenReturn("BobsPipeline");
        when(jenkins.getItemByFullName("BobsPipeline", Job.class)).thenReturn(job);

        org = mock(BlueOrganization.class);
        branch = new BranchImpl(org, job, new Link("foo"));
    }

    @After
    public void cleanup() {
        jenkinsMockedStatic.close();
    }

    @Test
    public void testGetURL() {
        assertNull(branch.getBranch());

        ObjectMetadataAction oma = new ObjectMetadataAction(
            "My Branch",
            "A feature branch",
            "https://path/to/branch"
        );
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(oma);

        Caches.BRANCH_METADATA.invalidateAll();

        assertNotNull(branch.getBranch());
        assertFalse(branch.getBranch().isPrimary());
        assertEquals("https://path/to/branch", branch.getBranch().getUrl());
    }

    @Test
    public void testBranchInfo() {
        assertNull(branch.getBranch());

        ObjectMetadataAction oma = new ObjectMetadataAction(
            "My Branch",
            "A feature branch",
            "https://path/to/branch"
        );
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(oma);
        when(job.getAction(PrimaryInstanceMetadataAction.class)).thenReturn(new PrimaryInstanceMetadataAction());

        Caches.BRANCH_METADATA.invalidateAll();

        assertEquals("https://path/to/branch", branch.getBranch().getUrl());
        assertTrue(branch.getBranch().isPrimary());
    }
}
