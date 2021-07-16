package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.model.User;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractScm extends Scm {
    /**
     * Gives authenticated user
     * @return logged in {@link User}
     * @throws ServiceException.UnauthorizedException
     */
    protected User getAuthenticatedUser(){
        User authenticatedUser = User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }
        return authenticatedUser;
    }

    protected HttpResponse createResponse(final String credentialId) {
        return (req, rsp, node ) -> {
            rsp.setStatus(200);
            rsp.getWriter().print(JsonConverter.toJson( MapsHelper.of("credentialId", credentialId)));
        };
    }

    protected static @CheckForNull String getCredentialIdFromRequest(@Nonnull StaplerRequest request){
        String credentialId = request.getParameter(CREDENTIAL_ID);

        if(credentialId == null){
            credentialId = request.getHeader(X_CREDENTIAL_ID);
        }
        return credentialId;
    }
}
