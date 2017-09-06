package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTable;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.stapler.export.CustomExportedBean;

import java.util.List;
import java.util.Map;

/**
 * Trend for Stage durations
 */
public class StageRuntimeTrend extends BlueTrend {

    private final BranchImpl branch;
    private final Link parent;

    public StageRuntimeTrend(BranchImpl branch, Link parent) {
        this.branch = branch;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "stageDuration";
    }

    @Override
    public BlueTable getTable() {
        return new StageRuntimeTable(branch);
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    public static class StageRuntimeTable extends BlueTable {
        private final BranchImpl branch;

        public StageRuntimeTable(BranchImpl branch) {
            this.branch = branch;
        }

        @Override
        public Map<String, String> getColumns() {
            return ImmutableMap.of();
        }

        @Override
        public List<Row> getRows() {
            return Lists.newArrayList(Iterators.transform(branch.getRuns().iterator(0, 100), new Function<BlueRun, Row>() {
                @Override
                public Row apply(BlueRun input) {
                    return new RowImpl(input);
                }
            }));
        }
    }

    public static class RowImpl extends BlueTable.Row implements CustomExportedBean {

        private final BlueRun run;

        public RowImpl(BlueRun run) {
            this.run = run;
        }

        @Override
        public String getId() {
            return run.getId();
        }

        @Override
        public Object toExportedObject() {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            builder.put("id", getId());
            for (BluePipelineNode node : run.getNodes()) {
                builder.put(node.getDisplayName(), node.getDurationInMillis());
            }
            return builder.build();
        }
    }

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            if (!(pipeline instanceof BranchImpl)) {
                return null;
            }
            BranchImpl branch = (BranchImpl)pipeline;
            return new StageRuntimeTrend(branch, parent);
        }
    }
}
