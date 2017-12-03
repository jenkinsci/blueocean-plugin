package io.jenkins.blueocean.service.embedded;

import hudson.model.Action;
import hudson.model.Run;

import javax.annotation.CheckForNull;
import java.util.Objects;

/**
 * Added to a job to provide details of downstream jobs triggered by it, as an inverse of Cause.UpstreamCause which
 * is added to the downstream job during its creation.
 */
public class DownstreamJobAction implements Action {

    private final String downstreamProject;
    private final int downstreamBuild;

    public DownstreamJobAction(Run<?, ?> run) {
        this.downstreamProject = run.getParent().getName();
        this.downstreamBuild = run.getNumber();
    }

    public DownstreamJobAction(String downstreamProject, int downstreamBuild) {
        this.downstreamProject = downstreamProject;
        this.downstreamBuild = downstreamBuild;
    }

    public String getDownstreamProject() {
        return downstreamProject;
    }

    public int getDownstreamBuild() {
        return downstreamBuild;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "DownstreamJobAction - job \"" + downstreamProject + "\" build #" + downstreamBuild;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DownstreamJobAction that = (DownstreamJobAction) o;
        return downstreamBuild == that.downstreamBuild &&
            Objects.equals(downstreamProject, that.downstreamProject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(downstreamProject, downstreamBuild);
    }
}
