package io.jenkins.blueocean.rest.impl.pipeline.scm;

/**
 * @author Vivek Pandey
 */
public abstract class ScmSaveFileRequest {
    public abstract ScmFile save(Scm scm);
}
