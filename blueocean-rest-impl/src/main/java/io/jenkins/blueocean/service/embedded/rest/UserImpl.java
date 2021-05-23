package io.jenkins.blueocean.service.embedded.rest;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.tasks.Mailer;
import hudson.tasks.UserAvatarResolver;
import io.jenkins.blueocean.commons.ServiceException.ForbiddenException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.UserRoute;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserPermission;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * {@link BlueUser} implementation backed by in-memory {@link User}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class UserImpl extends BlueUser {
    private static final String CREDENTIAL_CREATE_PERMISSION = CredentialsProvider.CREATE.name.toLowerCase();
    private static final String CREDENTIAL_VIEW_PERMISSION = CredentialsProvider.VIEW.name.toLowerCase();
    private static final String CREDENTIAL_DELETE_PERMISSION = CredentialsProvider.DELETE.name.toLowerCase();
    private static final String CREDENTIAL_UPDATE_PERMISSION = CredentialsProvider.UPDATE.name.toLowerCase();
    private static final String CREDENTIAL_MANAGE_DOMAINS_PERMISSION = StringUtils.uncapitalize(CredentialsProvider.MANAGE_DOMAINS.name);

    protected final User user;

    private final BlueOrganization organization;
    private final AccessControlled organizationBase;

    private final Reachable parent;

    public UserImpl(@NonNull BlueOrganization organization, @NonNull User user, Reachable parent) {
        this.parent = parent;
        this.user = user;
        this.organization = organization;
        organizationBase = getAccessControllerOrganization();
    }

    public UserImpl(@NonNull BlueOrganization organization, @NonNull User user) {
        this(organization, user, null);
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
        if(isAnonymous(name)){
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

        /*
         * Get the user id using authenticated user. User.current() returns authenticated user using security realm and
         * associated IdStrategy to get a consistent id.
         *
         * @see IdStrategy#keyFor(String)
         * @see IdStrategy.CaseInsensitive#keyFor(String)
         *
         */
        User u = User.current();
        String expectedUserId = u != null ? u.getId(): Jenkins.ANONYMOUS.getName();

        if(!user.getId().equals(expectedUserId)) {
            throw new ForbiddenException("This user '" + expectedUserId + "' cannot access resource owned by '" + user.getId() + "'");
        }
        return new FavoriteContainerImpl(this, this);
    }

    @Override
    public BlueUserPermission getPermission() {
        Authentication authentication = Jenkins.getAuthentication();
        String name = authentication.getName();
        if(isAnonymous(name)){
            return null;
        }

        User loggedInUser = User.get(name, false, Collections.EMPTY_MAP);
        if(loggedInUser == null){
            return null;
        }

        // If this user is not logged in, we do not show it's permissions
        // XXX: This is done to avoid impersonation which has performance
        //      implications, e.g. github oauth plugin might do a network
        //      round trip to fetch user and authorizations
        if(!loggedInUser.getId().equals(user.getId())){
            return null;
        }

        return new BlueUserPermission() {
            @Override
            public boolean isAdministration() {
                return isAdmin();
            }

            @Override
            public Map<String, Boolean> getPipelinePermission() {
                return UserImpl.this.getPipelinePermissions();
            }

            @Override
            public Map<String, Boolean> getCredentialPermission() {
                return UserImpl.this.getCredentialPermissions();
            }
        };
    }

    @Override
    public Link getLink() {
        return (parent != null)?parent.getLink().rel(getId()): ApiHead.INSTANCE().getLink().rel("users/"+getId());
    }

    /**
     * Give plugins chance to handle this API route.
     *
     * @param route URL path that needs handling. e.g. for requested url /rest/organizations/:id/users/:user/xyz,  route param value will be 'xyz'
     * @return stapler object that can handle give route. Could be null
     */
    public Object getDynamic(String route){
        // Try to find any UserRoutes
        for(UserRoute userRoute: ExtensionList.lookup(UserRoute.class)){
            if(userRoute.getUrlName() != null && userRoute.getUrlName().equals(route)){
                return userRoute.get(this);
            }
        }
        return null;
    }

    private boolean isAdmin(){
        return Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER);
    }

    private Map<String, Boolean> getPipelinePermissions(){
        return ImmutableMap.of(
                               BluePipeline.CREATE_PERMISSION, organizationBase.hasPermission(Item.CREATE),
                               BluePipeline.READ_PERMISSION, organizationBase.hasPermission(Item.READ),
                               BluePipeline.START_PERMISSION, organizationBase.hasPermission(Item.BUILD),
                               BluePipeline.STOP_PERMISSION, organizationBase.hasPermission(Item.CANCEL),
                               BluePipeline.CONFIGURE_PERMISSION, organizationBase.hasPermission(Item.CONFIGURE)
        );
    }

    private Map<String, Boolean> getCredentialPermissions(){
        return ImmutableMap.of(
                               CREDENTIAL_CREATE_PERMISSION, organizationBase.hasPermission(CredentialsProvider.CREATE),
                               CREDENTIAL_VIEW_PERMISSION, organizationBase.hasPermission(CredentialsProvider.VIEW),
                               CREDENTIAL_DELETE_PERMISSION, organizationBase.hasPermission(CredentialsProvider.DELETE),
                               CREDENTIAL_UPDATE_PERMISSION, organizationBase.hasPermission(CredentialsProvider.UPDATE),
                               CREDENTIAL_MANAGE_DOMAINS_PERMISSION, organizationBase.hasPermission(CredentialsProvider.MANAGE_DOMAINS)
        );
    }

    private boolean isAnonymous(String name){
        return name.equals("anonymous") || user.getId().equals("anonymous");
    }

    private AccessControlled getAccessControllerOrganization() {
        AccessControlled orgBase = Jenkins.getInstance();

        if (organization instanceof AbstractOrganization) {
            ModifiableTopLevelItemGroup group = ((AbstractOrganization) organization).getGroup();
            if (group instanceof AccessControlled) {
                orgBase = (AccessControlled) group;
            }
        }

        return orgBase;
    }
}
