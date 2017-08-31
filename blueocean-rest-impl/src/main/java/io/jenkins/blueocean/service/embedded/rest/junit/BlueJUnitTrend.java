package io.jenkins.blueocean.service.embedded.rest.junit;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Restricted(NoExternalUse.class)
public class BlueJUnitTrend extends BlueTrend {
    private final BluePipeline pipeline;
    private final Link parent;

    public BlueJUnitTrend(BluePipeline pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "junit";
    }

    @Override
    public BlueTable getTable() {
        // TODO: make this understand query params for paging, date ranges, etc
        Iterator<BlueRun> iterator = pipeline.getRuns().iterator(0, 101);
        return new JUnitHistoryTable(iterator);
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    public static class JUnitHistoryTable extends BlueTable {

        private final Iterator<BlueRun> runs;

        public JUnitHistoryTable(Iterator<BlueRun> runs) {
            this.runs = runs;
        }

        @Override
        public List<Row> getRows() {
            return Lists.newArrayList(Iterators.transform(runs, new Function<BlueRun, Row>() {
                @Override
                public Row apply(BlueRun input) {
                    return new RowImpl(input.getTestSummary(), input.getId());
                }
            }));
        }
    }

    public enum TestCategory {
        TOTAL, PASSED, FIXED, FAILED, EXISITING_FAILED, REGRESSIONS, SKIPPED
    }

    public static class RowImpl extends BlueTable.Row {
        private final List<BlueTable.Column> columns;
        private final String id;

        RowImpl(BlueTestSummary summary, String id) {
            this.id = id;
            columns = new ArrayList<>();
            columns.add(new TestCount(TestCategory.TOTAL, summary.getTotal()));
            columns.add(new TestCount(TestCategory.PASSED, summary.getPassedTotal()));
            columns.add(new TestCount(TestCategory.FIXED, summary.getFixedTotal()));
            columns.add(new TestCount(TestCategory.FAILED, summary.getFailedTotal()));
            columns.add(new TestCount(TestCategory.EXISITING_FAILED, summary.getExistingFailedTotal()));
            columns.add(new TestCount(TestCategory.REGRESSIONS, summary.getRegressionsTotal()));
            columns.add(new TestCount(TestCategory.SKIPPED, summary.getSkippedTotal()));
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public List<BlueTable.Column> getColumns() {
            return columns;
        }
    }

    public static class TestCount extends BlueTable.Column {

        private final TestCategory category;
        private final long count;

        public TestCount(TestCategory category, long count) {
            this.category = category;
            this.count = count;
        }

        @Override
        public String getName() {
            return category.toString();
        }

        @Override
        public Object getValue() {
            return count;
        }
    }

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            return new BlueJUnitTrend(pipeline, parent);
        }
    }
}
