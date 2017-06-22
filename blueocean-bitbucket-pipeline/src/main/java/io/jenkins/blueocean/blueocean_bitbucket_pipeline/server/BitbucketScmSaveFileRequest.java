package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Vivek Pandey
 */
public class BitbucketScmSaveFileRequest {
    private final GitContent content;

    @DataBoundConstructor
    public BitbucketScmSaveFileRequest(GitContent content) {
        this.content = content;
    }

    public GitContent getContent() {
        return content;
    }
}
