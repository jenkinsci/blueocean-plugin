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
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;
import java.util.List;

import static io.jenkins.blueocean.rest.model.BlueTestSummary.*;

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

    public class JUnitHistoryTable extends BlueTable {

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

    public static class RowImpl extends BlueTable.Row {
        private final BlueTestSummary summary;
        private final String id;

        RowImpl(BlueTestSummary summary, String id) {
            this.summary = summary;
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Exported(name = PASSED)
        public long getPassedTotal() {
            return summary.getPassedTotal();
        }

        @Exported(name = FAILED)
        public long getFailedTotal() {
            return summary.getFailedTotal();
        }

        @Exported(name = SKIPPED)
        public long getSkippedTotal() {
            return summary.getSkippedTotal();
        }

        @Exported(name = FIXED)
        public long getFixedTotal() {
            return summary.getFixedTotal();
        }

        @Exported(name = EXISTING_FAILED)
        public long getExistingFailedTotal() {
            return summary.getExistingFailedTotal();
        }

        @Exported(name = REGRESSIONS)
        public long getRegressionsTotal() {
            return summary.getRegressionsTotal();
        }

        @Exported(name = TOTAL)
        public long getTotal() {
            return summary.getTotal();
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
