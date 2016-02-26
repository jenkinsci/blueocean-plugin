package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserContainer;

import java.util.Iterator;

/**
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class UserContainerImpl extends BlueUserContainer {
    @Override
    public BlueUser get(String name) {
        User user = User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(user);
    }

    /**
     * Iterates all the users in the system
     */
    @Override
    public Iterator<BlueUser> iterator() {
        return new AdaptedIterator<User, BlueUser>(User.getAll()) {
            @Override
            protected BlueUser adapt(User item) {
                return new UserImpl(item);
            }
        };
    }
}
