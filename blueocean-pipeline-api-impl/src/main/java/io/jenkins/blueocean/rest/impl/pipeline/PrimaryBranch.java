package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.model.Job;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class PrimaryBranch {
    /**
     * Resolves the primary branch for a folder
     * @param folder to check within
     * @return default branch
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static Job resolve(@NonNull AbstractFolder folder) {
        Job job = (Job) folder.getAllJobs().stream().filter(
            input -> input != null && ( (Job) input ).getAction( PrimaryInstanceMetadataAction.class ) != null ).
            findFirst().
            orElse(null);
        // Kept for backward compatibility for Git SCMs that do not yet implement PrimaryInstanceMetadataAction
        if (job == null) {
            job = (Job) folder.getJob(DEFAULT_BRANCH);
        }
        return job;
    }

    private static final String DEFAULT_BRANCH = "master";

    private PrimaryBranch() {}
}
