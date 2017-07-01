package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpointTest extends PipelineBaseTest {
    private static final String URL = "/organizations/jenkins/scm/bitbucket-server/servers/";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().
            dynamicPort());

    String token;

    @Before
    public void createUser() throws UnirestException, IOException {
        User user = login();
        token = getJwtToken(j.jenkins, user.getId(), user.getId());
    }

    @Test
    public void testServerNotBitbucket() throws Exception {
        validBitbucketServer(false);

        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        List errors = (List) resp.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("apiUrl", error1.get("field"));
        Assert.assertNotNull(error1.get("message"));
        assertEquals("INVALID", error1.get("code"));
    }

    @Test
    public void testServerBadServer() throws Exception {
        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", "http://foobar/"
                ))
                .post(URL)
                .build(Map.class);

        List errors = (List) resp.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("apiUrl", error1.get("field"));
        assertNotNull(error1.get("message"));
        assertEquals("INVALID", error1.get("code"));
    }

    @Test
    public void testMissingParams() throws Exception {
        validBitbucketServer(true);
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of())
                .post(URL)
                .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        assertEquals(2, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("name", error1.get("field"));

        Map error2 = (Map) errors.get(1);
        assertEquals("apiUrl", error2.get("field"));
    }

    @Test
    public void testMissingUrlParam() throws Exception {
        validBitbucketServer(true);
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of("name", "foo"))
                .post(URL)
                .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("apiUrl", error1.get("field"));
        assertEquals("MISSING", error1.get("code"));
        assertNotNull(error1.get("message"));
    }

    @Test
    public void testMissingNameParam() throws Exception {
        validBitbucketServer(true);
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of("apiUrl", getApiUrl()))
                .post(URL)
                .build(Map.class);
        Assert.assertNotNull(resp);

        List errors = (List) resp.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("name", error1.get("field"));
        assertEquals("MISSING", error1.get("code"));
        assertNotNull(error1.get("message"));
    }

    @Test
    public void avoidDuplicateByUrl() throws Exception {
        validBitbucketServer(true);
        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        // Create a server
        Map resp = server = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server 2",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        List errors = (List) resp.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("apiUrl", error1.get("field"));
        assertEquals("ALREADY_EXISTS", error1.get("code"));
        assertNotNull(error1.get("message"));
    }

    @Test
    public void avoidDuplicateByName() throws Exception {
        validBitbucketServer(true);
        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        List errors = (List) resp.get("errors");
        assertEquals(2, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("name", error1.get("field"));
        assertEquals("ALREADY_EXISTS", error1.get("code"));
        assertNotNull(error1.get("message"));
    }

    @Test
    public void createAndList() throws Exception {
        validBitbucketServer(true);
        assertEquals(1, getServers().size()); //there is always bitbucket cloud endpoint

        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", getApiUrl()
                ))
                .post(URL)
                .build(Map.class);

        assertEquals("My Server", server.get("name"));
        assertEquals(getApiUrl(), server.get("apiUrl"));

        // Get the list of servers and check that it persisted
        List servers = getServers();
        assertEquals(2, servers.size());

        server = (Map) servers.get(1);
        assertEquals("My Server", server.get("name"));
        assertEquals(getApiUrl(), server.get("apiUrl"));
    }

    private String getApiUrl() {
        return "http://localhost:" + wireMockRule.port();
    }

    private void validBitbucketServer(boolean hasHeader) throws Exception {
        MappingBuilder mappingBuilder = WireMock.get(urlEqualTo("/"));
        if (hasHeader) {
            stubFor(mappingBuilder.willReturn(ok().withHeader("X-AREQUESTID", "foobar")));
        } else {
            stubFor(mappingBuilder.willReturn(ok()));
        }
    }

    private List getServers() {
        return request()
                .status(200)
                .jwtToken(token)
                .get(URL)
                .build(List.class);
    }
}
