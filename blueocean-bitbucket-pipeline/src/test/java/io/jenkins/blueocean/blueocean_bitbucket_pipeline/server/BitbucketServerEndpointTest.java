package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.util.HttpRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BitbucketServerApi.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
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
    public void testServerNotBitbucket() throws Exception {
        PowerMockito.mockStatic(BitbucketServerApi.class);
        PowerMockito.when(BitbucketServerApi.getVersion(apiUrl))
                .thenThrow(new ServiceException.NotFoundException("Not found"));
        // Create a server
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
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
                .crumb( crumb )
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
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
        Map resp = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
                .data(ImmutableMap.of("apiUrl", apiUrl))
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
                .crumb( crumb )
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
                ))
                .post(URL)
                .build(Map.class);
        assertEquals(apiUrl, server.get("apiUrl"));

        // Create a server
        Map resp = server = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
                .data(ImmutableMap.of(
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
                .crumb( crumb )
                .data(ImmutableMap.of(
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

    @Test
    public void shouldFailOnIncompatibleVersionInAdd() throws UnirestException, IOException {
        PowerMockito.mockStatic(BitbucketServerApi.class);
        PowerMockito.when(BitbucketServerApi.getVersion(apiUrl))
                .thenReturn("5.0.2");

        Map server = request()
                .status(400)
                .jwtToken(token)
                .crumb( crumb )
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
                ))
                .post(URL)
                .build(Map.class);

        List errors = (List) server.get("errors");
        assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        assertEquals("apiUrl", error1.get("field"));
        assertEquals("INVALID", error1.get("code"));
        assertNotNull(error1.get("message"));
        assertEquals("This Bitbucket Server is too old (5.0.2) to work with Jenkins. Please upgrade Bitbucket to 5.2.0 or later.", error1.get("message"));
    }

    @Test
    public void shouldFailOnIncompatibleVersion() throws UnirestException, IOException {
        // Create a server
        Map server = request()
                .status(200)
                .jwtToken(token)
                .crumb( crumb )
                .data(ImmutableMap.of(
                        "name", "My Server",
                        "apiUrl", apiUrl
                ))
                .post(URL)
                .build(Map.class);

        String id = DigestUtils.sha256Hex(apiUrl);
        assertEquals(id, server.get("id"));
        assertEquals("My Server", server.get("name"));
        assertEquals(apiUrl, server.get("apiUrl"));

        PowerMockito.mockStatic(BitbucketServerApi.class);
        PowerMockito.when(BitbucketServerApi.getVersion(apiUrl))
                .thenReturn("5.0.2");

        Map r = new RequestBuilder(baseUrl)
                .status(428)
                .jwtToken(token)
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/servers/"+id+"/validate/")
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
            .bodyJson(ImmutableMap.of(
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
                .crumb( crumb )
                .jwtToken(token)
                .get(URL)
                .build(List.class);
    }

    private HttpRequest httpRequest() {
        return new HttpRequest(baseUrl)
            .header("Authorization", "Bearer "+token);
    }
}
