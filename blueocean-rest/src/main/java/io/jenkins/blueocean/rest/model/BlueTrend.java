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
    public static final String DISPLAY_NAME = "displayName";

    /**
     * A unique identifier for the trend to be used in the REST API path, e.g. my-pipeline/trends/${id}
     * Note that an ID collision will result in only one trend being available.
     * @return identifier
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * @return name display in UI
     */
    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    /**
     * @return map of columns' property names (keys) and descriptions (values). optional
     */
    @Exported(name = COLUMNS, inline = true)
    public abstract Map<String, String> getColumns();

    /**
     @return individual rows of data
     */
    @Navigable
    public abstract Container<BlueTableRow> getRows();

}
