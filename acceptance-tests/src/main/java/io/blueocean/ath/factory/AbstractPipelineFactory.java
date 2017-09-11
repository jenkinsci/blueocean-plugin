package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.model.Folder;

public interface AbstractPipelineFactory<T extends AbstractPipeline> {
    T pipeline(Folder folder, String name);

    T pipeline(String name);
}
