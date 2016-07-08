package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Ivan Meredith
 */
@ExportedBean
@Capability("io.jenkins.blueocean.rest.model.BlueFavorite")
public class BlueFavorite {
    private static final String PIPELINE = "pipeline";
    private final String pipeline;

    public BlueFavorite(String pipeline) {
        this.pipeline = pipeline;
    }

    @Exported(name = PIPELINE)
    @JsonProperty(PIPELINE)
    public  String getPipeline(){
        return pipeline;
    }
}
