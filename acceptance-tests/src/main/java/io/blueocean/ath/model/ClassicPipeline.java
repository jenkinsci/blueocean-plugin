package io.blueocean.ath.model;


import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClassicPipeline extends AbstractPipeline {
    @AssistedInject
    public ClassicPipeline(@Assisted String name) {
        super(name);
    }

    @AssistedInject
    public ClassicPipeline(@Assisted Folder folder, @Assisted String name) {
        super(folder, name);
    }

    @Inject
    ClassicJobApi jobApi;

    public ClassicPipeline createPipeline(String script) throws IOException {
        jobApi.createPipeline(jobApi.getFolder(getFolder(), true), getName(), script);
        return this;
    }

    public ClassicPipeline createPipeline(File script) throws IOException {
        return createPipeline(FileUtils.readFileToString(script));
    }

    public ClassicPipeline build() throws IOException {
        jobApi.build(getFolder(), getName());
        return this;
    }


}
