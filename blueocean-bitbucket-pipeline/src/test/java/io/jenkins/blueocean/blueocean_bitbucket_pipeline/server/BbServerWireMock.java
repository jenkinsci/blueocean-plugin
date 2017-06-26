package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketWireMockBase;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * @author Vivek Pandey
 */
public class BbServerWireMock extends BitbucketWireMockBase {

    @Rule
    public WireMockRule bitbucketApi = new WireMockRule(wireMockConfig().
            dynamicPort().dynamicHttpsPort()
            .usingFilesUnderClasspath("api/server")
    );


    @Override
    protected WireMockRule getWireMockRule() {
        return bitbucketApi;
    }

    @Override
    protected String wireMockFileSystemPath() {
        return "src/test/resources/api/";
    }

    @Override
    protected String wireMockProxyUrl() {
        return "http://localhost:7990";
    }
}
