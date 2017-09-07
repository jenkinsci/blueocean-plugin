package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Map;

/**
 * A table-like data structure consisting of multiple rows each with multiple "columns" (properties)
 * If a table's columns are consistent for all rows, the "columns" property can hold this metadata as a hint to consumers.
 *
 * @author cliffmeyers
 */
@ExportedBean
public abstract class BlueTable {

    public static final String COLUMNS = "columns";
    public static final String ROWS = "rows";

    /**
     * @return map of columns' property names (keys) and descriptions (values). optional
     */
    @Exported(name = COLUMNS, inline = true)
    public abstract Map<String, String> getColumns();

    /**
     * @return individual rows of data
     */
    @Exported(name = ROWS)
    public abstract List<BlueTableRow> getRows();

}
