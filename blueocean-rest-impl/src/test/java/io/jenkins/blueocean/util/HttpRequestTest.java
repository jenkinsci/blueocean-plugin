package io.jenkins.blueocean.util;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * @author cliffmeyers
 */
@RunWith(JUnit4.class)
public class HttpRequestTest {

    @Rule
    public WireMockRule rule = createWireMockServerRule("wiremock/httprequest");

    private static WireMockRule createWireMockServerRule(String resourceFolder) {
        final WireMockRule rule = new WireMockRule(
            wireMockConfig()
                .dynamicPort()
                .usingFilesUnderClasspath(resourceFolder)
        );

        String mappingsPath = String.format("src/test/resources/%s/mappings", resourceFolder);
        String filesPath = String.format("src/test/resources/%s/__files", resourceFolder);

        new File(mappingsPath).mkdirs();
        new File(filesPath).mkdirs();

        rule.enableRecordMappings(
            new SingleRootFileSource(mappingsPath),
            new SingleRootFileSource(filesPath)
        );

        return rule;
    }

    private HttpRequest request;

    @Before
    public void setUp() {
        request = HttpRequest.build(String.format("http://%s:%s", "localhost", rule.port()));
    }

    @Test
    public void testGet404() throws IOException {
        String urlPath = "/test/get/404";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(404)));

        request.GET(urlPath)
            .status(404)
            .execute();
    }

    @Test
    public void testGetJson() throws IOException {
        String urlPath = "/test/parse/json";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withBodyFile("body-organizations-jenkins-BiWX8.json")));

        Map map = request.GET(urlPath)
            .asObject(Map.class);

        Assert.assertNotNull(map);
        Assert.assertEquals("jenkins", map.get("name"));
        Assert.assertNotNull(map.get("_links"));
    }

    @Test
    public void testNonMatchingStatusThrowsException() throws IOException {
        String urlPath = "/test/status/mismatch";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(403)));

        try {
            request.GET(urlPath).execute();
            Assert.fail("should not succeed");
        } catch (Exception ex) {
            Assert.assertTrue("should contain 403", ex.getMessage().contains("403"));
        }
    }

    @Test
    public void testSendBasicAuthenticationHeaderSent() throws IOException {
        String urlPath = "/test/basic/auth";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200)));

        request.GET(urlPath)
            .auth("user1", "user1")
            .execute();

        verify(getRequestedFor(urlEqualTo(urlPath))
            .withHeader("Authorization", containing("Basic")));
    }

    @Test
    public void testSendJsonBody() throws IOException {
        String urlPath = "/test/send/json/body";

        stubFor(post(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200)));

        request.POST(urlPath)
            .bodyJson(ImmutableMap.of("foo", "bar"))
            .execute();

        verify(postRequestedFor(urlEqualTo(urlPath))
            .withRequestBody(containing("\"foo\" : \"bar\"")));
    }

    @Test
    public void testRequestHeader() throws IOException {
        String urlPath = "/test/send/request/header";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200)));

        request.GET(urlPath)
            .header("Foo", "Baz")
            .execute();

        verify(getRequestedFor(urlEqualTo(urlPath))
            .withHeader("Foo", equalTo("Baz")));
    }

    @Test
    public void testUriTemplating() throws IOException {
        String templatedPath = "/test/uri/templating/{fname}/{lname}";
        String urlPath = "/test/uri/templating/londo/mollari";
        String expectedBody = "spoo!";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200).withBody(expectedBody)));

        String actualBody = request.GET(templatedPath)
            .urlPart("fname", "londo")
            .urlPart("lname", "mollari")
            .asText();

        Assert.assertEquals(expectedBody, actualBody);
    }

}
