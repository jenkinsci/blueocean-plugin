package io.blueocean.ath.util;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * @author cliffmeyers
 */
public class WireMockBase {

    // By default the wiremock tests will run without proxy
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy while writing and debugging tests.
    private final static boolean useProxy = !System.getProperty("test.wiremock.useProxy", "false").equals("false");

    private static Logger logger = Logger.getLogger(WireMockBase.class);

    protected static String getServerUrl(WireMockRule rule) {
        return String.format("http://localhost:%s/", rule.port());
    }

    protected static WireMockRule createWireMockServerRule(String resourceFolder, String baseUrl) {
        ReplaceUrlTransformer replaceUrlTransformer = new ReplaceUrlTransformer();

        final WireMockRule rule = new WireMockRule(
            wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .usingFilesUnderClasspath(resourceFolder)
                .extensions(
                    new GzipDecompressTransformer(),
                    replaceUrlTransformer
                )
        );

        String mappingsPath = String.format("src/test/resources/%s/mappings", resourceFolder);
        String filesPath = String.format("src/test/resources/%s/__files", resourceFolder);

        new File(mappingsPath).mkdirs();
        new File(filesPath).mkdirs();

        rule.enableRecordMappings(
            new SingleRootFileSource(mappingsPath),
            new SingleRootFileSource(filesPath)
        );

        if (useProxy) {
            rule.stubFor(
                WireMock.get(urlMatching(".*"))
                    .atPriority(10)
                    .willReturn(aResponse().proxiedFrom(baseUrl)));
        }

        replaceUrlTransformer.configure(baseUrl, rule);

        return rule;
    }

    /**
     * Decompresses the request body if gzipped.
     * In most cases this should be added as the first ResponseTransformer in the "extensions" list.
     */
    static class GzipDecompressTransformer extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            // if gzipped, ungzip they body and discard the Content-Encoding header
            if (response.getHeaders().getHeader("Content-Encoding").containsValue("gzip")) {
                Iterable<HttpHeader> headers = Iterables.filter(
                    response.getHeaders().all(),
                    (HttpHeader header) -> header != null && !header.keyEquals("Content-Encoding") && !header.containsValue("gzip")
                );
                return Response.Builder.like(response)
                    .but()
                    .body(Gzip.unGzip(response.getBody()))
                    .headers(new HttpHeaders(headers))
                    .build();
            }
            return response;
        }

        @Override
        public String getName() {
            return "gzip";
        }
    }

    static class ReplaceUrlTransformer extends ResponseTransformer {

        private String sourceUrl;
        private WireMockRule rule;

        public void configure(String sourceUrl, WireMockRule rule) {
            this.sourceUrl = sourceUrl.endsWith("/") ? sourceUrl : String.format("%s/", sourceUrl);
            this.rule = rule;
        }

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            if ("application/json"
                .equals(response.getHeaders().getContentTypeHeader().mimeTypePart())) {
                return Response.Builder.like(response)
                    .but()
                    .body(response.getBodyAsString().replace(sourceUrl, getServerUrl(rule)))
                    .build();
            }
            return response;
        }

        @Override
        public String getName() {
            return "replace-url";
        }
    }

    static class NoOpLoggingTransformer extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            String url = request.getUrl();
            StringBuilder requestHeaders = new StringBuilder();
            StringBuilder responseHeaders = new StringBuilder();

            for (HttpHeader header : request.getHeaders().all()) {
                requestHeaders.append(String.format("\nrequest header %s %s", header.key(), header.values()));
            }

            for (HttpHeader header : response.getHeaders().all()) {
                responseHeaders.append(String.format("\nresponse header %s %s", header.key(), header.values()));
            }

            logger.info(String.format("----> begin serving %s response fromProxy=%s for url=%s", response.getStatus(), response.isFromProxy(), url));
            logger.info(requestHeaders.toString());
            logger.info(String.format("----> end request headers, begin response headers for url=%s", url));
            logger.info(responseHeaders.toString());
            logger.info("----> end response headers");
            logger.info(String.format("----> begin response body for url=%s", url));
            logger.info(response.getBodyAsString());
            logger.info("----> end response body");

            return response;
        }

        @Override
        public String getName() {
            return "no-op-logging";
        }
    }
}
