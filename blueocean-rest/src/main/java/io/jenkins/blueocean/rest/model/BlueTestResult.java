package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.GET;

public abstract class BlueTestResult extends Resource {

    public static final String STATUS = "status";
    public static final String DURATION = "duration";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String STDERR = "stderr";
    public static final String STDOUT = "stdout";

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
    @WebMethod(name= STDERR)
    public abstract String getStdErr();

    @Navigable
    @GET
    @WebMethod(name= STDOUT)
    public abstract String getStdOut();
}
