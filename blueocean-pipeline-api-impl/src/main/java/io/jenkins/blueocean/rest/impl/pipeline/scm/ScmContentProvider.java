package io.jenkins.blueocean.rest.impl.pipeline.scm;

import javax.annotation.Nonnull;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMSource;

/**
 * Provides {@link ScmContent} for the given {@link SCMSource}
 *
 * @author Vivek Pandey
 */
public abstract class ScmContentProvider implements ExtensionPoint{
    /**
     * {@link ScmContent} instance from given {@link SCMFile}.
     *
     * @param scmFile file
     * @return SCMContent instance
     */
    public abstract @Nonnull ScmContent getContent(@Nonnull SCMSource scmSource, @Nonnull SCMFile scmFile);

    /**
     * {@link ScmContentProvider} instance that supports given {@link SCMSource}.
     *
     * @return true if this provide can handle it.
     */
    public abstract boolean support(@Nonnull SCMSource source);

    public static ScmContentProvider resolve(SCMSource source){
        for(ScmContentProvider provider: all()){
            if(provider.support(source)){
                return provider;
            }
        }
        return null;
    }

    public static ExtensionList<ScmContentProvider> all(){
        return ExtensionList.lookup(ScmContentProvider.class);
    }
}
