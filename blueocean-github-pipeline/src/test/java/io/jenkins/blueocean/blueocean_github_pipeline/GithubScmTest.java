package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.User;
import hudson.security.SecurityRealm;
import hudson.tasks.Mailer;
import hudson.util.Secret;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static io.jenkins.blueocean.rest.impl.pipeline.scm.Scm.CREDENTIAL_ID;
import static org.powermock.api.mockito.PowerMockito.*;


/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GithubScm.class, Jenkins.class, Authentication.class, User.class, Secret.class,
    CredentialsMatchers.class, CredentialsProvider.class, Stapler.class, HttpRequest.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GithubScmTest {

    @Mock
    Jenkins jenkins;

    @Mock
    Authentication authentication;

    @Mock
    User user;

    @Before
    public void setup() throws Exception {
        mockStatic(Jenkins.class);

        when(Jenkins.get()).thenReturn(jenkins);
        when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
        when(Jenkins.getAuthentication()).thenReturn(authentication);
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY};

        Mockito.when(authentication.getAuthorities()).thenReturn(grantedAuthorities);
        Mockito.when(authentication.getPrincipal()).thenReturn("joe");
        mockStatic(User.class);

        when(user.getId()).thenReturn("joe");
        when(user.getFullName()).thenReturn("joe smith");
        when(user.getDisplayName()).thenReturn("joe smith");
        when(User.class, method(User.class, "get", Authentication.class)).withArguments(authentication).thenReturn(user);
        when(User.current()).thenReturn(user);
    }

    @Test
    public void validateAccessTokenScopes() throws Exception {

        HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
        doNothing().when(httpURLConnectionMock).connect();

        URL urlMock = mock(URL.class);
        whenNew(URL.class).withAnyArguments().thenReturn(urlMock);
        when(urlMock.openConnection(Proxy.NO_PROXY)).thenReturn(httpURLConnectionMock);
        when(httpURLConnectionMock.getHeaderField("X-OAuth-Scopes")).thenReturn("user:email,repo");
        when(httpURLConnectionMock.getResponseCode()).thenReturn(200);

        HttpURLConnection httpURLConnection = GithubScm.connect(GitHubSCMSource.GITHUB_URL, "abcd");
        GithubScm.validateAccessTokenScopes(httpURLConnection);
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
        when(userProperty.getAddress()).thenReturn("joe@example.com");
        JSONObject req = new JSONObject().element("accessToken", accessToken);
        GithubScm githubScm = new GithubScm(() -> new Link( "/blue/organizations/jenkins/scm/"));

        mockCredentials("joe", accessToken, githubScm.getId(), GithubScm.DOMAIN_NAME);

        mockStatic(HttpRequest.class);
        HttpRequest httpRequestMock = mock(HttpRequest.class);

        ArgumentCaptor<String> urlStringCaptor = ArgumentCaptor.forClass(String.class);
        when(HttpRequest.get(urlStringCaptor.capture())).thenReturn(httpRequestMock);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        when(httpRequestMock.withAuthorizationToken(tokenCaptor.capture())).thenReturn(httpRequestMock);

        HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
        doNothing().when(httpURLConnectionMock).connect();
        when(httpRequestMock.connect()).thenReturn(httpURLConnectionMock);

        when(httpURLConnectionMock.getHeaderField("X-OAuth-Scopes")).thenReturn("user:email,repo");
        when(httpURLConnectionMock.getResponseCode()).thenReturn(200);

        String guser = "{\n  \"login\": \"joe\",\n  \"id\": 1, \"email\": \"joe@example.com\", \"created_at\": \"2008-01-14T04:33:35Z\"}";

        mockStatic(Stapler.class);
        StaplerRequest request = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(request);

        when(HttpRequest.getInputStream(httpURLConnectionMock)).thenReturn(new ByteArrayInputStream(guser.getBytes(StandardCharsets.UTF_8)));

        githubScm.validateAndCreate(req);

        String id = githubScm.getCredentialId();
        Assert.assertEquals(githubScm.getId(), id);

        Assert.assertEquals("constructed url", "https://api.github.com/user", urlStringCaptor.getValue());
        Assert.assertEquals("access token passed to github", accessToken.trim(), tokenCaptor.getValue());
    }

    @Test
    public void getOrganizations() {
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);

        when(staplerRequest.getParameter(CREDENTIAL_ID)).thenReturn("12345");

    }

    void mockCredentials(String userId, String accessToken, String credentialId, String domainName) throws Exception {
        //Mock Credentials
        UsernamePasswordCredentialsImpl credentials = mock(UsernamePasswordCredentialsImpl.class);
        whenNew(UsernamePasswordCredentialsImpl.class).withAnyArguments().thenReturn(credentials);
        when(credentials.getId()).thenReturn(credentialId);
        when(credentials.getUsername()).thenReturn(userId);

        Secret secret = mock(Secret.class);
        when(secret.getPlainText()).thenReturn(accessToken);
        when(credentials.getPassword()).thenReturn(secret);
        CredentialsMatcher credentialsMatcher = mock(CredentialsMatcher.class);
        mockStatic(CredentialsMatchers.class);
        mockStatic(CredentialsProvider.class);
        when(CredentialsMatchers.withId(
            credentialId)).thenReturn(credentialsMatcher);

        BlueOceanDomainRequirement blueOceanDomainRequirement = mock(BlueOceanDomainRequirement.class);
        whenNew(BlueOceanDomainRequirement.class).withNoArguments().thenReturn(blueOceanDomainRequirement);

        when(CredentialsProvider.class, "lookupCredentials",
             StandardUsernamePasswordCredentials.class, jenkins, authentication, blueOceanDomainRequirement)
            .thenReturn(Collections.singletonList(credentials));

        when(CredentialsMatchers.class, "firstOrNull",
             Collections.singletonList(credentials), credentialsMatcher).thenReturn(credentials);

        when(CredentialsMatchers.allOf(credentialsMatcher)).thenReturn(credentialsMatcher);

        //Mock credentials Domain
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(domainName);

        //Mock credentials Store
        CredentialsStore credentialsStore = mock(CredentialsStore.class);
        when(credentialsStore.hasPermission(CredentialsProvider.CREATE)).thenReturn(true);
        when(credentialsStore.hasPermission(CredentialsProvider.UPDATE)).thenReturn(true);
        when(credentialsStore.getDomainByName(domainName)).thenReturn(domain);

        when(CredentialsProvider.class, "lookupStores", user).
            thenReturn(Collections.singletonList(credentialsStore));

        when(credentialsStore.addCredentials(domain, credentials)).thenReturn(true);
        when(credentialsStore.updateCredentials(domain, credentials, credentials)).thenReturn(true);
    }
}
