package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import jenkins.scm.api.SCMNavigator;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * SCM content specific APIs
 *
 * @author Vivek Pandey
 */
public abstract class ScmContentProvider implements ExtensionPoint{
    /**
     * Get the "scmId" for the content provider as defined in Scm.getId()
     * @return SCM identifier
     */
    public abstract @NonNull String getScmId();

    /**
     * Get the SCM API URL for the provided item
     * @param item {@link Item} item to resolve backing SCM API URL
     * @return SCM API URL
     */
    public abstract @CheckForNull String getApiUrl(@NonNull Item item);

    /**
     * Gives content of scm file.
     *
     * @param staplerRequest {@link StaplerRequest} request specific to this SCM
     * @param item {@link Item} in context. e.g.  {@link jenkins.branch.OrganizationFolder}
     *                         or {@link jenkins.branch.MultiBranchProject}
     * @return scm content
     */
    public abstract @CheckForNull Object getContent(@NonNull StaplerRequest staplerRequest, @NonNull Item item);

    /**
     * Save content
     *
     * @param staplerRequest {@link StaplerRequest} request specific to this SCM
     * @param item {@link Item} in context. e.g.  {@link jenkins.branch.OrganizationFolder}
     *                         or {@link jenkins.branch.MultiBranchProject}
     * @return SCM specific save content response
     */
    public abstract @CheckForNull Object saveContent(@NonNull StaplerRequest staplerRequest, @NonNull Item item);

    /**
     * {@link ScmContentProvider} instance that supports given {@link SCMNavigator}.
     *
     * @return true if this provide can handle it.
     */
    public abstract boolean support(@NonNull Item item);

    /**
     * Resolve {@link ScmContentProvider} for given {@link Item}.
     *
     * @param item item for which {@link ScmContentProvider} is resolved
     * @return resolved ScmContentProvider
     */
    public static ScmContentProvider resolve(@NonNull Item item){
        for(ScmContentProvider provider: all()){
            if(provider.support(item)){
                return provider;
            }
        }
        return null;
    }

    public static ExtensionList<ScmContentProvider> all(){
        return ExtensionList.lookup(ScmContentProvider.class);
    }
}
