package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.model.BOUser;
import io.jenkins.blueocean.rest.model.BOUserContainer;

import java.util.Iterator;

/**
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class UserContainerImpl extends BOUserContainer {
    @Override
    public BOUser get(String name) {
        User user = User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(user);
    }

    /**
     * Iterates all the users in the system
     */
    @Override
    public Iterator<BOUser> iterator() {
        return new AdaptedIterator<User, BOUser>(User.getAll()) {
            @Override
            protected BOUser adapt(User item) {
                return new UserImpl(item);
            }
        };
    }
}
