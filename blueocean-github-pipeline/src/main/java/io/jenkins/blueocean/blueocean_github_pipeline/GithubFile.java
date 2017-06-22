package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Vivek Pandey
 */
public class GithubFile extends ScmFile<GitContent> {
    private final GitContent content;

    @DataBoundConstructor
    public GithubFile(GitContent content) {
        this.content = content;
    }

    @Override
    public GitContent getContent() {
        return content;
    }

}
