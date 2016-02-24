package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import io.jenkins.blueocean.api.profile.CreateUserRequest;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.sandbox.User;
import io.jenkins.blueocean.rest.sandbox.UserContainer;

/**
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class UserContainerImpl extends UserContainer {
    @Override
    public User create(@JsonBody CreateUserRequest req) {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @Override
    public User get(String name) {
        hudson.model.User user = hudson.model.User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(user);
    }
}
