package io.blueocean.ath.factory;

import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.Pipeline;

public interface PipelineFactory<T extends Pipeline> {
    T pipeline(Folder folder, String name);

    T pipeline(String name);
}
