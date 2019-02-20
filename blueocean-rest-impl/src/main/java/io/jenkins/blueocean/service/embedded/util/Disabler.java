package io.jenkins.blueocean.service.embedded.util;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractProject;
import jenkins.model.ParameterizedJobMixIn;

import java.io.IOException;

public class Disabler {
    public static void makeDisabled(AbstractFolder folder, boolean b) throws IOException {
        folder.makeDisabled(b);
    }

    public static void makeDisabled(AbstractProject project, boolean b) throws IOException {
        project.makeDisabled(b);
    }

    public static void makeDisabled(ParameterizedJobMixIn.ParameterizedJob job, boolean b) throws IOException {
        job.makeDisabled(b);
    }

    public static void makeDisabled(Object item, boolean b) throws IOException {
        if (item instanceof AbstractFolder) {
            Disabler.makeDisabled((AbstractFolder) item, b);
        }
        if (item instanceof AbstractProject) {
            Disabler.makeDisabled((AbstractProject) item, b);
        }
        if (item instanceof ParameterizedJobMixIn.ParameterizedJob ) {
            Disabler.makeDisabled((ParameterizedJobMixIn.ParameterizedJob ) item, b);
        }
    }


    public static Boolean isDisabled(AbstractFolder folder) {
        return folder.isDisabled();
    }

    public static Boolean isDisabled(AbstractProject project) {
        return project.isDisabled();
    }

    public static Boolean isDisabled(ParameterizedJobMixIn.ParameterizedJob job) {
        return job.isDisabled();
    }

    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "isDisabled will return null if the job type doesn't support it")
    public static Boolean isDisabled(Object item) {
        if (item instanceof AbstractFolder) {
            return Disabler.isDisabled((AbstractFolder) item);
        }
        if (item instanceof AbstractProject) {
            return Disabler.isDisabled((AbstractProject) item);
        }
        if (item instanceof ParameterizedJobMixIn.ParameterizedJob ) {
            return Disabler.isDisabled((ParameterizedJobMixIn.ParameterizedJob ) item);
        }
        return null;
    }
}
