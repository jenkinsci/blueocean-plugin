package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * SCM content specific APIs
 *
 * @author Vivek Pandey
 */
public abstract class ScmContentProvider implements ExtensionPoint{
    /**
     * Gives content of given {@link SCMFile}.
     *
     * @param scmFile file
     * @return scm content
     */
    public abstract @CheckForNull Object getContent(@Nonnull SCMSource scmSource, @Nonnull SCMFile scmFile);

    /**
     * Save content
     *
     * @param staplerRequest {@link StaplerRequest} request specific to this SCM
     * @param item {@link Item} in context. e.g.  {@link jenkins.branch.OrganizationFolder}
     *                         or {@link jenkins.branch.MultiBranchProject}
     * @return SCM specific save content response
     */
    public abstract @CheckForNull Object saveContent(@Nonnull StaplerRequest staplerRequest, @Nonnull Item item);

    /**
     * {@link ScmContentProvider} instance that supports given {@link SCMNavigator}.
     *
     * @return true if this provide can handle it.
     */
    public abstract boolean support(@Nonnull Item item);

    /**
     * Resolve {@link ScmContentProvider} for given {@link Item}.
     *
     * @param item item for which {@link ScmContentProvider} is resolved
     * @return resolved ScmContentProvider
     */
    public static ScmContentProvider resolve(@Nonnull Item item){
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
