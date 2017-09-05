package io.jenkins.blueocean.service.embedded.rest.coverage;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.stapler.export.Exported;

import java.util.List;
import java.util.Map;


/**
 * @author cliffmeyersØ
 */
public abstract class BlueCoverageTrend extends BlueTrend {
    private static final String CLASSES = "classes";
    private static final String METHODS = "methods";
    private static final String LINES = "lines";
    private static final String BRANCHES = "branches";
    private static final String INSTRUCTIONS = "instructions";

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



    public static abstract class CoverageHistoryTable extends BlueTable {
        private final Map<String, String> LABELS = ImmutableMap.<String, String> builder()
            .put(CLASSES, "Classes")
            .put(METHODS, "Methods")
            .put(LINES, "Lines")
            .put(BRANCHES, "Branches")
            .put(INSTRUCTIONS, "Instructions")
            .build();

        @Override
        public Map<String, String> getColumns() {
            return LABELS;
        }

        public abstract List<Row> getRows();
    }

    public static class RowImpl extends BlueTable.Row {
        private final String id;
        private final BlueCoverageSummary summary;

        public RowImpl(BlueCoverageSummary summary, String id) {
            this.id = id;
            this.summary = summary;
        }

        @Override
        public String getId() {
            return id;
        }

        @Exported(name = CLASSES)
        public int getClasses() {
            return summary.getClasses().getPercent();
        }

        @Exported(name = METHODS)
        public int getMethods() {
            return summary.getMethods().getPercent();
        }

        @Exported(name = LINES)
        public int getLines() {
            return summary.getLines().getPercent();
        }

        @Exported(name = BRANCHES)
        public int getBranches() {
            return summary.getBranches().getPercent();
        }

        @Exported(name = INSTRUCTIONS)
        public int getInstructions() {
            return summary.getInstructions().getPercent();
        }
    }
}
