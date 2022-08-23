package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.ExtensionList;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import hudson.ExtensionPoint;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Factory for Bitbucket API. Bitbucket server and cloud APIs are different and
 * different factories are needed to handle it
 *
 * @author Vivek Pandey
 */
public abstract class BitbucketApiFactory implements ExtensionPoint {
    /**
     * @return true if this factory can handle this scmId
     */
    public abstract boolean handles(@NonNull String scmId);

    /**
     * Create {@link BitbucketApi} instance.
     *
     * Before calling this method, clients must ensure {@link #handles(String)} is called first and returned true
     *
     * @param apiUrl API url (host url) of bitbucket server or cloud. e.g. https://mybitbucketserver.com/
     * @param credentials {@link StandardUsernamePasswordCredentials}
     * @return {@link BitbucketApi} instance
     */
    public abstract @NonNull BitbucketApi create(@NonNull String apiUrl, @NonNull StandardUsernamePasswordCredentials credentials);

    /**
     * Resolves a {@link BitbucketApiFactory}
     * @param scmId id {@link Scm#getId()} of Bitbucket SCM provider
     * @return {@link BitbucketApiFactory} instance, could be null
     */
    public static @CheckForNull BitbucketApiFactory resolve(@NonNull String scmId){
        for(BitbucketApiFactory api: ExtensionList.lookup(BitbucketApiFactory.class)){
            if(api.handles(scmId)){
                return api;
            }
        }
        return null;
    }
}
