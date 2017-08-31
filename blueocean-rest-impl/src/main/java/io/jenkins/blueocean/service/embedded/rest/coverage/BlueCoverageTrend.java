package io.jenkins.blueocean.service.embedded.rest.coverage;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTrend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend.CoverageCategory.BRANCHES;
import static io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend.CoverageCategory.CLASSES;
import static io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend.CoverageCategory.INSTRUCTIONS;
import static io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend.CoverageCategory.LINES;
import static io.jenkins.blueocean.service.embedded.rest.coverage.BlueCoverageTrend.CoverageCategory.METHODS;


/**
 * @author cliffmeyers
 */
public abstract class BlueCoverageTrend extends BlueTrend {
    protected final BluePipeline pipeline;
    protected final Link parent;

    public BlueCoverageTrend(BluePipeline pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    public abstract BlueTable getTable();

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    enum CoverageCategory {
        CLASSES, METHODS, LINES, BRANCHES, INSTRUCTIONS
    }

    public static abstract class CoverageHistoryTable extends BlueTable {
        private final Map<String, String> LABELS = ImmutableMap.<String, String> builder()
            .put(CLASSES.toString(), "Classes")
            .put(METHODS.toString(), "Methods")
            .put(LINES.toString(), "Lines")
            .put(BRANCHES.toString(), "Branches")
            .put(INSTRUCTIONS.toString(), "Instructions")
            .build();

        @Override
        public Map<String, String> getLabels() {
            return LABELS;
        }

        public abstract List<Row> getRows();
    }

    public static class RowImpl extends BlueTable.Row {
        private final String id;
        private final Map<String, Integer> totals;

        public RowImpl(BlueCoverageSummary summary, String id) {
            this.id = id;
            totals = new HashMap<>();
            totals.put(CLASSES.toString(), summary.getClasses().getPercent());
            totals.put(METHODS.toString(), summary.getMethods().getPercent());
            totals.put(LINES.toString(), summary.getLines().getPercent());
            totals.put(BRANCHES.toString(), summary.getBranches().getPercent());
            totals.put(INSTRUCTIONS.toString(), summary.getInstructions().getPercent());
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Map<String, ?> getColumns() {
            return totals;
        }
    }
}
