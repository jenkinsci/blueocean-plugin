package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.tasks.Mailer;
import hudson.tasks.UserAvatarResolver;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserPermission;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link BlueUser} implementation backed by in-memory {@link User}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class UserImpl extends BlueUser {
    protected final User user;

    private final Reachable parent;
    public UserImpl(User user, Reachable parent) {
        this.parent = parent;
        this.user = user;
    }

    public UserImpl(User user) {
        this.user = user;
        this.parent = null;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getFullName() {
        return user.getFullName();
    }

    @Override
    public String getEmail() {
        String name = Jenkins.getAuthentication().getName();
        if(name.equals("anonymous") || user.getId().equals("anonymous")){
            return null;
        }else{
            User user = User.get(name, false, Collections.EMPTY_MAP);
            if(user == null){
                return null;
            }
            if (!user.hasPermission(Jenkins.ADMINISTER)) return null;
        }

        Mailer.UserProperty p = user.getProperty(Mailer.UserProperty.class);
        return p != null ? p.getAddress() : null;
    }

    @Override
    public String getAvatar() {
        return UserAvatarResolver.resolveOrNull(user, "48x48");
    }

    @Override
    public BlueFavoriteContainer getFavorites() {
        String name = Jenkins.getAuthentication().getName();
        if(!user.getId().equals(name)) {
            throw new ServiceException.ForbiddenException("You do not have access to this resource.");
        }
        return new FavoriteContainerImpl(this, this);
    }

    @Override
    public BlueUserPermission getPermission() {
        Authentication authentication = Jenkins.getAuthentication();
        String name = authentication.getName();
        final boolean[] ad = new boolean[1];
        final Map<String,Boolean> pipelinePermission = new HashMap<>();

        if(name.equals(user.getId())){
            ad[0] = isAdmin();
            pipelinePermission.putAll(getPipelinePermissions());
        }else if(!name.equals(user.getId()) && Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
            // authenticated user is different from requested user and authenticated user is admin,
            // we will let it impersonate requested user
            try {
                ACL.impersonate(user.impersonate(), new Runnable() {
                    @Override
                    public void run() {
                        ad[0] = isAdmin();
                        pipelinePermission.putAll(getPipelinePermissions());
                    }
                });
            }catch (UsernameNotFoundException e){
                return null;
            }
        }else{ //different than the logged in user and logged in user is not admin
            return null;
        }

        return new BlueUserPermission() {
            @Override
            public boolean isAdministration() {
                return ad[0];
            }

            @Override
            public Map<String, Boolean> getPipelinePermission() {
                return pipelinePermission;
            }
        };
    }

    @Override
    public Link getLink() {
        return (parent != null)?parent.getLink().rel(getId()): ApiHead.INSTANCE().getLink().rel("users/"+getId());
    }

    private boolean isAdmin(){
        return Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER);
    }

    private Map<String, Boolean> getPipelinePermissions(){
        return ImmutableMap.of(
                BluePipeline.CREATE_PERMISSION, Jenkins.getInstance().hasPermission(Item.CREATE),
                BluePipeline.READ_PERMISSION, Jenkins.getInstance().hasPermission(Item.READ),
                BluePipeline.START_PERMISSION, Jenkins.getInstance().hasPermission(Item.BUILD),
                BluePipeline.STOP_PERMISSION, Jenkins.getInstance().hasPermission(Item.CANCEL)
        );
    }

}
