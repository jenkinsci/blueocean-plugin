package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * A row of data which adds one or more data properties.
 */
@ExportedBean(defaultVisibility = 2)
public abstract class BlueTableRow {
    /**
     * @return unique identifier for the row, frequently a run's ID.
     */
    @Exported(name = "id")
    public abstract String getId();
}
