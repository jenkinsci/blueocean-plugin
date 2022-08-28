package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.SecurityRealm;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Vivek Pandey
 */
@RunWith(MockitoJUnitRunner.class)
public class GithubScmTest {

    @Mock
    Jenkins jenkins;

    @Mock
    Authentication authentication;

    @Mock
    User user;

    MockedStatic<Jenkins> jenkinsMockedStatic;
    MockedStatic<User> userMockedStatic;

    @Before
    public void setup() throws Exception {
        jenkinsMockedStatic = mockStatic(Jenkins.class);

        when(Jenkins.get()).thenReturn(jenkins);
        when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
        when(Jenkins.getInstance()).thenReturn(jenkins);
        when(Jenkins.getAuthentication()).thenReturn(authentication);
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY};

        userMockedStatic = mockStatic(User.class);

        when(user.getId()).thenReturn("joe");
        when(User.get(authentication)).thenReturn(user);
        when(User.current()).thenReturn(user);
    }

    @After
    public void cleanup() {
        jenkinsMockedStatic.close();
        userMockedStatic.close();
    }

    @Test
    public void validateAccessTokenScopes() throws Exception {

        HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);

        HttpRequest httpRequest = mock(HttpRequest.class);

        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            when(HttpRequest.get(GitHubSCMSource.GITHUB_URL)).thenReturn(httpRequest);
            when(httpRequest.withAuthorizationToken(any())).thenReturn(httpRequest);
            when(httpRequest.connect()).thenReturn(httpURLConnectionMock);
            when(httpURLConnectionMock.getHeaderField("X-OAuth-Scopes")).thenReturn("user:email,repo");
            when(httpURLConnectionMock.getResponseCode()).thenReturn(200);
            HttpURLConnection httpURLConnection = GithubScm.connect(GitHubSCMSource.GITHUB_URL, "abcd");
            GithubScm.validateAccessTokenScopes(httpURLConnection);
        }
    }

    @Test
    public void validateAndCreate() throws Exception {
        validateAndCreate("12345");
    }

    @Test
    public void validateAndCreatePaddedToken() throws Exception {
        validateAndCreate(" 12345 ");
    }

    protected void validateAndCreate(String accessToken) throws Exception {

        Mailer.UserProperty userProperty = mock(Mailer.UserProperty.class);
        JSONObject req = new JSONObject().element("accessToken", accessToken);
        GithubScm githubScm = new GithubScm(() -> new Link( "/blue/organizations/jenkins/scm/"));

        mockCredentials("joe", accessToken, githubScm.getId(), GithubScm.DOMAIN_NAME);

        try (MockedStatic<OrganizationFactory> mock = mockStatic(OrganizationFactory.class);
             MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class);
             MockedStatic<Stapler> mockedStatic1 = mockStatic(Stapler.class);
             MockedStatic<CredentialsUtils> credentialsUtilsMockedStatic = mockStatic(CredentialsUtils.class)) {

            OrganizationFactoryImpl organizationFactory = mock(OrganizationFactoryImpl.class);
            when(OrganizationFactory.getInstance()).thenReturn(organizationFactory);
            AbstractOrganization organization = mock(AbstractOrganization.class);
            when(organizationFactory.list()).thenReturn(Collections.singletonList(organization));
            Folder rootOrgFolder = mock(Folder.class);
            when(organization.getGroup()).thenReturn(rootOrgFolder);
            when(rootOrgFolder.hasPermission(Item.CREATE)).thenReturn(true);
            HttpRequest httpRequestMock = mock(HttpRequest.class);

            ArgumentCaptor<String> urlStringCaptor = ArgumentCaptor.forClass(String.class);
            when(HttpRequest.get(urlStringCaptor.capture())).thenReturn(httpRequestMock);

            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            when(httpRequestMock.withAuthorizationToken(tokenCaptor.capture())).thenReturn(httpRequestMock);

            HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
            when(httpRequestMock.connect()).thenReturn(httpURLConnectionMock);

            when(httpURLConnectionMock.getHeaderField("X-OAuth-Scopes")).thenReturn("user:email,repo");
            when(httpURLConnectionMock.getResponseCode()).thenReturn(200);

            String guser = "{\n  \"login\": \"joe\",\n  \"id\": 1, \"email\": \"joe@example.com\", \"created_at\": \"2008-01-14T04:33:35Z\"}";

            StaplerRequest request = mock(StaplerRequest.class);
            when(Stapler.getCurrentRequest()).thenReturn(request);

            when(HttpRequest.getInputStream(httpURLConnectionMock)).thenReturn(new ByteArrayInputStream(guser.getBytes(StandardCharsets.UTF_8)));

            StandardUsernamePasswordCredentials credentials = mock(StandardUsernamePasswordCredentials.class);
            when(credentials.getId()).thenReturn(GithubScm.ID);
            when(CredentialsUtils.findCredential(any(), any(), any())).thenReturn(credentials);

            githubScm.validateAndCreate(req);

            String id = githubScm.getCredentialId();
            Assert.assertEquals(githubScm.getId(), id);

            Assert.assertEquals("constructed url", "https://api.github.com/user", urlStringCaptor.getValue());
            Assert.assertEquals("access token passed to github", accessToken.trim(), tokenCaptor.getValue());
        }

    }

    @Test
    public void getOrganizations() {
        try (MockedStatic<Stapler> staplerMockedStatic = mockStatic(Stapler.class)) {
            StaplerRequest staplerRequest = mock(StaplerRequest.class);
            when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        }
    }

    void mockCredentials(String userId, String accessToken, String credentialId, String domainName) throws Exception {
        //Mock Credentials
        UsernamePasswordCredentialsImpl credentials = mock(UsernamePasswordCredentialsImpl.class);

        CredentialsMatcher credentialsMatcher = mock(CredentialsMatcher.class);
        try(MockedStatic<CredentialsMatchers> credentialsMatchersMockedStatic = mockStatic(CredentialsMatchers.class);
            MockedStatic<CredentialsProvider> credentialsProviderMockedStatic =  mockStatic(CredentialsProvider.class)){
            when(CredentialsMatchers.withId(credentialId)).thenReturn(credentialsMatcher);

            BlueOceanDomainRequirement blueOceanDomainRequirement = mock(BlueOceanDomainRequirement.class);

            when(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, jenkins, authentication, blueOceanDomainRequirement))
                .thenReturn(Collections.singletonList(credentials));
            when(CredentialsMatchers.firstOrNull(Collections.singletonList(credentials), credentialsMatcher)).thenReturn(credentials);
            when(CredentialsMatchers.allOf(credentialsMatcher)).thenReturn(credentialsMatcher);

            //Mock credentials Store
            CredentialsStore credentialsStore = mock(CredentialsStore.class);
            when(CredentialsProvider.lookupStores(user)).thenReturn(Collections.singleton(credentialsStore));
        }
    }
}
