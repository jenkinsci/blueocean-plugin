package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.pageable.Pageable;

import java.util.Map;

/**
 * @author cliffmeyers
 */
public interface BlueTable {

    String COLUMNS = "columns";
    String ROWS = "rows";

    /**
     * @return map of columns' property names (keys) and descriptions (values). optional
     */
    Map<String, String> getColumns();

    /**
     * @return individual rows of data
     */
    Pageable<BlueTableRow> getRows();

}
