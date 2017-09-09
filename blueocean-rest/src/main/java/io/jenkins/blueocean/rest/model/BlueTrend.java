package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.export.Exported;

import java.util.Map;

/**
 * A category of tabular data, typically one that changes over time (e.g. across successive runs)
 *
 * To create a new "trend"
 * 1. Implement {@link io.jenkins.blueocean.rest.factory.BlueTrendFactory} and return a new subclass of BlueTrend
 * 2. Subclass {@link BlueTable} and {@link BlueTableRow} adding suitable properties to construct the data set.
 *
 * @author cliffmeyers
 */
public abstract class BlueTrend extends Resource implements BlueTable {

    public static final String ID = "id";

    /**
     * @return name or identifier of the trend, e.g. "buildDuration"
     */
    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = COLUMNS, inline = true)
    public abstract Map<String, String> getColumns();

    @Navigable
    public abstract Container<BlueTableRow> getRows();

}
