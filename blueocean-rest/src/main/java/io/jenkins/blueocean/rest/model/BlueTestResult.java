package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.GET;

public abstract class BlueTestResult extends Resource {

    public static final String STATUS = "STATUS";
    public static final String DURATION = "DURATION";
    public static final String NAME = "NAME";
    public static final String ID = "ID";

    public enum Status {
        PASSED,
        FAILED,
        SKIPPED
    }

    @Exported(name = STATUS)
    public abstract Status getStatus();

    @Exported(name = DURATION)
    public abstract float getDuration();

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = NAME)
    public abstract String getName();

    @Navigable
    @GET
    @WebMethod(name="stderr")
    public abstract String getStdErr();

    @Navigable
    @GET
    @WebMethod(name="stdout")
    public abstract String getStdOut();
}
