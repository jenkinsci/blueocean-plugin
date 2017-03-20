package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Vivek Pandey
 */
public abstract class ScmRepositoryContainer extends Resource {

    @Exported(name = "repositories", inline = true)
    public abstract ScmRepositories getRepositories();

    public final Object getDynamic(String name) {
        return get(name);
    }

    public abstract ScmRepository get(String name);
}
