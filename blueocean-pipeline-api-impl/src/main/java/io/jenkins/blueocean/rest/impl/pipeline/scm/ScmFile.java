package io.jenkins.blueocean.rest.impl.pipeline.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author Vivek Pandey
 */
@ExportedBean(defaultVisibility = 9999)
public abstract class ScmFile<T extends ScmContent> {

    private static final String CONTENT = "content";

    /**
     *
     * @return Gives SCM file content
     */
    @Exported(name = CONTENT, inline = true)
    public abstract T getContent();
}
