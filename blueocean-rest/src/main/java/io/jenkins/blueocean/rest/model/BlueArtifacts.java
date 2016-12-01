package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

public abstract class BlueArtifacts extends Resource{
    @Exported
    public abstract String getZipFile();

    @Exported(inline=true)
    public abstract BlueArtifactContainer getArtifacts();

}
