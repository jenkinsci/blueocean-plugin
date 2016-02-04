package io.jenkins.blueocean.api.profile;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.api.profile.model.Organization;
import io.jenkins.blueocean.api.profile.model.User;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nonnull;

/**
 * Profile service API. This API manages user and organization.
 *
 * @author Vivek Pandey
 */
public interface ProfileService extends ExtensionPoint {
    /**
     * Gives {@link User}
     *
     * @param identity user identity in this context
     * @param request {@link GetUserRequest} instance
     * @return {@link GetUserResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    GetUserResponse getUser(@Nonnull Identity identity, @Nonnull GetUserRequest request);

    /**
     * Gives {@link io.jenkins.blueocean.api.profile.model.UserDetails}
     *
     * @param identity user identity in this context
     * @param request {@link }GetUserDetailsRequest} instance
     * @return {@link GetUserDetailsResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    GetUserDetailsResponse getUserDetails(@Nonnull Identity identity, @Nonnull GetUserDetailsRequest request);

    /**
     * Gives {@link Organization}
     *
     * @param identity user identity in this context
     * @param request {@link GetOrganizationRequest} instance
     * @return {@link GetOrganizationResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    GetOrganizationResponse getOrganization(@Nonnull Identity identity, @Nonnull GetOrganizationRequest request);

    /**
     * Creates a new Organization
     *
     * @param identity user identity in this context
     * @param request {@link CreateOrganizationRequest} instance
     * @return {@link CreateOrganizationResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    CreateOrganizationResponse createOrganization(@Nonnull Identity identity, @Nonnull CreateOrganizationRequest request);

    /**
     * Gives {@link FindUsersResponse}
     *
     * @param identity user identity in this context
     * @param request {@link FindUsersRequest} instance
     * @return {@link FindUsersResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    FindUsersResponse findUsers(@Nonnull Identity identity, @Nonnull FindUsersRequest request);

}
