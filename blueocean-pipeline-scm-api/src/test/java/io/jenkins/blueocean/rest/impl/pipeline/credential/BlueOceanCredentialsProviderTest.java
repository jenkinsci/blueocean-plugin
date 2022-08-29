package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import com.cloudbees.plugins.credentials.domains.PathRequirement;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import hudson.model.ItemGroup;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.SecurityRealm;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BlueOceanCredentialsProviderTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Jenkins jenkins;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractFolder folder;

    @Mock
    DescribableList describableList;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    User user;

    @Test
    @Issue("JENKINS-53188")
    public void getCredentialsWhenUserExistedButNotAccessible() {
        Mockito.mockStatic(Jenkins.class);
        Mockito.when(Jenkins.get()).thenReturn(jenkins);
        Mockito.when(Jenkins.get()).thenReturn(jenkins);

        Mockito.mockStatic(User.class);
        // Make sure we return a user, cause it did once exist
        Mockito.when(User.get(anyString(), anyBoolean(), any())).thenReturn(user);

        Domain domain = BlueOceanCredentialsProvider.createDomain("api.github.com");
        BlueOceanCredentialsProvider blueOceanCredentialsProvider = new BlueOceanCredentialsProvider();
        BlueOceanCredentialsProvider.FolderPropertyImpl prop = new BlueOceanCredentialsProvider.FolderPropertyImpl(
            "halkeye",
            "halkeye",
            domain
        );

        // Should be empty when trying to impersonate and grab credentials though
        List<StandardUsernameCredentials> credentials = blueOceanCredentialsProvider.getCredentials(
            StandardUsernameCredentials.class,
            (ItemGroup) folder,
            ACL.SYSTEM,
            new ArrayList<>(Arrays.asList(
                new SchemeRequirement("https"),
                new HostnameRequirement("api.github.com"),
                new PathRequirement("/")
            ))
        );
        assertEquals(Collections.emptyList(), credentials);

        List<Credentials> storeCredentials = prop.getStore().getCredentials(domain);
        assertEquals(Collections.emptyList(), storeCredentials);


    }
}
