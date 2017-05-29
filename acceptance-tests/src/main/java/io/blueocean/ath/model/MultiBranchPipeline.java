package io.blueocean.ath.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;

import java.io.IOException;

public class MultiBranchPipeline extends Pipeline{

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
        jobApi.createMultlBranchPipeline(jobApi.getFolder(getFolder(), true), getName(), git);
        return this;
    }
}
