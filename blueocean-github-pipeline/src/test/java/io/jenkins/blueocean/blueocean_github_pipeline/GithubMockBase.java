package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import hudson.util.DescribableList;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.After;
import org.junit.After;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Stapler.class, OrganizationFolder.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*"})
public abstract class GithubMockBase extends PipelineBaseTest {
    protected String githubApiUrl;
    protected User user;
    protected String accessToken = "12345";

    @Rule
    public WireMockRule githubApi = new WireMockRule(wireMockConfig().
            dynamicPort().dynamicHttpsPort()
            .usingFilesUnderClasspath("api")
            .extensions(
                    new ResponseTransformer() {
                        @Override
                        public Response transform(Request request, Response response, FileSource files,
                                                  Parameters parameters) {
                            if ("application/json"
                                    .equals(response.getHeaders().getContentTypeHeader().mimeTypePart())) {
                                return Response.Builder.like(response)
                                        .but()
                                        .body(response.getBodyAsString()
                                                .replace("https://api.github.com/",
                                                        "http://localhost:" + githubApi.port() + "/")
                                        )
                                        .build();
                            }
                            return response;
                        }

                        @Override
                        public String getName() {
                            return "url-rewrite";
                        }

                    })
    );

    private final List<StubMapping> perTestStubMappings = new ArrayList<>();

    @Override
    public void setup() throws Exception {
        super.setup();
        //setup github api mock with WireMock
        new File("src/test/resources/api/mappings").mkdirs();
        new File("src/test/resources/api/__files").mkdirs();
        githubApi.enableRecordMappings(new SingleRootFileSource("src/test/resources/api/mappings"),
                new SingleRootFileSource("src/test/resources/api/__files"));
        githubApi.stubFor(
                WireMock.get(urlMatching(".*")).atPriority(10).willReturn(aResponse().proxiedFrom("https://api.github.com/")));

        this.user = login("vivek", "Vivek Pandey", "vivek.pandey@gmail.com");
        this.githubApiUrl = String.format("http://localhost:%s",githubApi.port());
    }

    @After
    public void tearDown() {
        if (!perTestStubMappings.isEmpty()) {
            for (StubMapping mapping : perTestStubMappings) {
                githubApi.removeStub(mapping);
            }

            perTestStubMappings.clear();
        }
    }

    protected String createGithubCredential() throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/?apiUrl="+githubApiUrl)
                .build(Map.class);
        String credentialId = (String) r.get("credentialId");
        assertEquals("github", credentialId);
        return credentialId;
    }

    protected String createGithubEnterpriseCredential() throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .data(ImmutableMap.of("accessToken", accessToken))
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .put("/organizations/jenkins/scm/github-enterprise/validate/?apiUrl="+githubApiUrl)
            .build(Map.class);
        String credentialId = (String) r.get("credentialId");
        assertEquals(GithubEnterpriseScm.ID+":"+ getGithubApiUrlEncoded(), credentialId);
        return credentialId;
    }

    protected String getGithubApiUrlEncoded() {
        return DigestUtils.sha256Hex(githubApiUrl);
    }

    /**
     * Add a StubMapping to Wiremock corresponding to the supplied builder.
     * Any mappings added will automatically be removed when @After fires.
     * @param builder
     */
    protected void addPerTestStub(MappingBuilder builder) {
        StubMapping mapping = githubApi.stubFor(builder);
        perTestStubMappings.add(mapping);
    }

    protected OrganizationFolder mockOrgFolder(String credentialId){
        OrganizationFolder orgFolder = mock(OrganizationFolder.class);

        //mock GithubSCMNavigator
        GitHubSCMNavigator navigator = mock(GitHubSCMNavigator.class);
        when(navigator.getApiUri()).thenReturn(githubApiUrl);
        when(navigator.getScanCredentialsId()).thenReturn(credentialId);
        when(navigator.getRepoOwner()).thenReturn("cloudbeers");


        when((orgFolder).getSCMNavigators()).thenReturn(Lists.<SCMNavigator>newArrayList(navigator));

        //mock blueocean credential provider stuff
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(orgFolder);
        properties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                user.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(githubApiUrl)
        ));
        when(orgFolder.getProperties()).thenReturn(properties);
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(GithubScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);
        return orgFolder;
    }

    protected MultiBranchProject mockMbp(OrganizationFolder orgFolder, String credentialId){
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("PR-demo");
        when(mbp.getParent()).thenReturn(orgFolder);
        GitHubSCMSource scmSource = mock(GitHubSCMSource.class);
        when(scmSource.getApiUri()).thenReturn(githubApiUrl);
        when(scmSource.getScanCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn("cloudbeers");
        when(scmSource.getRepository()).thenReturn("PR-demo");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> mbpProperties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(orgFolder);
        when(mbp.getProperties()).thenReturn(mbpProperties);
        return mbp;
    }

    protected StaplerRequest mockStapler(){
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("PR-demo");
        return staplerRequest;
    }
}
