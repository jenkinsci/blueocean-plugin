package io.blueocean.ath.model;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.sse.SSEEvents;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;

public class MultiBranchPipeline extends AbstractPipeline {

    @Inject
    ClassicJobApi jobApi;
    @AssistedInject
    public MultiBranchPipeline(@Assisted String name) {
        super(name);
    }

    @AssistedInject
    public MultiBranchPipeline(@Assisted Folder folder, @Assisted String name) {
        super(folder, name);
    }

    public MultiBranchPipeline createPipeline(GitRepositoryRule git) throws IOException {
        jobApi.createMultiBranchPipeline(jobApi.getFolder(getFolder(), true), getName(), git);
        return this;
    }

    public MultiBranchPipeline createPipeline(FolderJob folderJob, GitRepositoryRule git) throws IOException {
        jobApi.createMultiBranchPipeline(folderJob, getName(), git);
        return this;
    }


    public Predicate<List<JSONObject>> buildsFinished = list -> SSEEvents.activityComplete(getFolder().getPath(getName())).apply(list);

    public MultiBranchPipeline buildBranch(String branch) throws IOException {
        jobApi.buildBranch(getFolder(), getName(), branch);
        return this;
    }

    // Uses jobApi.build(Folder folder, String pipeline) to force a rescan.
    public void rescanThisPipeline() throws IOException {
        jobApi.build(getFolder(), getName());
    }

    public void stopAllRuns() throws IOException {
        jobApi.abortAllBuilds(getFolder(), getName());
    }

    public void deleteThisPipeline(String name) throws IOException {
        jobApi.deletePipeline(name);
    }

    @Override
    public boolean isMultiBranch() {
        return true;
    }

    /**
     * Get the full name to the default branch "master"
     */
    @Override
    public String getFullName() {
        return getFullName("master");
    }

    public String getFullName(String branch) {
        return super.getFullName() + "/" + branch;
    }
}
