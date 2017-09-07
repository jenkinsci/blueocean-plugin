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
import io.jenkins.blueocean.rest.model.BlueTableRow;
import io.jenkins.blueocean.rest.model.BlueTrend;
import org.kohsuke.stapler.export.CustomExportedBean;

import java.util.List;
import java.util.Map;

/**
 * Trend for Stage durations
 */
public class StageDurationTrend extends BlueTrend {

    private final PipelineImpl pipeline;
    private final Link parent;

    public StageDurationTrend(PipelineImpl pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "stageDuration";
    }

    @Override
    public BlueTable getTable() {
        return new StageRuntimeTable(pipeline);
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    public static class StageRuntimeTable extends BlueTable {
        private final PipelineImpl pipeline;

        public StageRuntimeTable(PipelineImpl pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public Map<String, String> getColumns() {
            return ImmutableMap.of();
        }

        @Override
        public List<BlueTableRow> getRows() {
            return Lists.newArrayList(Iterators.transform(pipeline.getRuns().iterator(0, 100), new Function<BlueRun, BlueTableRow>() {
                @Override
                public BlueTableRow apply(BlueRun input) {
                    return new RowImpl(input);
                }
            }));
        }
    }

    public static class RowImpl extends BlueTableRow implements CustomExportedBean {

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
            if (!(pipeline instanceof PipelineImpl)) {
                return null;
            }
            PipelineImpl pipelineImpl = (PipelineImpl)pipeline;
            return new StageDurationTrend(pipelineImpl, parent);
        }
    }
}
