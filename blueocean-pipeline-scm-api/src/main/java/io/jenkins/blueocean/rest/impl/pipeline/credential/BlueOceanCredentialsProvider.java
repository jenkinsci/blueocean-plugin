package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.pipeline.credential.Messages;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;

/**
 * {@link CredentialsProvider} to serve credentials stored in user store.
 *
 * It works by looking for {@link FolderPropertyImpl} on a folder. This should allow
 * using such user scoped properties with {@link jenkins.branch.OrganizationFolder} or {@link jenkins.branch.MultiBranchProject}
 * or even a Folder.
 *
 * @author Stephen Connoly
 * @author Vivek Pandey
 */
@Extension(ordinal = 99999)
public class BlueOceanCredentialsProvider extends CredentialsProvider {
    private static final BlueOceanDomainRequirement PROXY_REQUIREMENT = new BlueOceanDomainRequirement();
    private static final Logger logger = LoggerFactory.getLogger(BlueOceanCredentialsProvider.class);

    @Nonnull
    @Override
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          @Nonnull ItemGroup itemGroup,
                                                          @Nonnull
                                                              Authentication authentication) {
        return getCredentials(type, itemGroup, authentication, Collections.emptyList());
    }

    private static boolean IsSystemPropertyEnabled() {
        return SystemProperties.getBoolean( BlueOceanCredentialsProvider.class.getName() + ".enabled" );
    }

    @Override
    public boolean isEnabled(Object context)
    {
        return IsSystemPropertyEnabled() && super.isEnabled(context);
    }

    @Nonnull
    public <C extends Credentials> List<C> getCredentials(@Nonnull final Class<C> type,
                                                          @Nullable ItemGroup itemGroup,
                                                          @Nullable
                                                              Authentication authentication,
                                                          @Nonnull List<DomainRequirement> domainRequirements) {
        if (!IsSystemPropertyEnabled()) {
            return Collections.emptyList();
        }
        final List<C> result = new ArrayList<>();
        final FolderPropertyImpl prop = propertyOf(itemGroup);
        if (prop != null && prop.domain.test(domainRequirements)) {
            final User proxyUser = User.get(prop.getUser(), false, Collections.emptyMap());
            if (proxyUser != null) {
                try (ACLContext ignored = ACL.as(proxyUser.impersonate())) {
                    for (CredentialsStore s : CredentialsProvider.lookupStores(proxyUser)) {
                        for (Domain d : s.getDomains()) {
                            if (d.test(PROXY_REQUIREMENT)) {
                                for (Credentials c : filter(s.getCredentials(d), withId(prop.getId()))) {
                                    if (type.isInstance(c)) {
                                        result.add((C) c);
                                    }
                                }
                            }
                        }
                    }
                } catch (UsernameNotFoundException ex) {
                    logger.warn("BlueOceanCredentialsProvider#getCredentials(): Username attached to credentials can not be found");
                }
            }
        }
        return result;
    }

    @Nonnull
    @Override
    public <C extends IdCredentials> ListBoxModel getCredentialIds(@Nonnull Class<C> type,
                                                                   @Nullable ItemGroup itemGroup,
                                                                   @Nullable Authentication authentication,
                                                                   @Nonnull List<DomainRequirement> domainRequirements,
                                                                   @Nonnull CredentialsMatcher matcher) {
        if (!IsSystemPropertyEnabled()) {
            return new ListBoxModel();
        }
        ListBoxModel result = new ListBoxModel();
        FolderPropertyImpl prop = propertyOf(itemGroup);
        if (prop != null && prop.domain.test(domainRequirements)) {
            result.add(Messages.BlueOceanCredentialsProvider_DisplayName(), prop.getId());
        }
        return result;
    }

    @Override
    @Nonnull
    public String getDisplayName() {
        return Messages.BlueOceanCredentialsProvider_DisplayName();
    }

    @Override
    public CredentialsStore getStore(@CheckForNull ModelObject object) {
        if (!IsSystemPropertyEnabled()) {
            return null;
        }
        FolderPropertyImpl property = propertyOf(object);
        return property != null ? property.getStore() : null;
    }

    @Override
    public Set<CredentialsScope> getScopes(ModelObject object) {
        return Collections.singleton(CredentialsScope.GLOBAL);
    }

    private boolean isApplicable(ModelObject object){
        return object instanceof AbstractFolder &&
                ((AbstractFolder)object).getProperties().get(FolderPropertyImpl.class) != null;
    }

    private static FolderPropertyImpl propertyOf(ModelObject object) {
        if (object instanceof AbstractFolder) {
            FolderPropertyImpl prop = ((AbstractFolder<?>)object).getProperties().get(FolderPropertyImpl.class);

            if(prop == null){
                ItemGroup parent = ((AbstractFolder)object).getParent();
                if(parent instanceof Jenkins){
                    return null;
                }
                return propertyOf(parent);
            }else{
                return prop;
            }
        }
        return null;
    }

    public static class FolderPropertyImpl extends AbstractFolderProperty<AbstractFolder<TopLevelItem>> {
        private final Domain domain;
        private final String user;
        private final String id;
        private transient StoreImpl store;

        @DataBoundConstructor
        public FolderPropertyImpl(@Nonnull String user, @Nonnull String id, @Nonnull Domain domain) {
            this.user = user;
            this.id = id;
            this.domain = domain;
        }

        public StoreImpl getStore() {
            if (store == null) {
                // idempotent, don't care about synchronization as they are all the same anyway
                store = new StoreImpl();
            }
            return store;
        }

        public @Nonnull String getUser() {
            return user;
        }

        public @Nonnull String getId() {
            return id;
        }

        public @Nonnull Domain getDomain() {
            return domain;
        }

        @Override
        public AbstractFolderProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws FormException {
            return this;
        }

        @Extension
        public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {
        }

        public static class CredentialsStoreActionImpl extends CredentialsStoreAction {

            private final CredentialsStore store;

            CredentialsStoreActionImpl(CredentialsStore store) {
                this.store = store;
            }

            /**
             * {@inheritDoc}
             */
            @Nonnull
            @Override
            public CredentialsStore getStore() {
                return store;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getIconFileName() {
                return isVisible()
                        ? "/plugin/credentials/images/48x48/folder-store.png"
                        : null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getIconClassName() {
                return isVisible()
                        ? "icon-credentials-folder-store"
                        : null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.BlueOceanCredentialsProvider_DisplayName();
            }

            @Override
            public String getUrlName() {
                return super.getUrlName();
            }
        }

        public class StoreImpl extends CredentialsStore{
            private final CredentialsStoreAction storeAction;

            StoreImpl() {
                super(BlueOceanCredentialsProvider.class);
                this.storeAction = new CredentialsStoreActionImpl(this);
            }

            @Nonnull
            @Override
            public List<Domain> getDomains() {
                return Collections.singletonList(domain);
            }

            @Nullable
            @Override
            public CredentialsStoreAction getStoreAction() {
                return storeAction;
            }

            @Nonnull
            @Override
            public ModelObject getContext() {
                return owner;
            }

            @Override
            public boolean hasPermission(@Nonnull Authentication a, @Nonnull Permission permission) {
                // its read only so for all permissions other than READ, we return false
                if(permission == CREATE || permission == DELETE ||
                        permission == MANAGE_DOMAINS || permission == UPDATE){
                    return false;
                }
                return owner.getACL().hasPermission(a,permission);
            }

            @Nonnull
            @Override
            public List<Credentials> getCredentials(@Nonnull Domain domain) {
                if(!IsSystemPropertyEnabled()) {
                    return Collections.emptyList();
                }
                final List<Credentials> result = new ArrayList<>(1);
                if (domain.equals(FolderPropertyImpl.this.domain)) {
                    final User proxyUser = User.get(getUser(), false, Collections.emptyMap());
                    if (proxyUser != null) {
                        try (ACLContext ignored = ACL.as(proxyUser.impersonate())) {
                            for (CredentialsStore s : CredentialsProvider.lookupStores(proxyUser)) {
                                for (Domain d : s.getDomains()) {
                                    if (d.test(PROXY_REQUIREMENT)) {
                                        result.addAll(filter(s.getCredentials(d), withId(getId())));
                                    }
                                }
                            }
                        } catch (UsernameNotFoundException ex) {
                            logger.warn("BlueOceanCredentialsProvider.StoreImpl#getCredentials(): Username attached to credentials can not be found");
                        }
                    }
                }
                return result;
            }

            @Override
            public boolean addCredentials(@Nonnull Domain domain, @Nonnull Credentials credentials) throws IOException {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public boolean removeCredentials(@Nonnull Domain domain, @Nonnull Credentials credentials) throws IOException {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public boolean updateCredentials(@Nonnull Domain domain, @Nonnull Credentials current,
                                             @Nonnull Credentials replacement) throws IOException {
                throw new UnsupportedOperationException("Not supported");
            }
        }
    }

    /**
     * Creates a domain specific to {@link BlueOceanCredentialsProvider}
     *
     * @param uri repo URL
     * @return {@link Domain} instance
     */
    public static @Nonnull Domain createDomain(@Nonnull String uri){
        return new Domain("blueocean-folder-credential-domain", Messages.BlueOceanCredentialsProvider_DomainDescription(),
                CredentialsUtils.generateDomainSpecifications(uri));
    }
}
