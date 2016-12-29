package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

public abstract class BlueArtifact extends Resource{
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String SIZE = "size";
    public static final String PATH = "path";

    @Exported(name=NAME)
    public abstract String getName();

    @Exported(name=PATH)
    public abstract String getPath();

    @Exported(name=URL)
    public abstract String getUrl();

    @Exported(name=SIZE)
    public abstract long getSize();
}
