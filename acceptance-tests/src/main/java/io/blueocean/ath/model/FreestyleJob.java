package io.blueocean.ath.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.api.classic.ClassicJobApi;

import java.io.IOException;

public class FreestyleJob extends AbstractPipeline {
    @Inject
    ClassicJobApi jobApi;

    @AssistedInject
    public FreestyleJob(@Assisted String name) {
        super(name);
    }

    @AssistedInject
    public FreestyleJob(@Assisted Folder folder, @Assisted String name) {
        super(folder, name);
    }

    public FreestyleJob create(String command) throws IOException {
        jobApi.createFreeStyleJob(jobApi.getFolder(getFolder(), true), getName(), command);
        return this;
    }

    public FreestyleJob build() throws IOException {
        jobApi.build(getFolder(), getName());
        return this;
    }
}
