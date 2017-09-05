package io.blueocean.ath.model;


import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class Pipeline extends AbstractPipeline {
    @AssistedInject
    public Pipeline(@Assisted String name) {
        super(name);
    }

    @AssistedInject
    public Pipeline(@Assisted Folder folder, @Assisted String name) {
        super(folder, name);
    }

    @Inject
    ClassicJobApi jobApi;

    public Pipeline createPipeline(String script) throws IOException {
        jobApi.createPipeline(jobApi.getFolder(getFolder(), true), getName(), script);
        return this;
    }

    public Pipeline createPipeline(File script) throws IOException {
        return createPipeline(FileUtils.readFileToString(script));
    }

    public Pipeline build() throws IOException {
        jobApi.build(getFolder(), getName());
        return this;
    }
}
