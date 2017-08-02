package io.jenkins.blueocean.blueocean_git_pipeline;

import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Vivek Pandey
 */
public class GitFile extends ScmFile<GitContent> {
    private final GitContent content;

    @DataBoundConstructor
    public GitFile(GitContent content) {
        this.content = content;
    }

    @Override
    public GitContent getContent() {
        return content;
    }

}
