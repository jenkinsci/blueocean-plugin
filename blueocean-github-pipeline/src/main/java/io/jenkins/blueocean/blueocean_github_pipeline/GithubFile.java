package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Vivek Pandey
 */
public class GithubFile extends ScmFile<GithubContent> {
    private final GithubContent content;

    @DataBoundConstructor
    public GithubFile(GithubContent content) {
        this.content = content;
    }

    @Override
    public GithubContent getContent() {
        return content;
    }

}
