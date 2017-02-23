package io.jenkins.blueocean.rest.impl.pipeline.scm;

/**
 * @author Vivek Pandey
 */
public abstract class ScmSaveFileRequest {
    public abstract ScmContent save(Scm scm);
}
