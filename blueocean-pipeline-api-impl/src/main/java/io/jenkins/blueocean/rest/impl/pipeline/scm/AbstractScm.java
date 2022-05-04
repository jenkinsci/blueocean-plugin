package io.jenkins.blueocean.rest.impl.pipeline.scm;

import hudson.model.Item;
import hudson.model.User;
import hudson.security.AccessControlled;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ListsUtils;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.ModifiableTopLevelItemGroup;
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

    protected static AccessControlled getRootOrgFolder() {
        BlueOrganization organization = ListsUtils.getFirst(OrganizationFactory.getInstance().list(), null);

        if (organization instanceof AbstractOrganization) {
            ModifiableTopLevelItemGroup group = ((AbstractOrganization) organization).getGroup();
            return (AccessControlled) group;
        }

        throw new AssertionError(organization + " is not an instance of AbstractOrganization");
    }

    protected void checkPermission() {
        AccessControlled rootOrgFolder = getRootOrgFolder();
        if (!rootOrgFolder.hasPermission(Item.CREATE)) {
            throw new ServiceException.ForbiddenException("You do not have Job/Create permission");
        }
    }
}
