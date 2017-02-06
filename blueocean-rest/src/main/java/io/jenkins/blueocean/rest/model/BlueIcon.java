package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;

public abstract class BlueIcon extends Resource {

    public static final int DEFAULT_ICON_SIZE = 20;

    @Navigable
    public abstract void getUrl();
}
