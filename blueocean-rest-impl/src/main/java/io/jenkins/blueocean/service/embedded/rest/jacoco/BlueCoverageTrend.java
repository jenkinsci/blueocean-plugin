package io.jenkins.blueocean.service.embedded.rest.jacoco;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueCoverageSummary;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author cliffmeyers
 */
public class BlueCoverageTrend extends BlueTrend {

    private static final String CLASSES = "classes";
    private static final String METHODS = "methods";
    private static final String LINES = "lines";
    private static final String BRANCHES = "branches";
    private static final String INSTRUCTIONS = "instructions";

    private final BluePipeline pipeline;
    private final Link parent;

    public BlueCoverageTrend(BluePipeline pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "coverage";
    }

    @Override
    public BlueTable getTable() {
        // TODO: make this understand query params for paging, date ranges, etc
        Iterator<BlueRun> iterator = pipeline.getRuns().iterator(0, 101);
        return new BlueCoverageTrend.CoverageHistoryTable(iterator);
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    public static class CoverageHistoryTable extends BlueTable {
        private static final Map<String, String> LABELS = ImmutableMap.<String, String> builder()
            .put(CLASSES, "Classes")
            .put(METHODS, "Methods")
            .put(LINES, "Lines")
            .put(BRANCHES, "Branches")
            .put(INSTRUCTIONS, "Instructions")
            .build();

        private final Iterator<BlueRun> runs;

        public CoverageHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public Map<String, String> getColumns() {
            return LABELS;
        }

        @Override
        public List<Row> getRows() {
            return Lists.newArrayList(Iterators.transform(runs, new Function<BlueRun, Row>() {
                @Override
                public Row apply(BlueRun input) {
                    return new BlueCoverageTrend.RowImpl(input.getCoverageSummary(), input.getId());
                }
            }));
        }
    }

    public static class RowImpl extends BlueTable.Row {
        private final String id;
        private final BlueCoverageSummary summary;

        RowImpl(BlueCoverageSummary summary, String id) {
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

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            return new BlueCoverageTrend(pipeline, parent);
        }
    }
}
