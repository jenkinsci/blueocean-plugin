package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.List;

/**
 * Describes InputStep
 *
 * @author Vivek Pandey
 */
public abstract class BlueInputStep extends Resource {
    public static final String ID="id";
    public static final String MESSAGE="message";
    public static final String SUBMITTER="submitter";
    public static final String OK="ok";
    public static final String PARAMETERS="parameters";

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = MESSAGE)
    public abstract String getMessage();

    @Exported(name = SUBMITTER)
    public abstract String getSubmitter();

    @Exported(name = OK)
    public abstract String getOk();

    @Exported(name = PARAMETERS, inline = true)
    public abstract List<Object> getParameters();
}
