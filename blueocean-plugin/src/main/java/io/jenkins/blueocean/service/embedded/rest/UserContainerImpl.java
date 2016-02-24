package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.sandbox.User;
import io.jenkins.blueocean.rest.sandbox.UserContainer;

import java.util.Iterator;

/**
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class UserContainerImpl extends UserContainer {
    @Override
    public User get(String name) {
        hudson.model.User user = hudson.model.User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(user);
    }

    /**
     * Iterates all the users in the system
     */
    @Override
    public Iterator<User> iterator() {
        return new AdaptedIterator<hudson.model.User, User>(hudson.model.User.getAll()) {
            @Override
            protected User adapt(hudson.model.User item) {
                return new UserImpl(item);
            }
        };
    }
}
