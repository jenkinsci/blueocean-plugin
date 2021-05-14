package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.util.AdaptedIterator;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
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

    private final Reachable parent;
    private final BlueOrganization organization;

    public UserContainerImpl(@NonNull BlueOrganization organization, @NonNull Reachable parent) {
        this.parent = parent;
        this.organization = organization;
    }

    public UserContainerImpl() {
        this.parent = null;
        this.organization = OrganizationFactory.getInstance().list().iterator().next();
    }

    @Override
    public BlueUser get(String name) {
        User user = User.get(name, false, ImmutableMap.of());
        if (user==null)     return null;
        return new UserImpl(organization, user, this);
    }

    /**
     * Iterates all the users in the system
     */
    @Override
    public Iterator<BlueUser> iterator() {
        return new AdaptedIterator<User, BlueUser>(User.getAll()) {
            @Override
            protected BlueUser adapt(User item) {
                return new UserImpl(organization, item, UserContainerImpl.this);
            }
        };
    }

    @Override
    public Link getLink() {
        if(parent!=null) {
            return parent.getLink().rel(getUrlName());
        }
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }

}
