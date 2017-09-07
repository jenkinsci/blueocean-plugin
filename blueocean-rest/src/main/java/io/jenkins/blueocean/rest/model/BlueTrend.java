package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

/**
 * A category of tabular data, typically one that changes over time (e.g. across successive runs)
 *
 * To create a new "trend"
 * 1. Implement {@link io.jenkins.blueocean.rest.factory.BlueTrendFactory} and return a new subclass of BlueTrend
 * 2. Subclass {@link BlueTable} and {@link BlueTableRow} adding suitable properties to construct the data set.
 *
 * @author cliffmeyers
 */
public abstract class BlueTrend extends Resource {

    public static final String ID = "id";

    /**
     * @return name or identifier of the trend, e.g. "buildDuration"
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * @return raw data in "table" format
     */
    @Exported(merge = true)
    public abstract BlueTable getTable();
}
