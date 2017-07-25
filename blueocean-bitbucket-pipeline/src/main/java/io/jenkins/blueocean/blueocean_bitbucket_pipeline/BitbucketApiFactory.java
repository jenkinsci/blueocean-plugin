package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.ExtensionList;
import org.apache.tools.ant.ExtensionPoint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Factory for Bitbucket API. Bitbucket server and cloud APIs are different and
 * different factories are needed to handle it
 *
 * @author Vivek Pandey
 */
public abstract class BitbucketApiFactory extends ExtensionPoint{
    /**
     * @return true if this factory can handle this API url
     */
    public abstract boolean handles(@Nonnull String apiUrl);

    /**
     * Create {@link BitbucketApi} instance.
     *
     * Before calling this method, clients must ensure {@link #handles(String)} is called first and returned true
     *
     * @param apiUrl API url (host url) of bitbucket server or cloud. e.g. https://mybitcuketserver.com/
     * @param credentials {@link StandardUsernamePasswordCredentials}
     * @return {@link BitbucketApi} instance
     */
    public abstract @Nonnull BitbucketApi create(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials);

    public static @CheckForNull BitbucketApiFactory resolve(@Nonnull String apiUrl){
        for(BitbucketApiFactory api: ExtensionList.lookup(BitbucketApiFactory.class)){
            if(api.handles(apiUrl)){
                return api;
            }
        }
        return null;
    }
}
