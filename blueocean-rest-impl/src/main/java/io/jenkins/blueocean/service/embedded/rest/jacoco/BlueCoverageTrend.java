package io.jenkins.blueocean.service.embedded.rest.jacoco;

import com.google.common.base.Function;
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
import java.util.Iterator;
import java.util.List;


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

    public enum CoverageCategory {
        CLASSES, METHODS, LINES, BRANCHES, INSTRUCTIONS
    }

    public static class CoverageHistoryTable extends BlueTable {
        private final Iterator<BlueRun> runs;

        public CoverageHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
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
        private final List<BlueTable.Column> totals;

        RowImpl(BlueCoverageSummary summary, String id) {
            this.id = id;
            totals = new ArrayList<>();
            totals.add(new CoverageTotal(CoverageCategory.CLASSES, summary.getClasses().getPercent()));
            totals.add(new CoverageTotal(CoverageCategory.METHODS, summary.getMethods().getPercent()));
            totals.add(new CoverageTotal(CoverageCategory.LINES, summary.getLines().getPercent()));
            totals.add(new CoverageTotal(CoverageCategory.BRANCHES, summary.getBranches().getPercent()));
            totals.add(new CoverageTotal(CoverageCategory.INSTRUCTIONS, summary.getInstructions().getPercent()));
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public List<BlueTable.Column> getColumns() {
            return totals;
        }
    }

    public static class CoverageTotal extends BlueTable.Column {

        private final CoverageCategory category;
        private final int percent;

        public CoverageTotal(CoverageCategory category, int percent) {
            this.category = category;
            this.percent = percent;
        }

        @Override
        public String getName() {
            return category.toString();
        }

        @Override
        public Object getValue() {
            return percent;
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
