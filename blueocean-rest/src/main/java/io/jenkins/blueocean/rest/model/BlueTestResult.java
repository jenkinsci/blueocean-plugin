package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.GET;

import static hudson.Util.rawEncode;

public abstract class BlueTestResult extends Resource {

    public static final String STATUS = "status";
    public static final String DURATION = "duration";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String AGE = "age";
    public static final String STDERR = "stdErr";
    public static final String STDOUT = "stdOut";
    public static final String HAS_STDLOG = "hasStdLog";
    public static final String STATE = "state";
    public static final String ERROR_STACK_TRACE = "errorStackTrace";
    public static final String ERROR_DETAILS = "errorDetails";

    public enum Status {
        UNKNOWN,
        PASSED,
        FAILED,
        SKIPPED
    }

    public enum State {
        UNKNOWN,
        FIXED,
        REGRESSION
    }

    protected final Link parent;

    public BlueTestResult(Link parent) {
        this.parent = parent;
    }

    @Exported(name = STATUS)
    public abstract Status getStatus();

    @Exported(name = STATE)
    public abstract State getTestState();

    @Exported(name = DURATION)
    public abstract float getDuration();

    @Exported(name = ID)
    public final String getId() {
        return rawEncode(this.getClass().getName()) + ":" + rawEncode(getUniqueId());
    }

    @Exported(name = AGE)
    public abstract int getAge();

    @Exported(name = NAME)
    public abstract String getName();

    @Exported(name = ERROR_STACK_TRACE)
    public abstract String getErrorStackTrace();

    @Exported(name = ERROR_DETAILS)
    public abstract String getErrorDetails();

    @Navigable
    @GET
    @WebMethod(name= STDERR)
    public abstract String getStdErr();

    @Navigable
    @GET
    @WebMethod(name= STDOUT)
    public abstract String getStdOut();

    @Exported(name = HAS_STDLOG )
    /**
     * @return <code>true</code> if the result has stOut or stErr to serve
     */
    public abstract boolean hasStdLog();

    protected abstract String getUniqueId();

    @Override
    public Link getLink() {
        return parent.rel("tests/" + rawEncode(getId()));
    }
}
