package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketWireMockBase;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * @author Vivek Pandey
 */
public class BbCloudWireMock extends BitbucketWireMockBase {

    @Rule
    public WireMockRule bitbucketApi = new WireMockRule(wireMockConfig().
            dynamicPort().dynamicHttpsPort()
            .usingFilesUnderClasspath("api/cloud")
    );

    @Override
    protected WireMockRule getWireMockRule() {
        return bitbucketApi;
    }

    @Override
    protected String wireMockFileSystemPath() {
        return "src/test/resources/api/cloud/";
    }

    @Override
    protected String wireMockProxyUrl() {
        return "https://bitbucket.org";
    }

    @Override
    protected String getUserName() {
        return "vivekp7";
    }

    @Override
    protected String getPassword() {
        return "abcd";
    }
}
