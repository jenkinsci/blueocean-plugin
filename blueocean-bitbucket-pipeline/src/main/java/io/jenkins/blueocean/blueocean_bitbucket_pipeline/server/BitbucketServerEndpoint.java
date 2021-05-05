package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.AbstractBitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.Messages;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.GET;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpoint extends ScmServerEndpoint {
    private final String id;
    private final Link self;
    private final String apiUrl;
    private final String name;

    public BitbucketServerEndpoint(AbstractBitbucketEndpoint endpoint, Reachable parent) {
        this.id = DigestUtils.sha256Hex(endpoint.getServerUrl());
        this.apiUrl = endpoint.getServerUrl();
        this.name = StringUtils.isBlank(endpoint.getDisplayName()) ?  endpoint.getServerUrl() : endpoint.getDisplayName();
        this.self = parent.getLink().rel(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public Link getLink() {
        return self;
    }

    /**
     * Validates this server endpoint. Checks availability and version requirement.
     * @return If valid HttpStatus 200, if unsupported version then 428 and if unreachable then 400 error code is returned.
     */
    @GET
    @WebMethod(name="validate")
    public HttpResponse validate(){
        String version = BitbucketServerApi.getVersion(apiUrl);
        if(!BitbucketServerApi.isSupportedVersion(version)){
            throw new ServiceException.PreconditionRequired(
                    Messages.bbserver_version_validation_error(
                            version, BitbucketServerApi.MINIMUM_SUPPORTED_VERSION));
        }
        return new HttpResponse(){
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(200);
            }
        };
    }

    @WebMethod(name="") @DELETE
    public void doDelete(StaplerResponse resp) {
        final BitbucketEndpointConfiguration config = BitbucketEndpointConfiguration.get();
        config.removeEndpoint(getApiUrl());
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
