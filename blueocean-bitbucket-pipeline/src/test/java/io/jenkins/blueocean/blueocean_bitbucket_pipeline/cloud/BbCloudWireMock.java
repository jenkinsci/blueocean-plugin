package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketWireMockBase;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * @author Vivek Pandey
 */
public class BbCloudWireMock extends BitbucketWireMockBase {

    protected static String USER_UUID = "{1c5c9255-d59f-47e2-b5c7-52269c0332b9}";
    protected static String TEAM_UUID = "{47cd7cf2-ca31-4c90-bc0e-4c7ef67f9dfe}";
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
