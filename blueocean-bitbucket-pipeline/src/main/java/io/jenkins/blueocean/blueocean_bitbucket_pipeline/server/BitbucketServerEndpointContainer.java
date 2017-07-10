package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.AbstractBitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpointContainer extends ScmServerEndpointContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketServerEndpointContainer.class);
    private final Link self;

    public BitbucketServerEndpointContainer(Reachable parent) {
        this.self = parent.getLink().rel("servers");
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public ScmServerEndpoint create(JSONObject request) {
        List<ErrorMessage.Error> errors = Lists.newLinkedList();

        final String url = (String) request.get(ScmServerEndpoint.API_URL);
        if(StringUtils.isBlank(url)){
            errors.add(new ErrorMessage.Error(ScmServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.MISSING.toString(), ScmServerEndpoint.API_URL + " is required"));
        }else {
            try {
                Response response = Request.Get(url).execute();
                Header header = response.returnResponse().getFirstHeader("X-AREQUESTID");
                if (header == null || StringUtils.isBlank(header.getValue())) {
                    errors.add(new ErrorMessage.Error(BitbucketServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), "Specified URL is not a Bitbucket server"));
                }
            } catch (IOException e) {
                errors.add(new ErrorMessage.Error(BitbucketServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), "Could not connect to Bitbucket server"));
                LOGGER.error("Could not connect to Bitbucket", e);
            }
        }

        //validate presence of endpoint with same name
        final BitbucketEndpointConfiguration endpointConfiguration = BitbucketEndpointConfiguration.get();

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to create Bitbucket server endpoint").addAll(errors));
        }
        final com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketServerEndpoint endpoint = new com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketServerEndpoint(null, url, false, null);
        SecurityContext old=null;
        try {
            // We need to escalate privilege to add user defined endpoint to
            old = ACL.impersonate(ACL.SYSTEM);
            endpointConfiguration.addEndpoint(endpoint);
        }finally {
            //reset back to original privilege level
            if(old != null){
                SecurityContextHolder.setContext(old);
            }
        }
        return new BitbucketServerEndpoint(endpoint);
    }

    @Override
    public ScmServerEndpoint get(String name) {
        //Bitbucket server endpoint name is not unique so we won't support /scm/{scmId}/servers/{id}
        throw new ServiceException.NotImplementedException("not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<ScmServerEndpoint> iterator() {
        BitbucketEndpointConfiguration endpointConfiguration = BitbucketEndpointConfiguration.get();
        Iterator<com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketServerEndpoint> serverEndpoints =  Iterators
                .filter(endpointConfiguration.getEndpoints().iterator(),
                        com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketServerEndpoint.class);
        return Iterators.transform(serverEndpoints, new Function<AbstractBitbucketEndpoint, ScmServerEndpoint>() {
            @Override
            public ScmServerEndpoint apply(AbstractBitbucketEndpoint input) {
                return new BitbucketServerEndpoint(input);
            }
        });
    }
}
