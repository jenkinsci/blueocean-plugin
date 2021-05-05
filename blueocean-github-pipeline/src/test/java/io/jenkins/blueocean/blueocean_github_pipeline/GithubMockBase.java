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
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
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
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Stapler.class, OrganizationFolder.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public abstract class GithubMockBase extends PipelineBaseTest {

    // By default the wiremock tests will run without proxy
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy while writing and debugging tests.
    private final static boolean useProxy = !System.getProperty("test.wiremock.useProxy", "false").equals("false");

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

        if (useProxy) {
            githubApi.stubFor(
                WireMock.get(urlMatching(".*"))
                    .atPriority(10)
                    .willReturn(aResponse().proxiedFrom("https://api.github.com/")));
        }

        this.user = login("vivek", "Vivek Pandey", "vivek.pandey@gmail.com");
        this.githubApiUrl = String.format("http://localhost:%s",githubApi.port());
        this.crumb = getCrumb( j.jenkins );
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

    static String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    protected String createGithubCredential() throws UnirestException {
        return createGithubCredential(this.user);
    }

    protected String createGithubCredential(User user) throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .crumb( this.crumb )
                .put("/organizations/" + getOrgName() + "/scm/github/validate/?apiUrl="+githubApiUrl)
                .build(Map.class);
        String credentialId = (String) r.get("credentialId");
        assertEquals(GithubScm.ID, credentialId);
        return credentialId;
    }

    protected String createGithubEnterpriseCredential() throws UnirestException {
        return createGithubEnterpriseCredential(this.user);
    }
    protected String createGithubEnterpriseCredential(User user) throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .data(ImmutableMap.of("accessToken", accessToken))
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .crumb( this.crumb )
            .put("/organizations/" + getOrgName() + "/scm/github-enterprise/validate/?apiUrl="+githubApiUrl)
            .build(Map.class);
        String credentialId = (String) r.get("credentialId");
        assertEquals(GithubCredentialUtils.computeCredentialId(null, GithubEnterpriseScm.ID, githubApiUrl), credentialId);
        return credentialId;
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

    protected MultiBranchProject mockMbp(String credentialId,User user, String credentialDomainName){
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("PR-demo");
        when(mbp.getParent()).thenReturn(j.jenkins);
        GitHubSCMSource scmSource = mock(GitHubSCMSource.class);
        when(scmSource.getApiUri()).thenReturn(githubApiUrl);
        when(scmSource.getCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn("cloudbeers");
        when(scmSource.getRepository()).thenReturn("PR-demo");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> mbpProperties = new DescribableList<>(mbp);
        mbpProperties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                user.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(githubApiUrl)
        ));
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(credentialDomainName);
        when(folderProperty.getDomain()).thenReturn(domain);
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
