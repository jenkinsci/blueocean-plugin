package io.jenkins.blueocean.ssh;

import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.UserRoute;
import io.jenkins.blueocean.rest.model.BlueUser;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.GET;

/**
 * Route to handle user personal Jenkins-managed key requests
 */
@Extension
public class UserPublicKeyRoute implements UserRoute {
    @Override
    public String getUrlName() {
        return "publickey";
    }

    @Override
    public Object get(BlueUser user) {
        return new Handler(user);
    }

    public static class Handler {
        final BlueUser user;

        public Handler(BlueUser user) {
            this.user = user;
        }

        /**
         * Gets or creates the user's private Jenkins-managed key and returns the
         * public key to the user
         *
         * @return JSON response
         */
        @GET
        @WebMethod(name = "")
        @TreeResponse
        public UserKey getPublickey() {
            User authenticatedUser = User.current();
            if (authenticatedUser == null) {
                throw new ServiceException.UnauthorizedException("Not authorized");
            }
            if (!StringUtils.equals(user.getId(), authenticatedUser.getId())) {
                throw new ServiceException.ForbiddenException("Not authorized");
            }

            UserKey publicKey = UserSSHKeyManager.getPublicKey(authenticatedUser,
                UserSSHKeyManager.getOrCreate(authenticatedUser));

            return publicKey;
        }

        /**
         * Deletes the user's private Jenkins-managed key
         *
         * @return
         */
        @DELETE
        @WebMethod(name = "")
        @TreeResponse
        public UserKey resetPublicKey() {
            User authenticatedUser = User.current();
            if (authenticatedUser == null) {
                throw new ServiceException.UnauthorizedException("Not authorized");
            }
            if (!StringUtils.equals(user.getId(), authenticatedUser.getId())) {
                throw new ServiceException.ForbiddenException("Not authorized");
            }

            UserSSHKeyManager.reset(authenticatedUser);
            return getPublickey();
        }
    }
}
