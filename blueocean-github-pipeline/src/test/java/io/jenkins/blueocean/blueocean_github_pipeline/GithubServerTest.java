package io.jenkins.blueocean.blueocean_github_pipeline;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class GithubServerTest extends PipelineBaseTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
        wireMockConfig()
            .dynamicPort()
            .usingFilesUnderClasspath("server-api")
    );

    String token;

    @Before
    public void createUser() throws Exception {
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm( true);
        User writeUser = realm.createAccount("write_user", "pale_ale");
        j.jenkins.setSecurityRealm(realm);
        GlobalMatrixAuthorizationStrategy as = new GlobalMatrixAuthorizationStrategy();
        j.jenkins.setAuthorizationStrategy(as);
        as.add( Jenkins.READ, (String)Jenkins.ANONYMOUS.getPrincipal());
        {
            as.add( Item.BUILD, writeUser.getId());
            as.add(Item.CREATE, writeUser.getId());
            as.add(Item.CONFIGURE, writeUser.getId());
        }
        token = getJwtToken(j.jenkins, "write_user", "pale_ale");
        this.crumb = getCrumb(j.jenkins);
    }

    @Test
    public void testServerNotGithub() throws Exception {
        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb(crumb)
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getApiUrlCustomPath("/notgithub")
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("apiUrl", error1.get("field"));
        Assert.assertEquals(GithubServerContainer.ERROR_MESSAGE_INVALID_SERVER, error1.get("message"));
        Assert.assertEquals("INVALID", error1.get("code"));
    }

    @Test
    public void testServerGithubEnterpriseTopLevelUrl() throws Exception {
        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("apiUrl", error1.get("field"));
        Assert.assertEquals(GithubServerContainer.ERROR_MESSAGE_INVALID_APIURL, error1.get("message"));
        Assert.assertEquals("INVALID", error1.get("code"));
    }

    @Test
    public void testServerUnknownHost() throws Exception {
        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", "http://foobar/"
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("apiUrl", error1.get("field"));
        Assert.assertEquals(new UnknownHostException("foobar").toString(), error1.get("message"));
        Assert.assertEquals("INVALID", error1.get("code"));
    }

    @Test
    public void testMissingParams() throws Exception {
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of())
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(2, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("name", error1.get("field"));

        Map error2 = (Map) errors.get(1);
        Assert.assertEquals("apiUrl", error2.get("field"));
    }

    @Test
    public void testMissingUrlParam() throws Exception {
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of("name", "foo"))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("apiUrl", error1.get("field"));
        Assert.assertEquals("MISSING", error1.get("code"));
        Assert.assertEquals("apiUrl is required", error1.get("message"));
    }

    @Test
    public void testMissingNameParam() throws Exception {
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of("apiUrl", getDefaultApiUrl()))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("name", error1.get("field"));
        Assert.assertEquals("MISSING", error1.get("code"));
        Assert.assertEquals("name is required", error1.get("message"));
    }

    @Test
    public void avoidDuplicateByUrl() throws Exception {
        // Create a server
        Map server = request()
            .status(200)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getDefaultApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        // Create a server
        Map resp = server = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server 2",
                "apiUrl", getDefaultApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("apiUrl", error1.get("field"));
        Assert.assertEquals("ALREADY_EXISTS", error1.get("code"));
        Assert.assertEquals("apiUrl is already registered as 'My Server'", error1.get("message"));
    }

    @Test
    public void avoidDuplicateByName() throws Exception {
        // Create a server
        request()
            .status(200)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getDefaultApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getDefaultApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(2, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("name", error1.get("field"));
        Assert.assertEquals("ALREADY_EXISTS", error1.get("code"));
        Assert.assertEquals("name already exists for server at '" + getDefaultApiUrl() + "'", error1.get("message"));
    }

    @Test
    public void createAndList() throws Exception {
        Assert.assertEquals(0, getServers().size());

        // Create a server
        Map server = request()
            .status(200)
            .jwtToken(token)
            .crumb( crumb )
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", getDefaultApiUrl()
            ))
            .post("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals(getDefaultApiUrl(), server.get("apiUrl"));

        // Get the list of servers and check that it persisted
        List servers = getServers();
        Assert.assertEquals(1, servers.size());

        server = (Map) servers.get(0);
        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals(getDefaultApiUrl(), server.get("apiUrl"));

        // Load the server entry
        server = request()
            .status(200)
            .get("/organizations/jenkins/scm/github-enterprise/servers/" + Hashing.sha256().hashString(getDefaultApiUrl(), Charsets.UTF_8).toString() + "/")
            .build(Map.class);
        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals(getDefaultApiUrl(), server.get("apiUrl"));

        Map resp = request()
            .status(404)
            .get("/organizations/jenkins/scm/github-enterprise/servers/hello%20vivek")
            .build(Map.class);

        Assert.assertNotNull(resp);
        Assert.assertEquals(404, resp.get("code"));
    }

    private String getApiUrl() {
        return "http://localhost:" + wireMockRule.port();
    }

    private String getDefaultApiUrl() {
        return getApiUrlCustomPath("/api/v3");
    }

    private String getApiUrlCustomPath(String path) {
        return getApiUrl() +
            (path.startsWith("/") ?
                path : "/" + path);
    }

    private void validGithubServer(boolean hasHeader) throws Exception {
        MappingBuilder mappingBuilder = WireMock.get(urlEqualTo("/"));
        if (hasHeader) {
            stubFor(mappingBuilder.willReturn(ok().withHeader("X-GitHub-Request-Id", "foobar")));
        } else {
            stubFor(mappingBuilder.willReturn(ok()));
        }
    }
    private List getServers() {
        return request()
            .status(200)
            .jwtToken(token)
            .get("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(List.class);
    }
}
