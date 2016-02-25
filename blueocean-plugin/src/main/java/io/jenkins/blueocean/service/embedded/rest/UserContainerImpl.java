package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.sandbox.BOUser;
import io.jenkins.blueocean.rest.sandbox.BOUserContainer;

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
        hudson.model.User user = hudson.model.User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(user);
    }

    /**
     * Iterates all the users in the system
     */
    @Override
    public Iterator<BOUser> iterator() {
        return new AdaptedIterator<hudson.model.User, BOUser>(hudson.model.User.getAll()) {
            @Override
            protected BOUser adapt(hudson.model.User item) {
                return new UserImpl(item);
            }
        };
    }
}
