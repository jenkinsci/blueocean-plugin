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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.service.embedded.rest.jacoco.BlueCoverageTrend.CoverageCategory.BRANCHES;
import static io.jenkins.blueocean.service.embedded.rest.jacoco.BlueCoverageTrend.CoverageCategory.CLASSES;
import static io.jenkins.blueocean.service.embedded.rest.jacoco.BlueCoverageTrend.CoverageCategory.INSTRUCTIONS;
import static io.jenkins.blueocean.service.embedded.rest.jacoco.BlueCoverageTrend.CoverageCategory.LINES;
import static io.jenkins.blueocean.service.embedded.rest.jacoco.BlueCoverageTrend.CoverageCategory.METHODS;


/**
 * @author cliffmeyers
 */
public class BlueCoverageTrend extends BlueTrend {
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

    enum CoverageCategory {
        CLASSES, METHODS, LINES, BRANCHES, INSTRUCTIONS
    }

    public static class CoverageHistoryTable extends BlueTable {
        private static final Map<String, String> LABELS = ImmutableMap.<String, String> builder()
            .put(CLASSES.toString(), "Classes")
            .put(METHODS.toString(), "Methods")
            .put(LINES.toString(), "Lines")
            .put(BRANCHES.toString(), "Branches")
            .put(INSTRUCTIONS.toString(), "Instructions")
            .build();

        private final Iterator<BlueRun> runs;

        public CoverageHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public Map<String, String> getLabels() {
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
        private final Map<String, Integer> totals;

        RowImpl(BlueCoverageSummary summary, String id) {
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

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            return new BlueCoverageTrend(pipeline, parent);
        }
    }
}
