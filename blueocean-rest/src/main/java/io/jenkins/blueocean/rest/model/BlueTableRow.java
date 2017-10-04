package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author cliffmeyers
 */
@ExportedBean(defaultVisibility = 2)
public abstract class BlueTableRow {

    public static final String ID = "id";

    /**
     * @return unique identifier for the row, frequently a run's ID.
     */
    @Exported(name = ID)
    public abstract String getId();
}
