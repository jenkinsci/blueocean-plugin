package io.jenkins.blueocean.api.authentication;

import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;

/**
 * Authentication service
 */
public interface AuthenticationService {
    /**
     * Gives {@link AuthenticateResponse}
     *
     * @param identity user identity in this context
     * @param request {@link AuthenticateRequest} instance
     * @return {@link AuthenticateResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException if there is an exception in the server.
     */
    @Nonnull
    AuthenticateResponse authenticate(Identity identity, AuthenticateRequest request);
}
