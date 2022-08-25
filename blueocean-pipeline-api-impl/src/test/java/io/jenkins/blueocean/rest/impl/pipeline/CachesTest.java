package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class CachesTest {

    /*
     * Totally a dumb subclass so mockito can find the invocation of getParent properly (since parent is protected class)
     * See https://stackoverflow.com/questions/19915270/mockito-stub-abstract-parent-class-method?rq=1
     */
    public abstract class MockJob extends Job {

        @NonNull
        @Override
        public ItemGroup getParent() {
            return super.getParent();
        }

        protected MockJob(ItemGroup parent, String name) {
            super(parent, name);
        }
    }

    Jenkins jenkins;

    MockJob job;

    Folder folder;

    MockedStatic<ExtensionList> extensionListMockedStatic;

    @Before
    public void setup() {
        jenkins = Mockito.mock(Jenkins.class);
        when(jenkins.getFullName()).thenReturn("");

        folder = Mockito.mock(Folder.class);
        when(folder.getParent()).thenReturn(jenkins);
        when(folder.getFullName()).thenReturn("/Repo");
        when(jenkins.getItem("/Repo")).thenReturn(folder);

        job = Mockito.mock(MockJob.class);
        when(job.getParent()).thenReturn(folder);
        when(job.getFullName()).thenReturn("/Repo/cool-branch");

        when(jenkins.getItemByFullName("/Repo/cool-branch", Job.class)).thenReturn(job);
    }

    @After
    public void cleanup() {
        if(extensionListMockedStatic!=null) {
            extensionListMockedStatic.close();
        }
    }

    public static class UsesJenkinsRule {
        @Rule public GitSampleRepoRule sampleRepo1 = new GitSampleRepoRule();
        @Rule public JenkinsRule r = new JenkinsRule();
        @Rule public TemporaryFolder tmp = new TemporaryFolder();

        @Test
        public void testBranchCacheLoaderNoMetadata() throws Exception {
            sampleRepo1.init();
            sampleRepo1.write("Jenkinsfile", "node { echo 'hi'; }");
            sampleRepo1.git("add", "Jenkinsfile");
            sampleRepo1.git("commit", "--all", "--message=buildable");

            WorkflowMultiBranchProject project = r.jenkins.createProject(WorkflowMultiBranchProject.class, "Repo");
            GitSCMSource source = new GitSCMSource(sampleRepo1.toString());
            source.setTraits(new ArrayList<>(Collections.singletonList(new BranchDiscoveryTrait())));

            BranchSource branchSource = new BranchSource(source);
            branchSource.setStrategy(new DefaultBranchPropertyStrategy(null));

            TaskListener listener = StreamTaskListener.fromStderr();
            assertEquals("[SCMHead{'master'}]", source.fetch(listener).toString());
            project.setSourcesList(new ArrayList<>(Collections.singletonList(branchSource)));

            project.scheduleBuild2(0).getFuture().get();

            Caches.BranchCacheLoader loader = new Caches.BranchCacheLoader(r.jenkins);
            BranchImpl.Branch branch = loader.load(project.getFullName() + "/master");

            // if branch is defined, it'll be sorted by branch
            assertNotNull(branch);
            assertTrue(branch.isPrimary());
            assertEquals("master", branch.getUrl());
        }

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

        extensionListMockedStatic = Mockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Collections.<SCMHead.HeadByItem>singletonList(new HeadByItemForTest()).iterator());
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

        extensionListMockedStatic = Mockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Collections.emptyIterator());
        when(ExtensionList.lookup(SCMHead.HeadByItem.class)).thenReturn(extensionList);

        Caches.PullRequestCacheLoader loader = new Caches.PullRequestCacheLoader(jenkins);
        BranchImpl.PullRequest pr = loader.load(job.getFullName());

        assertNull(pr);
    }

    @Test
    public void testPullRequestCacheLoaderWithoutObjectMetadataAction() throws Exception {

        ContributorMetadataAction contributorMetadataAction = new ContributorMetadataAction("Hates Cake", "He hates cake", "hc@example.com");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        extensionListMockedStatic = Mockito.mockStatic(ExtensionList.class);

        ExtensionList<SCMHead.HeadByItem> extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Collections.<SCMHead.HeadByItem>singletonList(new HeadByItemForTest()).iterator());
        when(ExtensionList.lookup(SCMHead.HeadByItem.class)).thenReturn(extensionList);

        Caches.PullRequestCacheLoader loader = new Caches.PullRequestCacheLoader(jenkins);
        BranchImpl.PullRequest pr = loader.load(job.getFullName());

        assertNotNull(pr);
        assertEquals("Hates Cake", pr.getAuthor());
        assertEquals("1", pr.getId());
        assertNull(pr.getTitle());
        assertNull(pr.getUrl());
    }

    public static class HeadByItemForTest extends SCMHead.HeadByItem {
        @Override
        public SCMHead getHead(Item item) {
            return new MockChangeRequestSCMHead(1, "foo");
        }
    }
}
