package io.jenkins.blueocean.util;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.blueocean.commons.MapsHelper;
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
        request = new HttpRequest(getBaseUrl());
    }

    @Test
    public void testGet404() throws IOException {
        String urlPath = "/test/get/404";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(404)));

        request.Get(urlPath)
            .status(404)
            .as(Void.class);
    }

    @Test
    public void testGetJson() throws IOException {
        String urlPath = "/test/parse/json";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withBodyFile("body-organizations-jenkins-BiWX8.json")));

        Map<?,?> map = request.Get(urlPath)
            .as(Map.class);

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
            request.Get(urlPath).as(Void.class);
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

        request.Get(urlPath)
            .auth("user1", "user1")
            .as(Void.class);

        verify(getRequestedFor(urlEqualTo(urlPath))
            .withHeader("Authorization", containing("Basic")));
    }

    @Test
    public void testSendJsonBody() throws IOException {
        String urlPath = "/test/send/json/body";

        stubFor(post(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200)));

        request.Post(urlPath)
            .bodyJson(MapsHelper.of("foo", "bar"))
            .as(Void.class);

        verify(postRequestedFor(urlEqualTo(urlPath))
            .withRequestBody(containing("\"foo\" : \"bar\"")));
    }

    @Test
    public void testRequestHeader() throws IOException {
        String urlPath = "/test/send/request/header";

        stubFor(get(urlEqualTo(urlPath))
            .willReturn(aResponse().withStatus(200)));

        request.Get(urlPath)
            .header("Foo", "Baz")
            .as(Void.class);

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

        String actualBody = request.Get(templatedPath)
            .urlPart("fname", "londo")
            .urlPart("lname", "mollari")
            .asText();

        Assert.assertEquals(expectedBody, actualBody);
    }

    @Test
    public void testNoDefaultBaseUrl() throws IOException {
        String requestPath = "/test/no/baseurl/";
        String requestUrl = getBaseUrl() + requestPath;

        // NOTE: urlEqualTo doesn't appear to work correctly w/ absolute URL's; passing 'requestPath' works
        stubFor(get(urlEqualTo(requestPath))
            .willReturn(aResponse()));

        new HttpRequest()
            .Get(requestUrl)
            .as(Void.class);

        verify(getRequestedFor(urlEqualTo(requestPath)));
    }

    private String getBaseUrl() {
        return String.format("http://%s:%s", "localhost", rule.port());
    }

}
