package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class GithubServerTest extends PipelineBaseTest {

    String token;

    @Before
    public void createUser() throws UnirestException {
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        token = getJwtToken(j.jenkins, "alice", "alice");
    }

    @Test
    public void testMissingParams() throws Exception {
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .data(ImmutableMap.of())
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
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
            .data(ImmutableMap.of("name", "foo"))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
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
            .data(ImmutableMap.of("apiUrl", "http://google.com/"))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
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
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", "http://github.example.com/"
            ))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        // Create a server
        Map resp = server = request()
            .status(400)
            .jwtToken(token)
            .data(ImmutableMap.of(
                "name", "My Server 2",
                "apiUrl", "http://github.example.com/"
            ))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
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
        Map server = request()
            .status(200)
            .jwtToken(token)
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", "http://github.example.com/"
            ))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", "http://github.corp.example.com/"
            ))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        List errors = (List) resp.get("errors");
        Assert.assertEquals(1, errors.size());

        Map error1 = (Map) errors.get(0);
        Assert.assertEquals("name", error1.get("field"));
        Assert.assertEquals("ALREADY_EXISTS", error1.get("code"));
        Assert.assertEquals("name already exists for server at 'http://github.example.com/'", error1.get("message"));
    }

    @Test
    public void createListDelete() throws Exception {
        Assert.assertEquals(0, getServers().size());

        // Create a server
        Map server = request()
            .status(200)
            .jwtToken(token)
            .data(ImmutableMap.of(
                "name", "My Server",
                "apiUrl", "http://github.example.com/"
            ))
            .put("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(Map.class);

        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals("http://github.example.com/", server.get("apiUrl"));

        // Get the list of servers and check that it persisted
        List servers = getServers();
        Assert.assertEquals(1, servers.size());

        server = (Map) servers.get(0);
        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals("http://github.example.com/", server.get("apiUrl"));

        // Load the server entry
        server = request()
            .status(200)
            .get("/organizations/jenkins/scm/github-enterprise/servers/My%20Server")
            .build(Map.class);
        Assert.assertEquals("My Server", server.get("name"));
        Assert.assertEquals("http://github.example.com/", server.get("apiUrl"));

        Map resp = request()
            .status(404)
            .get("/organizations/jenkins/scm/github-enterprise/servers/hello%20vivek")
            .build(Map.class);

        Assert.assertNotNull(resp);
        Assert.assertEquals(404, resp.get("code"));
    }

    private List getServers() {
        return request()
            .status(200)
            .jwtToken(token)
            .get("/organizations/jenkins/scm/github-enterprise/servers/")
            .build(List.class);
    }
}
