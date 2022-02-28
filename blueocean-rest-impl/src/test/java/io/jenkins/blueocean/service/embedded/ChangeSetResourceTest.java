package io.jenkins.blueocean.service.embedded;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.service.embedded.rest.ChangeSetResource;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;

import org.junit.Rule;
import org.junit.Test;

import com.cloudbees.hudson.plugins.folder.Folder;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vivek Pandey
 */
public class ChangeSetResourceTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testChangeSet(){
        Reachable reachable = mock(Reachable.class);
        when(reachable.getLink()).thenReturn(new Link("/foo/bar"));
        User user = mock(User.class);
        when(user.getId()).thenReturn("vivek");
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        when(entry.getAuthor()).thenReturn(user);
        when(entry.getTimestamp()).thenReturn(System.currentTimeMillis());
        when(entry.getCommitId()).thenReturn("12345");
        when(entry.getMsg()).thenReturn("test changeset");
        when(entry.getAffectedPaths()).thenReturn(Collections.singleton("/foo/bar"));
        ChangeSetResource changeSetResource = new ChangeSetResource(new OrganizationImpl("testorg", mock(Folder.class)), entry, reachable);
        assertEquals(user.getId(), changeSetResource.getAuthor().getId());
        assertEquals(entry.getCommitId(), changeSetResource.getCommitId());
        assertEquals(entry.getMsg(), changeSetResource.getMsg());
    }

}
