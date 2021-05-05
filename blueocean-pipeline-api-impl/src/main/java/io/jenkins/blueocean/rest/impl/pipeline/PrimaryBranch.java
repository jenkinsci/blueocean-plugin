package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.model.Job;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public final class PrimaryBranch {
    /**
     * Resolves the primary branch for a folder
     * @param folder to check within
     * @return default branch
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static Job resolve(@Nonnull AbstractFolder folder) {
        Job job = Iterables.find((Collection<Job>)folder.getAllJobs(),
                                 input-> input != null && input.getAction(PrimaryInstanceMetadataAction.class) != null, null);
        // Kept for backward compatibility for Git SCMs that do not yet implement PrimaryInstanceMetadataAction
        if (job == null) {
            job = (Job) folder.getJob(DEFAULT_BRANCH);
        }
        return job;
    }

    private static final String DEFAULT_BRANCH = "master";

    private PrimaryBranch() {}
}
