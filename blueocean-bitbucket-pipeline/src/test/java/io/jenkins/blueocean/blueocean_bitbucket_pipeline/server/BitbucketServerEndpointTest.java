package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.util.HttpRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpointTest extends BbServerWireMock {
    private static final String URL = "/organizations/jenkins/scm/bitbucket-server/servers/";

    String token;

    @Before
    public void setup() throws Exception {
        super.setup();
        token = getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId());
        this.crumb = getCrumb( j.jenkins );
    }

    @Test
    public void testServerBadServer() throws Exception {
        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of(
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb(crumb)
                .data(new HashMap())
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of( "name", "foo"))
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of( "apiUrl", apiUrl))
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
        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
                ))
                .post(URL)
                .build(Map.class);
        assertEquals(apiUrl, server.get("apiUrl"));

        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of(
                        "name", "My Server 2",
                        "apiUrl", apiUrl
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
    public void createAndList() throws Exception {
        assertEquals(0, getServers().size());

        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .crumb(crumb)
                .data(MapsHelper.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
                ))
                .post(URL)
                .build(Map.class);

        String id = DigestUtils.sha256Hex(apiUrl);
        assertEquals(id, server.get("id"));
        assertEquals("My Server", server.get("name"));
        assertEquals(apiUrl, server.get("apiUrl"));

        // Get the list of servers and check that it persisted
        List servers = getServers();
        assertEquals(1, servers.size());

        server = (Map) servers.get(0);
        assertEquals(id, server.get("id"));
        assertEquals("My Server", server.get("name"));
        assertEquals(apiUrl, server.get("apiUrl"));

        //get this particular endpoint
        server = request()
                .status(200)
                .jwtToken(token)
                .get(URL+id)
                .build(Map.class);

        assertEquals(id, server.get("id"));
        assertEquals("My Server", server.get("name"));
        assertEquals(apiUrl, server.get("apiUrl"));

        //check if valid version
        request()
                .status(200)
                .jwtToken(token)
                .get(URL+id+"/validate/")
                .build(Map.class);
    }

    // TODO: need a test case as unprivileged user

    @Test
    public void createThenDelete() throws IOException {
        String serverId = DigestUtils.sha256Hex(apiUrl);

        httpRequest()
            .Get(URL+serverId+"/")
            .status(404)
            .as(Void.class);

        httpRequest().Post(URL)
            .header( crumb.field, crumb.value )
            .bodyJson(MapsHelper.of(
                "name", "My Server",
                "apiUrl", apiUrl
            ))
            .as(Map.class);

        Map map = httpRequest()
            .Get(URL+serverId+"/")
            .as(Map.class);

        Assert.assertEquals(serverId, map.get("id"));

        httpRequest()
            .Delete(URL+serverId+"/")
            .status(204)
            .as(Void.class);

        httpRequest()
            .Get(URL+serverId+"/")
            .status(404)
            .as(Void.class);
    }

    @Test
    public void should404OnDeleteNonexistent() throws IOException {
        String serverId = DigestUtils.sha256Hex(apiUrl);

        httpRequest()
            .Get(URL+serverId+"/")
            .status(404)
            .as(Void.class);

        httpRequest()
            .Delete(URL+serverId+"/")
            .status(404)
            .as(Void.class);
    }

    private List getServers() {
        return request()
                .status(200)
                .crumb(crumb)
                .jwtToken(token)
                .get(URL)
                .build(List.class);
    }

    private HttpRequest httpRequest() {
        return new HttpRequest(baseUrl)
            .header("Authorization", "Bearer "+token);
    }
}
