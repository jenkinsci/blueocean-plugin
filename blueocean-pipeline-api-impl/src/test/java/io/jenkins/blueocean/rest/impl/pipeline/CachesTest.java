package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.collect.Lists;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ExtensionList.class)
public class CachesTest {

    Jenkins jenkins;
    Job job;

    @Before
    public void setup() {
        jenkins = mock(Jenkins.class);
        when(jenkins.getFullName()).thenReturn("");

        Folder folder = mock(Folder.class);
        when(folder.getParent()).thenReturn(jenkins);
        when(folder.getFullName()).thenReturn("/Repo");
        when(jenkins.getItem("/Repo")).thenReturn(folder);

        job = mock(Job.class);
        when(job.getParent()).thenReturn(folder);
        when(job.getFullName()).thenReturn("cool-branch");

        when(jenkins.getItemByFullName("/Repo/cool-branch", Job.class)).thenReturn(job);
    }

    @Test
    public void testBranchCacheLoader() throws Exception {
        ObjectMetadataAction metadataAction = new ObjectMetadataAction("A cool branch", "A very cool change", "http://example.com/branches/cool-branch");
        PrimaryInstanceMetadataAction instanceMetadataAction = new PrimaryInstanceMetadataAction();
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(metadataAction);
        when(job.getAction(PrimaryInstanceMetadataAction.class)).thenReturn(instanceMetadataAction);

        Caches.BranchCacheLoader loader = new Caches.BranchCacheLoader(jenkins);
        BranchImpl.Branch branch = loader.load(job.getFullName());

        assertNotNull(branch);
        assertTrue(branch.isPrimary());
        assertEquals("http://example.com/branches/cool-branch", branch.getUrl());
    }

    @Test
    public void testBranchCacheLoaderWithNoObjectMetadataAction() throws Exception {
        PrimaryInstanceMetadataAction instanceMetadataAction = new PrimaryInstanceMetadataAction();
        when(job.getAction(PrimaryInstanceMetadataAction.class)).thenReturn(instanceMetadataAction);
        when(job.getFullName()).thenReturn("cool-branch");

        Caches.BranchCacheLoader loader = new Caches.BranchCacheLoader(jenkins);
        BranchImpl.Branch branch = loader.load(job.getFullName());

        assertNotNull(branch);
        assertTrue(branch.isPrimary());
        assertNull(branch.getUrl());
    }

    @Test
    public void testBranchCacheLoaderWithNoPrimaryInstanceMetadataAction() throws Exception {
        ObjectMetadataAction metadataAction = new ObjectMetadataAction("A cool branch", "A very cool change", "http://example.com/branches/cool-branch");
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(metadataAction);
        when(job.getFullName()).thenReturn("cool-branch");

        Caches.BranchCacheLoader loader = new Caches.BranchCacheLoader(jenkins);
        BranchImpl.Branch branch = loader.load(job.getFullName());

        assertNotNull(branch);
        assertFalse(branch.isPrimary());
        assertEquals("http://example.com/branches/cool-branch", branch.getUrl());
    }

    @Test
    public void testPullRequestCacheLoader() throws Exception {
        ObjectMetadataAction metadataAction = new ObjectMetadataAction("A cool PR", "A very cool change", "http://example.com/pr/1");
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(metadataAction);

        ContributorMetadataAction contributorMetadataAction = new ContributorMetadataAction("Hates Cake", "He hates cake", "hc@example.com");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        PowerMockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Lists.<SCMHead.HeadByItem>newArrayList(new HeadByItemForTest()).iterator());
        when(ExtensionList.lookup(SCMHead.HeadByItem.class)).thenReturn(extensionList);

        Caches.PullRequestCacheLoader loader = new Caches.PullRequestCacheLoader(jenkins);
        BranchImpl.PullRequest pr = loader.load(job.getFullName());

        assertNotNull(pr);
        assertEquals("Hates Cake", pr.getAuthor());
        assertEquals("1", pr.getId());
        assertEquals("A cool PR", pr.getTitle());
        assertEquals("http://example.com/pr/1", pr.getUrl());
    }

    @Test
    public void testPullRequestCacheLoaderWithoutScmHead() throws Exception {
        ObjectMetadataAction metadataAction = new ObjectMetadataAction("A cool PR", "A very cool change", "http://example.com/pr/1");
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(metadataAction);

        ContributorMetadataAction contributorMetadataAction = new ContributorMetadataAction("Hates Cake", "He hates cake", "hc@example.com");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        PowerMockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Lists.<SCMHead.HeadByItem>newArrayList().iterator());
        when(ExtensionList.lookup(SCMHead.HeadByItem.class)).thenReturn(extensionList);

        Caches.PullRequestCacheLoader loader = new Caches.PullRequestCacheLoader(jenkins);
        BranchImpl.PullRequest pr = loader.load(job.getFullName());

        assertNull(pr);
    }

    @Test
    public void testPullRequestCacheLoaderWithoutObjectMetadataAction() throws Exception {

        ContributorMetadataAction contributorMetadataAction = new ContributorMetadataAction("Hates Cake", "He hates cake", "hc@example.com");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        PowerMockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Lists.<SCMHead.HeadByItem>newArrayList(new HeadByItemForTest()).iterator());
        when(ExtensionList.lookup(SCMHead.HeadByItem.class)).thenReturn(extensionList);

        Caches.PullRequestCacheLoader loader = new Caches.PullRequestCacheLoader(jenkins);
        BranchImpl.PullRequest pr = loader.load(job.getFullName());

        assertNotNull(pr);
        assertEquals("Hates Cake", pr.getAuthor());
        assertEquals("1", pr.getId());
        assertNull(pr.getTitle());
        assertNull(pr.getUrl());
    }

    public class HeadByItemForTest extends SCMHead.HeadByItem {
        @Override
        public SCMHead getHead(Item item) {
            return new MockChangeRequestSCMHead(1, "foo");
        }
    }
}
