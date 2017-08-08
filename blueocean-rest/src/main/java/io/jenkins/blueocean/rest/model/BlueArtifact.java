package io.jenkins.blueocean.rest.model;

import hudson.Util;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.export.Exported;

import static hudson.Util.rawEncode;

public abstract class BlueArtifact extends Resource{
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String SIZE = "size";
    public static final String PATH = "path";
    public static final String DOWNLOADABLE = "downloadable";

    protected final Link parent;

    public BlueArtifact(Link parent) {
        this.parent = parent;
    }

    @Exported(name=NAME)
    public abstract String getName();

    @Exported(name=PATH)
    public abstract String getPath();

    @Exported(name=URL)
    public abstract String getUrl();

    @Exported(name=SIZE)
    public abstract long getSize();

    @Exported(name = DOWNLOADABLE)
    public abstract boolean isDownloadable();

    @Override
    public final Link getLink() {
        return parent.rel(Util.rawEncode(rawEncode(this.getClass().getName()) + ":" + rawEncode(getName())));
    }
}
