package io.jenkins.blueocean.rest.impl.pipeline.credential;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import io.jenkins.blueocean.rest.impl.pipeline.Messages;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type, @Nullable ItemGroup itemGroup,
                                                          @Nullable Authentication authentication) {
        if (itemGroup instanceof AbstractFolder) {
            return (List<C>) getCredentials((AbstractFolder)itemGroup);
        }
        return Collections.emptyList();
    }

    private static  List<Credentials> getCredentials(@Nonnull AbstractFolder folder){
        return getCredentials((FolderPropertyImpl)folder.getProperties().get(FolderPropertyImpl.class));
    }

    private static  List<Credentials> getCredentials(@Nullable FolderPropertyImpl prop){
        if (prop != null) {
            User user = User.get(prop.getUser(), false, Collections.emptyMap());
            if(user != null){
                for (CredentialsStore store: CredentialsProvider.lookupStores(user)) {
                    Domain domain = store.getDomainByName(prop.getDomain());
                    if(domain != null){
                        return CredentialsMatchers.filter(store.getCredentials(domain),
                                CredentialsMatchers.allOf(CredentialsMatchers.withId(prop.getId())));
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public <C extends IdCredentials> ListBoxModel getCredentialIds(@Nonnull Class<C> type,
                                                                   @Nullable ItemGroup itemGroup,
                                                                   @Nullable Authentication authentication,
                                                                   @Nonnull List<DomainRequirement> domainRequirements,
                                                                   @Nonnull CredentialsMatcher matcher) {
        ListBoxModel result = new ListBoxModel();
        if (itemGroup instanceof AbstractFolder) {
            FolderPropertyImpl prop = (FolderPropertyImpl) ((AbstractFolder) itemGroup).getProperties().get(FolderPropertyImpl.class);
            if (prop != null) {
                result.add(Messages.BlueOceanCredentialsProvider_DisplayName(), prop.getId());
            }

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
        if(isApplicable(object)) {
            return new FolderPropertyImpl.StoreImpl((AbstractFolder) object);
        }
        return null;
    }

    @Override
    public Set<CredentialsScope> getScopes(ModelObject object) {
        if(isApplicable(object)){
            return ImmutableSet.of(CredentialsScope.GLOBAL);
        }
        return Collections.emptySet();
    }

    private boolean isApplicable(ModelObject object){
        return object instanceof AbstractFolder &&
                ((AbstractFolder)object).getProperties().get(FolderPropertyImpl.class) != null;
    }

    public static class FolderPropertyImpl extends AbstractFolderProperty<AbstractFolder<TopLevelItem>> {
        private final String user;
        private final String id;
        private final String domain;

        @DataBoundConstructor
        public FolderPropertyImpl(@Nonnull String user, @Nonnull String id, @Nonnull String domain) {
            this.user = user;
            this.id = id;
            this.domain = domain;
        }

        public @Nonnull String getUser() {
            return user;
        }

        public @Nonnull String getId() {
            return id;
        }

        public @Nonnull String getDomain() {
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

        public static class StoreImpl extends CredentialsStore{
            private final AbstractFolder abstractFolder;
            private final CredentialsStoreAction storeAction;

            StoreImpl(AbstractFolder abstractFolder) {
                super(BlueOceanCredentialsProvider.class);
                this.abstractFolder = abstractFolder;
                this.storeAction = new CredentialsStoreActionImpl(this);
            }

            @Nonnull
            @Override
            public List<Domain> getDomains() {
                FolderPropertyImpl prop = (FolderPropertyImpl) abstractFolder.getProperties().get(FolderPropertyImpl.class);
                List<Domain> domains = new ArrayList<>();
                if(prop != null){
                    User user = User.get(prop.getUser(), false, Collections.emptyMap());
                    if(user != null) {
                        for (CredentialsStore store : CredentialsProvider.lookupStores(user)) {
                            Domain domain = store.getDomainByName(prop.getDomain());
                            if(domain != null) {
                                domains.add(domain);
                            }
                        }
                    }
                }
                return domains;
            }

            @Nullable
            @Override
            public CredentialsStoreAction getStoreAction() {
                return storeAction;
            }

            @Nonnull
            @Override
            public ModelObject getContext() {
                return abstractFolder;
            }

            @Override
            public boolean hasPermission(@Nonnull Authentication a, @Nonnull Permission permission) {
                // its read only so for all permissions other than READ, we return false
                if(permission != Permission.READ){
                    return false;
                }
                return abstractFolder.getACL().hasPermission(a,permission);
            }

            @Nonnull
            @Override
            public List<Credentials> getCredentials(@Nonnull Domain domain) {
                FolderPropertyImpl prop = (FolderPropertyImpl)abstractFolder.getProperties().get(FolderPropertyImpl.class);
                if(prop == null || !prop.getDomain().equals(domain.getName())){
                    return Collections.emptyList();
                }
                return BlueOceanCredentialsProvider.getCredentials(prop);
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
            public boolean updateCredentials(@Nonnull Domain domain, @Nonnull Credentials current, @Nonnull Credentials replacement) throws IOException {
                throw new UnsupportedOperationException("Not supported");
            }
        }
    }
}
