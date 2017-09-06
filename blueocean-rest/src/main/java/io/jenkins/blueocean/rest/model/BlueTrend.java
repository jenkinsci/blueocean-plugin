package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

public abstract class BlueTrend extends Resource {

    public static final String DATA = "data";
    public static final String ID = "id";

    @Exported(name = ID)
    public abstract String getId();

    @Exported(merge = true)
    public abstract BlueTable getTable();
}
