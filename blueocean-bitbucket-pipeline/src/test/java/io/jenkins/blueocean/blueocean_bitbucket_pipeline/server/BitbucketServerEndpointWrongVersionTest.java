package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.commons.MapsHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpointWrongVersionTest extends BbServerWireMock {

    private static final String URL = "/organizations/jenkins/scm/bitbucket-server/servers/";

    String token;

    @Rule
    public WireMockRule bitbucketApi = new WireMockRule(wireMockConfig().
        dynamicPort().dynamicHttpsPort()
        .usingFilesUnderClasspath("api/server_wrong_version")
    );

    @Override
    protected WireMockRule getWireMockRule() {
        return bitbucketApi;
    }

    protected String wireMockFileSystemPath() {
        return "src/test/resources/api/server_wrong_version/";
    }

    @Before
    public void setup() throws Exception {
        super.setup();
        token = getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId());
        this.crumb = getCrumb( j.jenkins );
    }


    @Test
    public void testServerNotBitbucket() throws Exception {

        // Create a server
        Map resp = request()
            .status(400)
            .jwtToken(token)
            .crumb(crumb)
            .data(MapsHelper.of(
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
    public void shouldFailOnIncompatibleVersionInAdd() throws UnirestException, IOException {

        Map server = request()
            .status(400)
            .jwtToken(token)
            .crumb(crumb)
            .data(MapsHelper.of(
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
        assertEquals("This Bitbucket Server is too old (5.0.0) to work with Jenkins. Please upgrade Bitbucket to 5.2.0 or later.", error1.get("message"));

    }

}
