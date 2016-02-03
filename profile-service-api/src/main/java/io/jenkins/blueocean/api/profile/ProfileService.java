package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.api.profile.model.Organization;
import io.jenkins.blueocean.api.profile.model.User;

import javax.annotation.Nonnull;

/**
 * Profile service API. This API manages user and organization.
 *
 * @author Vivek Pandey
 */
public interface ProfileService {
    /**
     * Givers {@link User}
     *
     * @param request {@link GetUserRequest} instance
     *
     * @return {@link GetUserResponse} instance
     */
    public @Nonnull GetUserResponse getUser(@Nonnull GetUserRequest request);

    /**
     *  Gives {@link io.jenkins.blueocean.api.profile.model.UserDetails}
     *
     * @param request {@link }GetUserDetailsRequest} instance
     * @return {@link GetUserDetailsResponse} instance
     */
    public @Nonnull GetUserDetailsResponse getUserDetails(@Nonnull GetUserDetailsRequest request);

    /**
     * Gives {@link Organization}
     *
     * @param request {@link GetOrganizationRequest} instance
     * @return {@link GetOrganizationResponse} instance
     */
    public @Nonnull GetOrganizationResponse getOrganization(@Nonnull GetOrganizationRequest request);

    /**
     * Gives {@link FindUsersResponse}
     *
     * @param request {@link FindUsersRequest} instance
     * @return {@link FindUsersResponse} instance
     */
    public @Nonnull FindUsersResponse findUsers(@Nonnull FindUsersRequest request);

}
