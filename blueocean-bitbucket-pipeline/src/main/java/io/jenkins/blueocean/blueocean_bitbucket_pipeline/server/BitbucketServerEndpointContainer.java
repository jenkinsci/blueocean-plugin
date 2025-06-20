package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpointProvider;
import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.EndpointType;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.Messages;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
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

        try {
            Jenkins.get().checkPermission(Item.CREATE);
        } catch (Exception e) {
            throw new ServiceException.ForbiddenException("User does not have permission to create repository", e);
        }

        List<ErrorMessage.Error> errors = new LinkedList<>();

        // Validate name
        final String name = (String) request.get(ScmServerEndpoint.NAME);
        if(StringUtils.isBlank(name)){
            errors.add(new ErrorMessage.Error(ScmServerEndpoint.NAME, ErrorMessage.Error.ErrorCodes.MISSING.toString(), ScmServerEndpoint.NAME + " is required"));
        }

        String url = (String) request.get(ScmServerEndpoint.API_URL);
        if(StringUtils.isBlank(url)){
            errors.add(new ErrorMessage.Error(ScmServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.MISSING.toString(), ScmServerEndpoint.API_URL + " is required"));
        }else {
            try {
                String version = BitbucketServerApi.getVersion(url);
                if (!BitbucketServerApi.isSupportedVersion(version)) {
                    errors.add(new ErrorMessage.Error(BitbucketServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                            Messages.bbserver_version_validation_error(
                                    version, BitbucketServerApi.MINIMUM_SUPPORTED_VERSION)));
                } else {
                    //validate presence of endpoint with same name
                    if (BitbucketEndpointProvider.lookupEndpoint(url).isPresent()) {
                        errors.add(new ErrorMessage.Error(ScmServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), ScmServerEndpoint.API_URL + " already exists"));
                    }
                }
            } catch (ServiceException e) {
                errors.add(new ErrorMessage.Error(BitbucketServerEndpoint.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), StringUtils.isBlank(e.getMessage()) ? "Invalid URL" : e.getMessage()));
            }
        }

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to create Bitbucket server endpoint").addAll(errors));
        }
        BitbucketEndpoint endpoint;
        try (ACLContext ctx = ACL.as2(ACL.SYSTEM2)) {
            // We need to escalate privilege to add user defined endpoint to
            endpoint = BitbucketEndpointProvider.registerEndpoint(name, url, null);
        }
        return new BitbucketServerEndpoint(endpoint, this);
    }

    @Override
    public ScmServerEndpoint get(String id) {
        for(BitbucketEndpoint endpoint: BitbucketEndpointProvider.all()){
            if(id.equals(DigestUtils.sha256Hex(endpoint.getServerURL()))){
                return new BitbucketServerEndpoint(endpoint, this);
            }
        }
        return null;
    }

    @Override
    public Iterator<ScmServerEndpoint> iterator() {
        return BitbucketEndpointProvider.lookupEndpoint(EndpointType.SERVER).stream()
            .map(bitbucketServerEndpoint -> (ScmServerEndpoint) new BitbucketServerEndpoint( bitbucketServerEndpoint,
                                                                    BitbucketServerEndpointContainer.this ) )
            .iterator();
    }
}
