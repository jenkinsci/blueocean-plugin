package io.jenkins.blueocean.rest.impl.pipeline.scm;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Item;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.Connector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Captures parameters for {@link io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider} from given {@link Item}
 *
 * @author Vivek Pandey
 * @see io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider
 */
public abstract class ScmContentProviderParams {
    private final String apiUrl;
    private final String owner;
    private final String repo;
    private final StandardUsernamePasswordCredentials credentials;


    @SuppressWarnings("unchecked")
    public ScmContentProviderParams(Item item) {
        String apiUrl = null;
        String owner=null;
        String repo = null;
        String credentialId = null;
        if (item instanceof OrganizationFolder) {
            List<SCMNavigator> navigators = ((OrganizationFolder) item).getSCMNavigators();
            if (!navigators.isEmpty()) {
                SCMNavigator navigator = navigators.get(0);
                apiUrl = apiUrl(navigator);
                credentialId = credentialId(navigator);
                owner = owner(navigator);
            }
        } else if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            if (!sources.isEmpty()) {
                SCMSource source = sources.get(0);
                apiUrl = apiUrl(source);
                credentialId = credentialId(source);
                owner = owner(source);
                repo = repo(source);
            }
        }
        this.apiUrl = apiUrl;

        StandardUsernamePasswordCredentials cred = null;
        if (credentialId != null) {
            StandardCredentials credentials = Connector.lookupScanCredentials(item, this.apiUrl, credentialId);
            if (credentials instanceof StandardUsernamePasswordCredentials) {
                cred = (StandardUsernamePasswordCredentials) credentials;
            }
        }
        this.owner = owner;
        this.repo = repo;
        this.credentials = cred;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public StandardUsernamePasswordCredentials getCredentials() {
        return credentials;
    }

    /**
     * Gives owner of {@link SCMSource}, typically organization or project
     *
     * @param scmSource scm source
     * @return null if there is no owner in this SCMSource
     */
    protected abstract @CheckForNull String owner(@Nonnull SCMSource scmSource);

    /**
     * Gives owner of {@link SCMNavigator}, typically organization
     *
     * @param scmNavigator scm navigator
     * @return null if there is no owner in this SCMSource
     */
    protected abstract @CheckForNull String owner(@Nonnull SCMNavigator scmNavigator);


    /**
     * Gives repo name attached to this {@link SCMSource}
     *
     * @param scmSource scm source
     * @return null if there is no repo attached to this SCMSource
     */
    protected abstract @CheckForNull String repo(@Nonnull SCMSource scmSource);

    /**
     * Gives SCM api URL attached to this SCMSource
     * @param scmSource scm source
     * @return SCM api URL
     */
    protected abstract @CheckForNull String apiUrl(@Nonnull SCMSource scmSource);

    /**
     * Gives SCM api URL attached to this ScmNavigator
     * @param scmNavigator scm source
     * @return SCM api URL
     */
    protected abstract @CheckForNull String apiUrl(@Nonnull SCMNavigator scmNavigator);

    /**
     * Gives credential id attached to SCMSource
     */
    protected abstract @CheckForNull String credentialId(@Nonnull SCMSource scmSource);

    /**
     * Gives credential id attached to SCMNavigator
     */
    protected abstract @CheckForNull String credentialId(@Nonnull SCMNavigator scmNavigator);

}
