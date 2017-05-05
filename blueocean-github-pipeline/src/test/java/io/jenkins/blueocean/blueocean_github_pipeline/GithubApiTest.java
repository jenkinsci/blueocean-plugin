package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.jenkins.blueocean.blueocean_github_pipeline.GithubScm.GITHUB_API_URL_PROPERTY;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class GithubApiTest extends PipelineBaseTest {

    private User user;
    private String githubApiUrl;
    private final String accessToken = "12345";

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

    @Before
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
        System.setProperty(GITHUB_API_URL_PROPERTY, githubApiUrl);
    }

    @Test
    public void validateGithubToken() throws IOException, UnirestException {
        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);
        Assert.assertNull(r.get("credentialId"));
        assertEquals("github", r.get("id"));

        r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));

        //check if this credentialId is created in correct user domain
        Domain domain = CredentialsUtils.findDomain("github", user);
        assertEquals("blueocean-github-domain", domain.getName());

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
        assertEquals("github", r.get("id"));

        //now try validating again, it should return the same credentialId
        r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
    }

    @Test
    public void validateGithubEnterpriseToken() throws IOException, UnirestException {
        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github-enterprise/")
                .build(Map.class);
        Assert.assertNull(r.get("credentialId"));
        assertEquals("github-enterprise", r.get("id"));
    }

    @Test
    public void getOrganizationsAndRepositories() throws Exception {
        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);


        assertEquals("github", r.get("credentialId"));
        String credentialId = (String) r.get("credentialId");


        List l = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/?credentialId=" + credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(List.class);

        Assert.assertTrue(l.size() > 0);


        Map resp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/?credentialId=" + credentialId + "&pageSize=10&page=1")
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);

        Map repos = (Map) resp.get("repositories");
        assertNotNull(repos);

        List<Map> repoItems = (List<Map>) repos.get("items");
        assertTrue(repoItems.size() > 0);

        resp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/RunMyProcess-task/?credentialId=" + credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);

        assertEquals("RunMyProcess-task", resp.get("name"));
    }
}
